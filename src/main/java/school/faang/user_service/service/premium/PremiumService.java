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
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.entity.premium.PremiumPurchaseIntent;
import school.faang.user_service.entity.premium.PremiumPurchaseStatus;
import school.faang.user_service.exception.PaymentFailedException;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PremiumService {
    private final PremiumRepository premiumRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final PremiumIntentService premiumIntentService;

    public Premium buyPremium(long userId, PremiumPeriod premiumPeriod, UUID idempotencyKey) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }
        PremiumPurchaseIntent intent = premiumIntentService.createOrLoad(userId, premiumPeriod, idempotencyKey);
        if (intent.getStatus() == PremiumPurchaseStatus.COMPLETED) {
            return intent.getPremium();
        }
        PaymentResponse response = makePayment(intent);
        return premiumIntentService.complete(intent.getId(), response);
    }

    @Transactional(readOnly = true)
    public List<Long> getPremiumUsers() {
        return premiumRepository.findByEndDateAfter(LocalDateTime.now())
                .stream()
                .map(premium -> premium.getUser().getId())
                .toList();
    }

    private PaymentResponse makePayment(PremiumPurchaseIntent intent) {
        PaymentRequest request = new PaymentRequest(intent.getPaymentNumber(), intent.getAmount(), Currency.USD);

        ResponseEntity<PaymentResponse> response = paymentServiceClient.sendPayment(request);
        PaymentResponse responseBody = response.getBody();

        if (response.getStatusCode() != HttpStatus.OK || responseBody == null) {
            String message = responseBody != null && responseBody.message() != null
                    ? responseBody.message()
                    : "Payment service returned no body with status " + response.getStatusCode();
            log.error("Payment failed for premium intent {} with status {}", intent.getId(), response.getStatusCode());
            throw new PaymentFailedException(message);
        }

        // Verify the payment actually succeeded, not just that the HTTP call was OK.
        if (responseBody.status() != PaymentStatus.SUCCESS) {
            String message = responseBody.message() != null
                    ? responseBody.message()
                    : "Payment status is " + responseBody.status();
            log.error("Payment for premium intent {} did not succeed: {}", intent.getId(), responseBody.status());
            throw new PaymentFailedException(message);
        }
        if (responseBody.paymentNumber() != intent.getPaymentNumber()) {
            throw new PaymentFailedException("Payment response number does not match premium purchase intent");
        }
        return responseBody;
    }
}
