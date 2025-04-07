package school.faang.user_service.repository.premium;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import school.faang.user_service.entity.premium.Premium;

import java.time.LocalDateTime;
import java.util.List;

public interface PremiumRepository extends CrudRepository<Premium, Long> {

    boolean existsByUserId(long userId);

    List<Premium> findAllByEndDateBefore(LocalDateTime endDate);

    @Modifying
    @Query("DELETE FROM Premium p WHERE p.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Long> ids);
}
