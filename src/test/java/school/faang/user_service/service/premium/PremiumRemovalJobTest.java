package school.faang.user_service.service.premium;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.config.premium.PremiumConfig;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PremiumRemovalJobTest {

    @Mock
    private PremiumRepository premiumRepository;
    @Mock
    private PremiumConfig premiumConfig;
    @Mock
    private PremiumRemovalHelper removalHelper;
    @InjectMocks
    private PremiumRemovalJob job;

    @Test
    void execute_returnsWhenThereAreNoExpiredPremiums() {
        when(premiumRepository.findAllByEndDateBefore(any(LocalDateTime.class))).thenReturn(List.of());

        job.execute(null);

        verify(removalHelper, never()).deleteBatch(any());
    }

    @Test
    void execute_deletesExpiredPremiumsInConfiguredBatches() {
        Premium first = Premium.builder().id(1L).build();
        Premium second = Premium.builder().id(2L).build();
        Premium third = Premium.builder().id(3L).build();
        when(premiumRepository.findAllByEndDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(first, second, third));
        when(premiumConfig.getBatchSize()).thenReturn(2);

        job.execute(null);

        verify(removalHelper).deleteBatch(List.of(first, second));
        verify(removalHelper).deleteBatch(List.of(third));
    }
}
