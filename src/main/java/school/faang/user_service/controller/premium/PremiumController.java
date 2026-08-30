package school.faang.user_service.controller.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.mapper.premium.PremiumMapper;
import school.faang.user_service.service.premium.PremiumService;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/premium")
@RestController
public class PremiumController {

    private final PremiumService premiumService;
    private final PremiumMapper premiumMapper;
    private final UserContext userContext;

    @PostMapping("/buy/{days}")
    @ResponseStatus(HttpStatus.CREATED)
    public PremiumDto buyPremium(@PathVariable int days) {
        long userId = userContext.getUserId();
        PremiumPeriod premiumPeriod = PremiumPeriod.fromDays(days);
        Premium premium = premiumService.buyPremium(userId, premiumPeriod);
        return premiumMapper.toDto(premium);
    }

    @GetMapping
    public ResponseEntity<List<Long>> getPremiumUsers() {
        return ResponseEntity.ok(premiumService.getPremiumUsers());
    }
}