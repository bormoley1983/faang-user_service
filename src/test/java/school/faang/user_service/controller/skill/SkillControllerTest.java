package school.faang.user_service.controller.skill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.mapper.SkillMapper;
import school.faang.user_service.service.skill.SkillService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SkillControllerTest {

    @Mock
    private SkillService skillService;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldReturnMappedSkillDto() {
        SkillDto in = new SkillDto();
        Skill entity = new Skill();
        Skill created = new Skill();
        SkillDto out = new SkillDto();
        when(skillMapper.toEntity(in)).thenReturn(entity);
        when(skillService.create(entity)).thenReturn(created);
        when(skillMapper.toSkillDto(created)).thenReturn(out);

        ResponseEntity<SkillDto> response = controller.create(in);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void getUserSkills_ShouldReturnMappedList() {
        List<Skill> skills = Collections.singletonList(new Skill());
        List<SkillDto> dtos = Collections.singletonList(new SkillDto());
        when(skillService.getUserSkills(1L)).thenReturn(skills);
        when(skillMapper.toSkillDtoList(skills)).thenReturn(dtos);

        ResponseEntity<List<SkillDto>> response = controller.getUserSkills(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }

    @Test
    void getOfferedSkills_ShouldReturnMappedCandidates() {
        List<Skill> skills = Collections.singletonList(new Skill());
        List<SkillCandidateDto> dtos = Collections.singletonList(new SkillCandidateDto());
        when(skillService.getOfferedSkills(1L)).thenReturn(skills);
        when(skillMapper.toSkillCandidateDtoList(skills)).thenReturn(dtos);

        ResponseEntity<List<SkillCandidateDto>> response = controller.getOfferedSkills(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }

    @Test
    void acquireSkillFromOffers_ShouldReturnMappedSkillDto() {
        Skill acquired = new Skill();
        SkillDto out = new SkillDto();
        when(skillService.acquireSkillFromOffers(1L, 2L)).thenReturn(acquired);
        when(skillMapper.toSkillDto(acquired)).thenReturn(out);

        ResponseEntity<SkillDto> response = controller.acquireSkillFromOffers(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }
}
