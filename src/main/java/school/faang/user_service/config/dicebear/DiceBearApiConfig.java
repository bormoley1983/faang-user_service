package school.faang.user_service.config.dicebear;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;

@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "dicebear")
@Configuration
public class DiceBearApiConfig {
    private String apiUrl;
    private long connectionTimeoutSeconds;
    private long readTimeoutSeconds;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .readTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .build();
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    public DicebearStyleGenerator dicebearStyleGenerator(SecureRandom random) {
        return new DicebearStyleGenerator(random);
    }
}
