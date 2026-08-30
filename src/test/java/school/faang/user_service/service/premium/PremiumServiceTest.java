package school.faang.user_service.service.premium;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PremiumServiceTest {

    @InjectMocks
    private PremiumService premiumService;

    @Mock
    private PremiumRepository premiumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private PremiumIntentService premiumIntentService;

    @Captor
    private ArgumentCaptor<Premium> premiumArgumentCaptor;

    private final long userId = 1;
    private final PremiumPeriod premiumPeriod = PremiumPeriod.MONTH;
    private final UUID idempotencyKey = UUID.randomUUID();

    @Test
    public void testPremiumExistByUserId() {
        when(premiumIntentService.createOrLoad(userId, premiumPeriod, idempotencyKey))
                .thenThrow(new IllegalStateException("User with id " + userId + " already has a premium subscription."));

        assertThrows(IllegalStateException.class,
                () -> premiumService.buyPremium(userId, premiumPeriod, idempotencyKey));
    }

    @Test
    public void testPaymentFailed() {
        Pair<PaymentRequest, ResponseEntity<PaymentResponse>> paymentPair = setUpPaymentRequestAndResponse(false);
        PremiumPurchaseIntent intent = pendingIntent();

        when(premiumIntentService.createOrLoad(userId, premiumPeriod, idempotencyKey))
                .thenReturn(intent);

        when(paymentServiceClient.sendPayment(any(PaymentRequest.class)))
                .thenReturn(paymentPair.getSecond());

        assertThrows(PaymentFailedException.class,
                () -> premiumService.buyPremium(userId, premiumPeriod, idempotencyKey));
    }

    @Test
    public void testPremiumFindByUserId() {
        when(premiumIntentService.createOrLoad(userId, premiumPeriod, idempotencyKey))
                .thenThrow(new NoSuchElementException());

        assertThrows(NoSuchElementException.class,
                () -> premiumService.buyPremium(userId, premiumPeriod, idempotencyKey));
    }

    @Test
    public void testSavePremium() {
        Pair<PaymentRequest, ResponseEntity<PaymentResponse>> paymentPair = setUpPaymentRequestAndResponse(true);
        PremiumPurchaseIntent intent = pendingIntent();

        when(premiumIntentService.createOrLoad(userId, premiumPeriod, idempotencyKey))
                .thenReturn(intent);

        when(paymentServiceClient.sendPayment(any(PaymentRequest.class)))
                .thenReturn(paymentPair.getSecond());

        Premium savedPremium = Premium.builder().build();
        when(premiumIntentService.complete(eq(intent.getId()), any(PaymentResponse.class)))
                .thenReturn(savedPremium);

        premiumService.buyPremium(userId, premiumPeriod, idempotencyKey);

        verify(premiumIntentService, times(1))
                .complete(eq(intent.getId()), any(PaymentResponse.class));
    }

    private PremiumPurchaseIntent pendingIntent() {
        PremiumPurchaseIntent intent = new PremiumPurchaseIntent();
        intent.setId(1L);
        intent.setUserId(userId);
        intent.setPremiumPeriod(premiumPeriod);
        intent.setPaymentNumber(12345L);
        intent.setAmount(BigDecimal.valueOf(premiumPeriod.getPrice()));
        intent.setStatus(PremiumPurchaseStatus.PENDING);
        return intent;
    }

    private Pair<PaymentRequest, ResponseEntity<PaymentResponse>> setUpPaymentRequestAndResponse(boolean isSuccessResponse) {
        long paymentNumber = 12345L;
        BigDecimal amount = BigDecimal.valueOf(99.99);
        Currency currency = Currency.USD;
        PaymentRequest paymentRequest = new PaymentRequest(paymentNumber, amount, currency);

        PaymentResponse paymentResponse = new PaymentResponse(
                PaymentStatus.SUCCESS,
                1234,
                paymentNumber,
                amount,
                currency,
                "Payment successful"
        );

        if (isSuccessResponse) {
            return Pair.of(paymentRequest, ResponseEntity.ok(paymentResponse));
        }
        return Pair.of(
                paymentRequest,
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(paymentResponse)
        );
    }
}
