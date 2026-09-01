package school.faang.user_service.controller.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.mapper.event.EventMapper;
import school.faang.user_service.service.event.EventService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEvent_ShouldReturnMappedDto() {
        Event event = new Event();
        EventDto dto = mock(EventDto.class);
        when(eventService.getEvent(1L)).thenReturn(event);
        when(eventMapper.toDto(event)).thenReturn(dto);

        EventDto result = eventController.getEvent(1L);

        assertEquals(dto, result);
    }

    @Test
    void getEvent_ShouldPropagateServiceFailure() {
        when(eventService.getEvent(anyLong())).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> eventController.getEvent(1L));
    }

    @Test
    void create_ShouldMapEntityAndReturnDto() {
        EventDto in = mock(EventDto.class);
        Event entity = new Event();
        Event created = new Event();
        EventDto out = mock(EventDto.class);
        when(eventMapper.toEntity(in)).thenReturn(entity);
        when(eventService.create(entity)).thenReturn(created);
        when(eventMapper.toDto(created)).thenReturn(out);

        EventDto result = eventController.create(in);

        assertEquals(out, result);
    }

    @Test
    void updateEvent_ShouldApplyUpdateAndReturnDto() {
        EventDto in = mock(EventDto.class);
        Event entity = new Event();
        Event updated = new Event();
        EventDto out = mock(EventDto.class);
        when(eventMapper.toEntity(in)).thenReturn(entity);
        when(eventService.updateEvent(entity)).thenReturn(updated);
        when(eventMapper.toDto(updated)).thenReturn(out);

        EventDto result = eventController.updateEvent(in);

        assertEquals(out, result);
        verify(eventMapper).update(entity, in);
    }

    @Test
    void deleteEvent_ShouldDelegateToService() {
        doNothing().when(eventService).deleteEvent(5L);

        eventController.deleteEvent(5L);

        verify(eventService).deleteEvent(5L);
    }

    @Test
    void getEventsByFilter_ShouldReturnMappedList() {
        EventFilterDto filter = new EventFilterDto();
        List<Event> events = Collections.singletonList(new Event());
        List<EventDto> dtos = Collections.singletonList(mock(EventDto.class));
        when(eventService.getEventsByFilter(filter)).thenReturn(events);
        when(eventMapper.toDto(events)).thenReturn(dtos);

        List<EventDto> result = eventController.getEventsByFilter(filter);

        assertEquals(dtos, result);
    }

    @Test
    void getOwnedEvents_ShouldReturnMappedList() {
        List<Event> events = Collections.singletonList(new Event());
        List<EventDto> dtos = Collections.singletonList(mock(EventDto.class));
        when(eventService.getOwnedEvents(2L)).thenReturn(events);
        when(eventMapper.toDto(events)).thenReturn(dtos);

        List<EventDto> result = eventController.getOwnedEvents(2L);

        assertEquals(dtos, result);
    }

    @Test
    void getParticipatedEvents_ShouldReturnMappedList() {
        List<Event> events = Collections.singletonList(new Event());
        List<EventDto> dtos = Collections.singletonList(mock(EventDto.class));
        when(eventService.getParticipatedEvents(3L)).thenReturn(events);
        when(eventMapper.toDto(events)).thenReturn(dtos);

        List<EventDto> result = eventController.getParticipatedEvents(3L);

        assertEquals(dtos, result);
    }
}
