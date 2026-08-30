package school.faang.user_service.repository.premium;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import school.faang.user_service.entity.premium.PremiumPurchaseIntent;

import java.util.Optional;
import java.util.UUID;

public interface PremiumPurchaseIntentRepository extends JpaRepository<PremiumPurchaseIntent, Long> {
    Optional<PremiumPurchaseIntent> findByIdempotencyKey(UUID idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM PremiumPurchaseIntent i WHERE i.id = :id")
    Optional<PremiumPurchaseIntent> findByIdForUpdate(Long id);
}
