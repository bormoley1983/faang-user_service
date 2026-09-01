package school.faang.user_service.controller.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.dto.RecommendationRequestResponseDto;
import school.faang.user_service.dto.RejectionDto;
import school.faang.user_service.dto.RequestFilterDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.mapper.RecommendationRequestMapper;
import school.faang.user_service.service.recommendation.RecommendationRequestService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationRequestControllerTest {

    @Mock
    private RecommendationRequestService recommendationRequestService;

    @Mock
    private RecommendationRequestMapper recommendationRequestMapper;

    @InjectMocks
    private RecommendationRequestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestRecommendation_ShouldReturnOkResponseDto() {
        RecommendationRequestDto in = new RecommendationRequestDto();
        RecommendationRequest entity = new RecommendationRequest();
        RecommendationRequest created = new RecommendationRequest();
        RecommendationRequestResponseDto out = new RecommendationRequestResponseDto();
        when(recommendationRequestMapper.toEntity(in)).thenReturn(entity);
        when(recommendationRequestService.create(entity)).thenReturn(created);
        when(recommendationRequestMapper.toResponseDto(created)).thenReturn(out);

        ResponseEntity<RecommendationRequestResponseDto> response = controller.requestRecommendation(in);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void getRecommendationRequests_ShouldReturnMappedList() {
        RequestFilterDto filter = new RequestFilterDto();
        List<RecommendationRequest> entities = Collections.singletonList(new RecommendationRequest());
        List<RecommendationRequestResponseDto> dtos = Collections.singletonList(new RecommendationRequestResponseDto());
        when(recommendationRequestService.getRequests(filter)).thenReturn(entities);
        when(recommendationRequestMapper.toResponseDtoList(entities)).thenReturn(dtos);

        ResponseEntity<List<RecommendationRequestResponseDto>> response = controller.getRecommendationRequests(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }

    @Test
    void getRecommendationRequest_ShouldReturnMappedDto() {
        RecommendationRequest entity = new RecommendationRequest();
        RecommendationRequestResponseDto out = new RecommendationRequestResponseDto();
        when(recommendationRequestService.getRequest(7L)).thenReturn(entity);
        when(recommendationRequestMapper.toResponseDto(entity)).thenReturn(out);

        ResponseEntity<RecommendationRequestResponseDto> response = controller.getRecommendationRequest(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void rejectRequest_ShouldPassReasonAndReturnMappedDto() {
        RejectionDto rejection = new RejectionDto();
        rejection.setReason("not now");
        RecommendationRequest rejected = new RecommendationRequest();
        RecommendationRequestResponseDto out = new RecommendationRequestResponseDto();
        when(recommendationRequestService.rejectRequest(3L, "not now")).thenReturn(rejected);
        when(recommendationRequestMapper.toResponseDto(rejected)).thenReturn(out);

        ResponseEntity<RecommendationRequestResponseDto> response = controller.rejectRequest(3L, rejection);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
        verify(recommendationRequestService).rejectRequest(3L, "not now");
    }
}
