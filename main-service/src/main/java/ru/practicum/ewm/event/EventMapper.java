package ru.practicum.ewm.event;

import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryMapper;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequest;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserMapper;

import java.time.LocalDateTime;

public class EventMapper {
    public static EventFullDto mapToEventFullDto(Event event, Long views, Long confirmedRequests) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .paid(event.getPaid())
                .views(views)
                .createdOn(event.getCreatedOn())
                .location(EventLocation.builder()
                        .lat(event.getLatitude())
                        .lon(event.getLongitude())
                        .build())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .confirmedRequests(confirmedRequests)
                .state(event.getEventState())
                .participantLimit(event.getParticipantLimit())
                .build();
    }

    public static EventShortDto mapToEventShortDto(Event event, Long views, Long confirmedRequests) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .paid(event.getPaid())
                .views(views)
                .confirmedRequests(confirmedRequests)
                .build();
    }

    public static Event mapToEvent(User initiator, NewEventDto newEventDto, Category category) {
        return Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(initiator)
                .latitude(newEventDto.getLocation().getLat())
                .longitude(newEventDto.getLocation().getLon())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .eventState(EventState.PENDING)
                .build();
    }

    public static void mapToUpdatedEvent(Event event, UpdateEventRequest updEvent, Category category) {
        if (updEvent.getRequestModeration() != null) {
            event.setRequestModeration(updEvent.getRequestModeration());
        }
        if (updEvent.getPaid() != null) {
            event.setPaid(updEvent.getPaid());
        }
        if (updEvent.getEventDate() != null) {
            event.setEventDate(updEvent.getEventDate());
        }
        if (updEvent.getTitle() != null && !updEvent.getTitle().isEmpty()) {
            event.setTitle(updEvent.getTitle());
        }
        if (updEvent.getAnnotation() != null && !updEvent.getAnnotation().isEmpty()) {
            event.setAnnotation(updEvent.getAnnotation());
        }
        if (updEvent.getDescription() != null && !updEvent.getDescription().isEmpty()) {
            event.setDescription(updEvent.getDescription());
        }
        if (updEvent.getLocation() != null && updEvent.getLocation().getLat() != null) {
            event.setLatitude(updEvent.getLocation().getLat());
        }
        if (updEvent.getLocation() != null && updEvent.getLocation().getLon() != null) {
            event.setLongitude(updEvent.getLocation().getLon());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (updEvent.getParticipantLimit() != null && updEvent.getParticipantLimit() >= 0) {
            event.setParticipantLimit(updEvent.getParticipantLimit());
        }
        if (updEvent.getStateAction() != null) {
            switch (updEvent.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setEventState(EventState.PENDING);
                    break;
                case REJECT_EVENT:
                    event.setEventState(EventState.REJECTED);
                    break;
                case PUBLISH_EVENT:
                    event.setEventState(EventState.PUBLISHED);
                    break;
                case CANCEL_REVIEW:
                    event.setEventState(EventState.CANCELED);
                    break;
            }
        }
    }
}
