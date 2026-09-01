package school.faang.user_service.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.InjectionStrategy;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.entity.event.Event;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = SkillMapper.class,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface EventMapper {
    @Mapping(target = "relatedSkills", source = "relatedSkills", qualifiedByName = "relatedSkillsToDto")
    @Mapping(target = "startTime", source = "startDate")
    @Mapping(target = "endTime", source = "endDate")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "eventType", source = "type")
    @Mapping(target = "eventStatus", source = "status")
    EventDto toDto(Event event);

    @Mapping(target = "relatedSkills", ignore = true)
    @Mapping(target = "startDate", source = "startTime")
    @Mapping(target = "endDate", source = "endTime")
    @Mapping(target = "owner", ignore = true)//!!!source = "ownerId"
    @Mapping(target = "type", source = "eventType")
    @Mapping(target = "status", source = "eventStatus")
    Event toEntity(EventDto eventDto);

    @Mapping(target = "relatedSkills", ignore = true)
    @Mapping(target = "startDate", source = "startTime")
    @Mapping(target = "endDate", source = "endTime")
    @Mapping(target = "owner", ignore = true)//!!!source = "ownerId"
    @Mapping(target = "type", source = "eventType")
    @Mapping(target = "status", source = "eventStatus")
    void update(@MappingTarget Event event, EventDto eventDto);

    List<EventDto> toDto(List<Event> events);
    List<Event> toEntity(List<EventDto> eventsDto);
}
