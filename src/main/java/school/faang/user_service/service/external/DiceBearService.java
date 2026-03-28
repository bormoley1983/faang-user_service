package school.faang.user_service.service.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import school.faang.user_service.config.dicebear.DiceBearApiConfig;
import school.faang.user_service.config.dicebear.DicebearStyleGenerator;
import school.faang.user_service.dto.avatar.AvatarType;
import school.faang.user_service.exception.DiceBearException;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiceBearService {

    private final RestTemplate restTemplate;
    private final DiceBearApiConfig diceBearApiConfig;
    private final DicebearStyleGenerator styleGenerator;

    public byte[] generateAvatar(String filename, AvatarType type) {
        String style = styleGenerator.getRandomStyleString();

        String url = UriComponentsBuilder.fromUriString(diceBearApiConfig.getApiUrl())
                .pathSegment(style)
                .pathSegment(type.name().toLowerCase())
                .queryParam("seed", filename)
                .queryParam("width", type.getWidth())
                .queryParam("height", type.getHeight())
                .toUriString();

        return executeWithRetry(() -> fetchAvatarFromApi(url));
    }

    private byte[] fetchAvatarFromApi(String url) {
        try {
            return restTemplate.getForObject(url, byte[].class);
        } catch (Exception e) {
            log.error("Error fetching avatar from DiceBear API: {}", url, e);
            throw new DiceBearException("Failed to fetch avatar from DiceBear API");
        }
    }

    private byte[] executeWithRetry(RetryableOperation operation) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                return operation.execute();
            } catch (DiceBearException e) {
                attempts++;
                log.warn("Retrying DiceBear API request (attempt {}/{})", attempts, 3);
                if (attempts == 3) {
                    throw e;
                }
            }
        }
        throw new DiceBearException("Max retries reached for DiceBear API");
    }

    @FunctionalInterface
    private interface RetryableOperation {
        byte[] execute();
    }
}
