package school.faang.user_service.service.premium;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.repository.premium.PremiumRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PremiumService {

    private final PremiumRepository premiumRepository;

    @Value("${user-premium.partition-size}")
    private int partitionSize;

    @Async
    public void removeExpiredPremiumAccess() {
        LocalDateTime now = LocalDateTime.now();
        try {
            List<Premium> expiredPremiums = premiumRepository.findAllByEndDateBefore(now);
            if (!expiredPremiums.isEmpty()) {
                List<Long> expiredIds = getPremiumIds(expiredPremiums);
                List<List<Long>> idBatches = ListUtils.partition(expiredIds, partitionSize);
                for (List<Long> batch : idBatches) {
                    premiumRepository.deleteByIdIn(batch);
                    log.info("Expired premium accesses before date: {} - was deleted in thread - {}",
                            now, Thread.currentThread().getName());
                }
            }
        } catch (Exception e) {
            log.error("Error during expired premium access removal: {}", e.getMessage(), e);
        }
    }

    private List<Long> getPremiumIds(List<Premium> premiums) {
        return premiums.stream()
                .map(Premium::getId)
                .toList();
    }

}
