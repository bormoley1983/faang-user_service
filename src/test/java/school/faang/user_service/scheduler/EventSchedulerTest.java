package school.faang.user_service.scheduler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import school.faang.user_service.entity.Country;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.entity.event.EventStatus;
import school.faang.user_service.entity.event.EventType;
import school.faang.user_service.repository.CountryRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.event.EventRepository;
import org.testcontainers.containers.Network;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration") // Marker to exclude this test from unit tests
@TestPropertySource(locations = "classpath:application-test.yaml")
@Testcontainers
@SpringBootTest
public class EventSchedulerTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");

        static Network testNetwork = Network.newNetwork();

    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer POSTGRESQL_CONTAINER =
        new PostgreSQLContainer(POSTGRES_IMAGE)
            .withNetwork(testNetwork)
            .withNetworkAliases("test-postgres")		        
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    private static final int SECONDS_TO_WAIT = 10;
    @DynamicPropertySource
    private static void setDatasourceProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        properties.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        properties.add("event.removal.cron", () -> String.format("*/%d * * * * *", SECONDS_TO_WAIT));
    }

//     @Autowired
//     private EventScheduler scheduler;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Test
    public void testClearEvents() {

        Country country = Country.builder()
                .title("Test country")
                .build();
        country = countryRepository.save(country);

        User user = User.builder()
                .username("test")
                .email("test@email.com")
                .password("test")
                .active(true)
                .country(Country.builder().id(country.getId()).build())
                .build();
        user = userRepository.save(user);

        Event event1 = Event.builder()
                .title("Test event1")
                .description("Test description1")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusDays(1).plusHours(1))
                .type(EventType.MEETING)
                .status(EventStatus.PLANNED)
                .location("Test location1")
                .maxAttendees(10)
                .owner(User.builder().id(user.getId()).build())
                .build();
        Event event2 = Event.builder()
                .title("Test event2")
                .description("Test description2")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusDays(1).plusHours(1))
                .type(EventType.MEETING)
                .status(EventStatus.PLANNED)
                .location("Test location2")
                .maxAttendees(10)
                .owner(User.builder().id(user.getId()).build())
                .build();

        List<Event> events = List.of(event1, event2);
        eventRepository.saveAll(events);

        await()
                .atMost(SECONDS_TO_WAIT + 5, TimeUnit.SECONDS)
                .untilAsserted(() -> events.forEach(e -> assertTrue(eventRepository.findById(e.getId()).isEmpty())));
    }
}
