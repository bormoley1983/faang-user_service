package school.faang.user_service.controller.premium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.mapper.premium.PremiumMapper;
import school.faang.user_service.service.premium.PremiumService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PremiumControllerTest {

    @Mock
    private PremiumService premiumService;

    @Mock
    private PremiumMapper premiumMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private PremiumController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userContext.getUserId()).thenReturn(1L);
    }

    @Test
    void buyPremium_ShouldMapPeriodAndReturnDto() {
        UUID key = UUID.randomUUID();
        Premium premium = new Premium();
        PremiumDto dto = new PremiumDto();
        when(premiumService.buyPremium(1L, PremiumPeriod.fromDays(30), key)).thenReturn(premium);
        when(premiumMapper.toDto(premium)).thenReturn(dto);

        PremiumDto result = controller.buyPremium(30, key);

        assertEquals(dto, result);
        verify(premiumService).buyPremium(1L, PremiumPeriod.fromDays(30), key);
    }

    @Test
    void getPremiumUsers_ShouldReturnIds() {
        List<Long> ids = List.of(1L, 2L);
        when(premiumService.getPremiumUsers()).thenReturn(ids);

        ResponseEntity<List<Long>> response = controller.getPremiumUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ids, response.getBody());
    }
}
