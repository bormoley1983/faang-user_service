package school.faang.user_service.service.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.premium.*;
import school.faang.user_service.exception.PaymentFailedException;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.premium.PremiumPurchaseIntentRepository;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PremiumIntentService {
    private final PremiumPurchaseIntentRepository intentRepository;
    private final PremiumRepository premiumRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PremiumPurchaseIntent createOrLoad(long userId, PremiumPeriod period, UUID key) {
        PremiumPurchaseIntent existing = intentRepository.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!existing.getUserId().equals(userId) || existing.getPremiumPeriod() != period) {
                throw new IllegalArgumentException("Idempotency key was already used for another premium purchase");
            }
            return existing;
        }
        userRepository.findById(userId).orElseThrow();
        if (premiumRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User with id " + userId + " already has a premium subscription.");
        }
        PremiumPurchaseIntent intent = new PremiumPurchaseIntent();
        intent.setIdempotencyKey(key);
        intent.setUserId(userId);
        intent.setPremiumPeriod(period);
        intent.setPaymentNumber(stablePaymentNumber(key));
        intent.setAmount(BigDecimal.valueOf(period.getPrice()));
        intent.setStatus(PremiumPurchaseStatus.PENDING);
        intent.setCreatedAt(LocalDateTime.now());
        return intentRepository.save(intent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Premium complete(long intentId, PaymentResponse response) {
        PremiumPurchaseIntent intent = intentRepository.findByIdForUpdate(intentId).orElseThrow();
        if (intent.getStatus() == PremiumPurchaseStatus.COMPLETED) {
            return intent.getPremium();
        }
        if (response == null || response.status() != PaymentStatus.SUCCESS
                || response.paymentNumber() != intent.getPaymentNumber()) {
            throw new PaymentFailedException("Payment response does not match premium purchase intent");
        }
        User user = userRepository.findById(intent.getUserId()).orElseThrow();
        if (premiumRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("User with id " + user.getId() + " already has a premium subscription.");
        }
        LocalDateTime start = LocalDateTime.now();
        Premium premium = Premium.builder().user(user).startDate(start)
                .endDate(start.plusDays(intent.getPremiumPeriod().getDays())).build();
        premium = premiumRepository.save(premium);
        intent.setPremium(premium);
        intent.setStatus(PremiumPurchaseStatus.COMPLETED);
        intentRepository.save(intent);
        return premium;
    }

    private long stablePaymentNumber(UUID key) {
        long value = (key.getMostSignificantBits() ^ key.getLeastSignificantBits()) & Long.MAX_VALUE;
        return value == 0 ? 1 : value;
    }
}
