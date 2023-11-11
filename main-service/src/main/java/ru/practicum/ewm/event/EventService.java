package ru.practicum.ewm.event;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto getEvent(Long eventId, String ip, String uri);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    EventFullDto updateEvent(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> getEventsByFilter(List<Long> userIds, List<EventState> states,
                                         List<Long> categoryIds, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, int from, int size);

    List<EventShortDto> getEventsByFilter(String text, List<Long> categoryIds, Boolean paid, boolean onlyAvailable,
                                         EventSort sort, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from,
                                         int size, String ip, String uri);
}
