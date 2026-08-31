package school.faang.user_service.listener.user_ban;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.event.UserBanEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.UserRepository;

import java.time.Duration;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.awaitility.Awaitility.await;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "spring.kafka.consumer.auto-offset-reset=earliest",
        }
)
@Sql(scripts = {"/UserService/drop.sql", "/UserService/user_initial.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/UserService/drop.sql", executionPhase = AFTER_TEST_METHOD)
@Testcontainers
public class UserBanIntegrationTest {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;


    @Value("${spring.kafka.topics.user-ban-topic.name}")
    private String userBanTopicName;

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("apache/kafka:4.3.1");
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");

    static Network testNetwork = Network.newNetwork();

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer KAFKA_CONTAINER =
        new KafkaContainer(KAFKA_IMAGE)
            .withNetwork(testNetwork)
            .withNetworkAliases("test-kafka");

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

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @Test
    public void banUserListenerTest() throws JsonProcessingException {
        for (MessageListenerContainer listenerContainer : registry.getAllListenerContainers()) {
            ContainerTestUtils.waitForAssignment(listenerContainer, 1);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String event = objectMapper.writeValueAsString(new UserBanEvent(1L, true));
        kafkaTemplate.send(userBanTopicName, event);

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    Optional<User> optionalUser = userRepository.findById(1L);
                    assertThat(optionalUser).isPresent();
                    assertThat(optionalUser.get().getBanned()).isTrue();
                });
    }
}
