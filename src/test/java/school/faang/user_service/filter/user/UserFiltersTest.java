package school.faang.user_service.filter.user;

import org.junit.jupiter.api.Test;
import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.Country;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.contact.Contact;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserFiltersTest {

    // --- UserNameFilter ---

    private final UserNameFilter nameFilter = new UserNameFilter();

    @Test
    void name_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(nameFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void name_apply_whenUsernameContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().username("john_doe").build();
        UserFilterDto dto = UserFilterDto.builder().namePattern("john").build();

        // Act
        boolean result = nameFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void name_apply_whenUsernameDoesNotContainPattern_returnsFalse() {
        // Arrange
        User user = User.builder().username("john_doe").build();
        UserFilterDto dto = UserFilterDto.builder().namePattern("jane").build();

        // Act
        boolean result = nameFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserAboutFilter ---

    private final UserAboutFilter aboutFilter = new UserAboutFilter();

    @Test
    void about_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(aboutFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void about_apply_whenAboutContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().aboutMe("I love Java and Spring").build();
        UserFilterDto dto = UserFilterDto.builder().aboutPattern("Java").build();

        // Act
        boolean result = aboutFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void about_apply_whenAboutNull_returnsFalse() {
        // Arrange
        User user = User.builder().aboutMe(null).build();
        UserFilterDto dto = UserFilterDto.builder().aboutPattern("Java").build();

        // Act
        boolean result = aboutFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserEmailFilter ---

    private final UserEmailFilter emailFilter = new UserEmailFilter();

    @Test
    void email_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(emailFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void email_apply_whenEmailContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().email("john@example.com").build();
        UserFilterDto dto = UserFilterDto.builder().emailPattern("example").build();

        // Act
        boolean result = emailFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void email_apply_whenEmailNull_returnsFalse() {
        // Arrange
        User user = User.builder().email(null).build();
        UserFilterDto dto = UserFilterDto.builder().emailPattern("example").build();

        // Act
        boolean result = emailFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserCityFilter ---

    private final UserCityFilter cityFilter = new UserCityFilter();

    @Test
    void city_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(cityFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void city_apply_whenCityContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().city("New York").build();
        UserFilterDto dto = UserFilterDto.builder().cityPattern("York").build();

        // Act
        boolean result = cityFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void city_apply_whenCityNull_returnsFalse() {
        // Arrange
        User user = User.builder().city(null).build();
        UserFilterDto dto = UserFilterDto.builder().cityPattern("York").build();

        // Act
        boolean result = cityFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserPhoneFilter ---

    private final UserPhoneFilter phoneFilter = new UserPhoneFilter();

    @Test
    void phone_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(phoneFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void phone_apply_whenPhoneContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().phone("+1234567890").build();
        UserFilterDto dto = UserFilterDto.builder().phonePattern("123").build();

        // Act
        boolean result = phoneFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void phone_apply_whenPhoneNull_returnsFalse() {
        // Arrange
        User user = User.builder().phone(null).build();
        UserFilterDto dto = UserFilterDto.builder().phonePattern("123").build();

        // Act
        boolean result = phoneFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserCountryFilter ---

    private final UserCountryFilter countryFilter = new UserCountryFilter();

    @Test
    void country_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(countryFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void country_apply_whenCountryTitleContainsPattern_returnsTrue() {
        // Arrange
        User user = User.builder().country(Country.builder().title("United States").build()).build();
        UserFilterDto dto = UserFilterDto.builder().countryPattern("States").build();

        // Act
        boolean result = countryFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void country_apply_whenCountryNull_returnsFalse() {
        // Arrange
        User user = User.builder().country(null).build();
        UserFilterDto dto = UserFilterDto.builder().countryPattern("States").build();

        // Act / Assert
        assertThatThrownBy(() -> countryFilter.apply(user, dto))
                .isInstanceOf(NullPointerException.class);
    }

    // --- UserContactFilter ---

    private final UserContactFilter contactFilter = new UserContactFilter();

    @Test
    void contact_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(contactFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void contact_apply_whenContactContainsPattern_returnsTrue() {
        // Arrange
        Contact contact = Contact.builder().contact("telegram:john").build();
        User user = User.builder().contacts(List.of(contact)).build();
        UserFilterDto dto = UserFilterDto.builder().contactPattern("telegram").build();

        // Act
        boolean result = contactFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void contact_apply_whenNoContacts_returnsFalse() {
        // Arrange
        User user = User.builder().contacts(null).build();
        UserFilterDto dto = UserFilterDto.builder().contactPattern("telegram").build();

        // Act
        boolean result = contactFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserSkillFilter ---

    private final UserSkillFilter skillFilter = new UserSkillFilter();

    @Test
    void skill_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(skillFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void skill_apply_whenSkillTitleContainsPattern_returnsTrue() {
        // Arrange
        Skill skill = Skill.builder().title("Java").build();
        User user = User.builder().skills(List.of(skill)).build();
        UserFilterDto dto = UserFilterDto.builder().skillPattern("Java").build();

        // Act
        boolean result = skillFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void skill_apply_whenNoSkills_returnsFalse() {
        // Arrange
        User user = User.builder().skills(null).build();
        UserFilterDto dto = UserFilterDto.builder().skillPattern("Java").build();

        // Act
        boolean result = skillFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    // --- UserExperienceMinFilter ---

    private final UserExperienceMinFilter expMinFilter = new UserExperienceMinFilter();

    @Test
    void expMin_isApplicable_whenValueNull_returnsFalse() {
        assertThat(expMinFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void expMin_apply_whenExperienceAboveMin_returnsTrue() {
        // Arrange
        User user = User.builder().experience(5).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMin(3).build();

        // Act
        boolean result = expMinFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void expMin_apply_whenExperienceBelowMin_returnsFalse() {
        // Arrange
        User user = User.builder().experience(1).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMin(3).build();

        // Act
        boolean result = expMinFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void expMin_apply_whenExperienceNull_treatedAsZero() {
        // Arrange
        User user = User.builder().experience(null).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMin(0).build();

        // Act
        boolean result = expMinFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    // --- UserExperienceMaxFilter ---

    private final UserExperienceMaxFilter expMaxFilter = new UserExperienceMaxFilter();

    @Test
    void expMax_isApplicable_whenValueNull_returnsFalse() {
        assertThat(expMaxFilter.isApplicable(UserFilterDto.builder().build())).isFalse();
    }

    @Test
    void expMax_apply_whenExperienceBelowMax_returnsTrue() {
        // Arrange
        User user = User.builder().experience(2).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMax(5).build();

        // Act
        boolean result = expMaxFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void expMax_apply_whenExperienceAboveMax_returnsFalse() {
        // Arrange
        User user = User.builder().experience(10).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMax(5).build();

        // Act
        boolean result = expMaxFilter.apply(user, dto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void expMax_apply_whenExperienceNull_treatedAsZero() {
        // Arrange
        User user = User.builder().experience(null).build();
        UserFilterDto dto = UserFilterDto.builder().experienceMax(0).build();

        // Act
        boolean result = expMaxFilter.apply(user, dto);

        // Assert
        assertThat(result).isTrue();
    }
}
