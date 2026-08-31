package school.faang.user_service.service.premium;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PremiumRemovalHelperTest {

    @Mock
    private PremiumRepository premiumRepository;
    @InjectMocks
    private PremiumRemovalHelper helper;

    @Test
    void deleteBatch_deletesEveryPremiumInBatch() {
        List<Premium> batch = List.of(Premium.builder().id(1L).build(), Premium.builder().id(2L).build());

        helper.deleteBatch(batch);

        verify(premiumRepository).deleteAll(batch);
    }

    @Test
    void deleteBatch_whenEmptyBatch_deletesNothing() {
        // Arrange
        List<Premium> batch = List.of();

        // Act
        helper.deleteBatch(batch);

        // Assert
        verify(premiumRepository).deleteAll(batch);
    }

    @Test
    void deleteBatch_whenSingleElement_deletesIt() {
        // Arrange
        Premium premium = Premium.builder().id(99L).build();
        List<Premium> batch = List.of(premium);

        // Act
        helper.deleteBatch(batch);

        // Assert
        verify(premiumRepository).deleteAll(batch);
    }
}
