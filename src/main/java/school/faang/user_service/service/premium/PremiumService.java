package school.faang.user_service.service.premium;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.client.PaymentServiceClient;
import school.faang.user_service.dto.payment.Currency;
import school.faang.user_service.dto.payment.PaymentRequest;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.exception.PaymentFailedException;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PremiumService {
    private final PremiumRepository premiumRepository;
    private final UserRepository userRepository;
    private final PaymentServiceClient paymentServiceClient;

    @Transactional
    public Premium buyPremium(long userId, PremiumPeriod premiumPeriod) {
        User user = userRepository.findById(userId).orElseThrow();
        if (premiumRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("User with id " + userId + " already has a premium subscription.");
        }

        makePayment(premiumPeriod);

        Premium premium = Premium.builder()
                .user(user)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(premiumPeriod.getDays()))
                .build();

        return premiumRepository.save(premium);
    }

    @Transactional(readOnly = true)
    public List<Long> getPremiumUsers() {
        return premiumRepository.findByEndDateAfter(LocalDateTime.now())
                .stream()
                .map(premium -> premium.getUser().getId())
                .toList();
    }

    private void makePayment(PremiumPeriod premiumPeriod) {
        // Use a positive payment number: getLeastSignificantBits() can be negative.
        long paymentNumber = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        if (paymentNumber == 0) {
            paymentNumber = 1;
        }
        PaymentRequest request = new PaymentRequest(paymentNumber,
                BigDecimal.valueOf(premiumPeriod.getPrice()), Currency.USD);

        ResponseEntity<PaymentResponse> response = paymentServiceClient.sendPayment(request);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            String message = response.getBody() != null && response.getBody().message() != null
                    ? response.getBody().message()
                    : "Payment service returned no body with status " + response.getStatusCode();
            log.error("Payment failed for period {} with status {}", premiumPeriod, response.getStatusCode());
            throw new PaymentFailedException(message);
        }

        // Verify the payment actually succeeded, not just that the HTTP call was OK.
        if (response.getBody().status() != PaymentStatus.SUCCESS) {
            String message = response.getBody().message() != null
                    ? response.getBody().message()
                    : "Payment status is " + response.getBody().status();
            log.error("Payment for period {} did not succeed: {}", premiumPeriod, response.getBody().status());
            throw new PaymentFailedException(message);
        }
    }
}