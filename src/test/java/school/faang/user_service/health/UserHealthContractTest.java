package school.faang.user_service.health;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-level contract for the published, unauthenticated health endpoints that
 * Kubernetes probes rely on. These are bounded: liveness is dependency-free (ping)
 * and readiness reflects core dependencies (db, redis, ping). Both must return HTTP 200
 * under normal dependency conditions so the workload can be probed over HTTP.
 *
 * <p>The context boots against real Postgres and Redis containers so that the readiness
 * group's {@code db} and {@code redis} indicators report UP, mirroring the production
 * probe contract rather than a mocked environment.
 */
@Testcontainers
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserHealthContractTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");

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

    @Container
    @SuppressWarnings("resource")
    protected static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(REDIS_IMAGE)
                    .withNetwork(testNetwork)
                    .withNetworkAliases("test-redis");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void livenessEndpoint_ShouldReturn200AndUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health/liveness");

        assertEquals(200, response.statusCode(), "liveness probe endpoint must return HTTP 200");
        assertTrue(response.body().contains("\"status\":\"UP\""),
                "liveness body should report UP, was: " + response.body());
    }

    @Test
    void readinessEndpoint_ShouldReturn200AndUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health/readiness");

        assertEquals(200, response.statusCode(), "readiness probe endpoint must return HTTP 200");
        assertTrue(response.body().contains("\"status\":\"UP\""),
                "readiness body should report UP, was: " + response.body());
    }

    @Test
    void unmappedRoute_ShouldReturn404Not500() throws Exception {
        HttpResponse<String> response = get("/this-route-does-not-exist");

        assertEquals(404, response.statusCode(),
                "unmapped routes must return HTTP 404, not the generic HTTP 500");
    }
}
