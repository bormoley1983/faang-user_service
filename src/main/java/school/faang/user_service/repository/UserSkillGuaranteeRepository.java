package school.faang.user_service.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import school.faang.user_service.entity.UserSkillGuarantee;

import java.util.List;

public interface UserSkillGuaranteeRepository extends CrudRepository<UserSkillGuarantee, Long> {
    @Query(nativeQuery = true, value = """
            SELECT g.* FROM user_skill_guarantee g
            WHERE user_id = ?1
            """)
    List<UserSkillGuarantee> findAllByUserId(long userId);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM user_skill_guarantee
            WHERE guarantor_id = :guarantorId
    """)
    void deleteAllByGuarantorId(@Param("guarantorId") Long guarantorId);

    /**
     * Deletes only the guarantees that were created for the skills offered in a specific
     * recommendation, instead of all guarantees given by the author.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM user_skill_guarantee g
            WHERE g.guarantor_id = :guarantorId
              AND g.user_id = :userId
              AND g.skill_id IN (
                  SELECT so.skill_id FROM skill_offer so
                  JOIN recommendation r ON r.id = so.recommendation_id
                  WHERE r.id = :recommendationId
              )
            """)
    void deleteByRecommendation(@Param("guarantorId") Long guarantorId,
                                @Param("userId") Long userId,
                                @Param("recommendationId") Long recommendationId);
}