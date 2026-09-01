package school.faang.user_service.controller.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.entity.recommendation.Recommendation;
import school.faang.user_service.mapper.RecommendationMapper;
import school.faang.user_service.service.recommendation.RecommendationService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private RecommendationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void giveRecommendation_ShouldReturnCreatedDto() {
        RecommendationDto in = new RecommendationDto();
        Recommendation entity = new Recommendation();
        Recommendation created = new Recommendation();
        RecommendationDto out = new RecommendationDto();
        when(recommendationMapper.toEntity(in)).thenReturn(entity);
        when(recommendationService.create(entity)).thenReturn(created);
        when(recommendationMapper.toDto(created)).thenReturn(out);

        ResponseEntity<RecommendationDto> response = controller.giveRecommendation(in);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void updateRecommendation_ShouldReturnOkDto() {
        RecommendationDto in = new RecommendationDto();
        Recommendation entity = new Recommendation();
        Recommendation updated = new Recommendation();
        RecommendationDto out = new RecommendationDto();
        when(recommendationMapper.toEntity(in)).thenReturn(entity);
        when(recommendationService.update(entity)).thenReturn(updated);
        when(recommendationMapper.toDto(updated)).thenReturn(out);

        ResponseEntity<RecommendationDto> response = controller.updateRecommendation(in);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void deleteRecommendation_ShouldReturnNoContent() {
        ResponseEntity<Void> response = controller.deleteRecommendation(5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(recommendationService).delete(5L);
    }

    @Test
    void getAllUserRecommendations_ShouldReturnMappedList() {
        List<Recommendation> entities = Collections.singletonList(new Recommendation());
        List<RecommendationDto> dtos = Collections.singletonList(new RecommendationDto());
        when(recommendationService.getAllUserRecommendations(1L)).thenReturn(entities);
        when(recommendationMapper.toRecommendationDtoList(entities)).thenReturn(dtos);

        ResponseEntity<List<RecommendationDto>> response = controller.getAllUserRecommendations(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }

    @Test
    void getAllGivenRecommendations_ShouldReturnMappedList() {
        List<Recommendation> entities = Collections.singletonList(new Recommendation());
        List<RecommendationDto> dtos = Collections.singletonList(new RecommendationDto());
        when(recommendationService.getAllGivenRecommendations(2L)).thenReturn(entities);
        when(recommendationMapper.toRecommendationDtoList(entities)).thenReturn(dtos);

        ResponseEntity<List<RecommendationDto>> response = controller.getAllGivenRecommendations(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }
}
