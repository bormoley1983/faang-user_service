package school.faang.user_service.entity;

import org.junit.jupiter.api.Test;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.premium.PremiumPurchaseIntent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityEqualityTest {

    @Test
    void user_equalityIsBasedOnIdOnly() {
        // Arrange
        User a = new User();
        a.setId(1L);
        a.setUsername("alice");
        a.setEmail("alice@example.com");

        User b = new User();
        b.setId(1L);
        b.setUsername("different");
        b.setEmail("different@example.com");

        User c = new User();
        c.setId(2L);
        c.setUsername("alice");
        c.setEmail("alice@example.com");

        // Act + Assert: same id -> equal, different id -> not equal
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void goal_equalityIsBasedOnIdOnly() {
        // Arrange
        Goal a = Goal.builder().id(10L).title("t1").build();
        Goal b = Goal.builder().id(10L).title("other").build();
        Goal c = Goal.builder().id(11L).title("t1").build();

        // Act + Assert
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void premiumPurchaseIntent_equalityIsBasedOnIdOnly() {
        // Arrange
        PremiumPurchaseIntent a = new PremiumPurchaseIntent();
        a.setId(100L);
        a.setIdempotencyKey(UUID.randomUUID());

        PremiumPurchaseIntent b = new PremiumPurchaseIntent();
        b.setId(100L);
        b.setIdempotencyKey(UUID.randomUUID());

        PremiumPurchaseIntent c = new PremiumPurchaseIntent();
        c.setId(101L);
        c.setIdempotencyKey(a.getIdempotencyKey());

        // Act + Assert
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }
}
