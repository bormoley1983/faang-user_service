package school.faang.user_service.service.premium;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.payment.Currency;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.entity.premium.PremiumPurchaseIntent;
import school.faang.user_service.entity.premium.PremiumPurchaseStatus;
import school.faang.user_service.exception.PaymentFailedException;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.premium.PremiumPurchaseIntentRepository;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PremiumIntentServiceTest {

    @Mock
    private PremiumPurchaseIntentRepository intentRepository;
    @Mock
    private PremiumRepository premiumRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PremiumIntentService service;

    @Test
    void createOrLoad_returnsMatchingExistingIntent() {
        UUID key = UUID.randomUUID();
        PremiumPurchaseIntent existing = intent(1L, 4L, PremiumPeriod.MONTH, 42L);
        when(intentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        assertSame(existing, service.createOrLoad(4L, PremiumPeriod.MONTH, key));
    }

    @Test
    void createOrLoad_rejectsReusedKeyForDifferentPurchase() {
        UUID key = UUID.randomUUID();
        PremiumPurchaseIntent existing = intent(1L, 4L, PremiumPeriod.MONTH, 42L);
        when(intentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.createOrLoad(5L, PremiumPeriod.MONTH, key));
    }

    @Test
    void createOrLoad_createsPendingIntentWithStablePaymentNumber() {
        UUID key = new UUID(0L, 0L);
        User user = User.builder().id(4L).build();
        when(intentRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(premiumRepository.existsByUserId(4L)).thenReturn(false);
        when(intentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PremiumPurchaseIntent result = service.createOrLoad(4L, PremiumPeriod.THREE_MONTHS, key);

        assertEquals(PremiumPurchaseStatus.PENDING, result.getStatus());
        assertEquals(1L, result.getPaymentNumber());
        assertEquals(BigDecimal.valueOf(PremiumPeriod.THREE_MONTHS.getPrice()), result.getAmount());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createOrLoad_rejectsUserWhoAlreadyHasPremium() {
        UUID key = UUID.randomUUID();
        when(intentRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(userRepository.findById(4L)).thenReturn(Optional.of(User.builder().id(4L).build()));
        when(premiumRepository.existsByUserId(4L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.createOrLoad(4L, PremiumPeriod.MONTH, key));
    }

    @Test
    void complete_returnsPremiumWhenIntentWasAlreadyCompleted() {
        Premium premium = Premium.builder().build();
        PremiumPurchaseIntent existing = intent(1L, 4L, PremiumPeriod.MONTH, 42L);
        existing.setStatus(PremiumPurchaseStatus.COMPLETED);
        existing.setPremium(premium);
        when(intentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

        assertSame(premium, service.complete(1L, successfulResponse(42L)));
        verify(premiumRepository, never()).save(any());
    }

    @Test
    void complete_rejectsMismatchedPayment() {
        PremiumPurchaseIntent existing = intent(1L, 4L, PremiumPeriod.MONTH, 42L);
        when(intentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

        assertThrows(PaymentFailedException.class,
                () -> service.complete(1L, successfulResponse(99L)));
    }

    @Test
    void complete_createsPremiumAndMarksIntentCompleted() {
        User user = User.builder().id(4L).build();
        PremiumPurchaseIntent existing = intent(1L, 4L, PremiumPeriod.MONTH, 42L);
        when(intentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(premiumRepository.existsByUserId(4L)).thenReturn(false);
        when(premiumRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Premium result = service.complete(1L, successfulResponse(42L));

        ArgumentCaptor<PremiumPurchaseIntent> captor = ArgumentCaptor.forClass(PremiumPurchaseIntent.class);
        verify(intentRepository).save(captor.capture());
        assertSame(user, result.getUser());
        assertEquals(result.getStartDate().plusDays(30), result.getEndDate());
        assertEquals(PremiumPurchaseStatus.COMPLETED, captor.getValue().getStatus());
        assertSame(result, captor.getValue().getPremium());
    }

    private PremiumPurchaseIntent intent(long id, long userId, PremiumPeriod period, long paymentNumber) {
        PremiumPurchaseIntent intent = new PremiumPurchaseIntent();
        intent.setId(id);
        intent.setUserId(userId);
        intent.setPremiumPeriod(period);
        intent.setPaymentNumber(paymentNumber);
        intent.setStatus(PremiumPurchaseStatus.PENDING);
        return intent;
    }

    private PaymentResponse successfulResponse(long paymentNumber) {
        return new PaymentResponse(PaymentStatus.SUCCESS, 1234, paymentNumber,
                BigDecimal.TEN, Currency.USD, "ok");
    }
}
