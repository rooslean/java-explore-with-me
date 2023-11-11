package ru.practicum.ewm.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequest;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.request.QRequest;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.request.RequestStatus;
import ru.practicum.ewm.request.dto.RequestCountByEventId;
import ru.practicum.ewm.stat.StatDto;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page)
                .stream()
                .collect(Collectors.toList());
        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            stats = getViews(events, true);
        } else {
            stats = Collections.emptyMap();
        }

        Map<Long, Long> confirmedRequests = requestRepository.countByUser(events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()), RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(RequestCountByEventId::getEventId, RequestCountByEventId::getCount));

        return events.stream()
                .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime now = LocalDateTime.now();
        if (newEventDto.getEventDate().isBefore(now)) {
            throw new BadRequestException("Дата события не может быть в прошлом");
        }
        if (now.plusHours(2).isAfter(newEventDto.getEventDate())) {
            throw new ConflictException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(ObjectNotFoundException::new);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(ObjectNotFoundException::new);
        Event event = EventMapper.mapToEvent(user, newEventDto, category);
        return EventMapper.mapToEventFullDto(eventRepository.save(event), 0L, 0L);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(ObjectNotFoundException::new);
        Long views = getViews(List.of(event), true)
                .getOrDefault(eventId, 0L);
        return EventMapper.mapToEventFullDto(event, views,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    public EventFullDto getEvent(Long eventId, String ip, String uri) {
        Event event = eventRepository.findByIdAndEventState(eventId, EventState.PUBLISHED)
                .orElseThrow(ObjectNotFoundException::new);
        Long views = getViews(List.of(event), true)
                .getOrDefault(eventId, 0L);
        statClient.addHit("ewm-main-service", uri, ip, LocalDateTime.now());
        return EventMapper.mapToEventFullDto(event, views,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventRequest updEvent) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException();
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(ObjectNotFoundException::new);
        if (!EventState.PENDING.equals(event.getEventState()) && !EventState.REJECTED.equals(event.getEventState())) {
            throw new ConflictException();
        }
        if (updEvent.getEventDate() != null && LocalDateTime.now().plusHours(2).isAfter(updEvent.getEventDate())) {
            throw new BadRequestException("Некорректная дата");
        }
        Category category = null;
        if (updEvent.getCategory() != null) {
            category = categoryRepository.findById(updEvent.getCategory())
                    .orElseThrow(ObjectNotFoundException::new);
        }
        EventMapper.mapToUpdatedEvent(event, updEvent, category);
        event = eventRepository.save(event);
        Long views = getViews(List.of(event), true)
                .getOrDefault(eventId, 0L);
        return EventMapper.mapToEventFullDto(event, views,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventRequest updEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        LocalDateTime now = LocalDateTime.now();
        if (updEvent.getEventDate() != null) {
            if (updEvent.getEventDate().isBefore(now)) {
                throw new BadRequestException("Нельзя изменить на прошедшую дату");
            }
            if (now.plusHours(1).isAfter(updEvent.getEventDate())) {
                throw new ConflictException();
            }
        }

        if (EventStateAction.PUBLISH_EVENT.equals(updEvent.getStateAction())
                && !event.getEventState().equals(EventState.PENDING)
                || EventStateAction.REJECT_EVENT.equals(updEvent.getStateAction())
                && event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException();
        }
        Category category = null;
        if (updEvent.getCategory() != null) {
            category = categoryRepository.findById(updEvent.getCategory())
                    .orElseThrow(ObjectNotFoundException::new);
        }
        EventMapper.mapToUpdatedEvent(event, updEvent, category);
        if (EventStateAction.PUBLISH_EVENT.equals(updEvent.getStateAction())) {
            event.setPublishedOn(LocalDateTime.now());
        }
        event = eventRepository.save(event);
        Long views = getViews(List.of(event), true)
                .getOrDefault(eventId, 0L);
        return EventMapper.mapToEventFullDto(event, views,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
    }

    @Override
    public List<EventFullDto> getEventsByFilter(List<Long> userIds, List<EventState> states, List<Long> categoryIds,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        ru.practicum.ewm.event.QEvent qEvent = ru.practicum.ewm.event.QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        if (rangeStart != null && rangeEnd != null) {
            if (!rangeStart.isBefore(rangeEnd)) {
                throw new BadRequestException("Дата начала не может быть позже даты конца");
            }
            conditions.add(qEvent.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            conditions.add(qEvent.eventDate.after(rangeStart));
        } else if (rangeEnd != null) {
            conditions.add(qEvent.eventDate.before(rangeEnd));
        }
        if (userIds != null && !userIds.isEmpty()) {
            conditions.add(qEvent.initiator.id.in(userIds));
        }
        if (states != null && !states.isEmpty()) {
            conditions.add(qEvent.eventState.in(states));
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            conditions.add(qEvent.category.id.in(categoryIds));
        }
        PageRequest page = PageRequest.of(from / size, size);
        Optional<BooleanExpression> finalCondition = conditions.stream()
                .reduce(BooleanExpression::and);
        List<Event> events = finalCondition.map(condition -> eventRepository.findAll(condition, page))
                .orElseGet(() -> eventRepository.findAll(page))
                .stream()
                .collect(Collectors.toList());

        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            stats = getViews(events, true);
        } else {
            stats = Collections.emptyMap();
        }
        Map<Long, Long> confirmedRequests = requestRepository.countByUser(events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()), RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(RequestCountByEventId::getEventId, RequestCountByEventId::getCount));

        return events.stream()
                .map(e -> EventMapper.mapToEventFullDto(e, stats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsByFilter(String text, List<Long> categoryIds, Boolean paid, boolean onlyAvailable,
                                                 EventSort sort, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 int from, int size, String ip, String uri) {
        ru.practicum.ewm.event.QEvent qEvent = ru.practicum.ewm.event.QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        if (rangeStart != null && rangeEnd != null) {
            if (!rangeStart.isBefore(rangeEnd)) {
                throw new BadRequestException("Дата начала не может быть позже даты конца");
            }
            conditions.add(qEvent.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            conditions.add(qEvent.eventDate.after(rangeStart));
        } else if (rangeEnd != null) {
            conditions.add(qEvent.eventDate.before(rangeEnd));
        } else {
            conditions.add(qEvent.eventDate.after(LocalDateTime.now()));
        }
        if (onlyAvailable) {
            QRequest qRequest = QRequest.request;

            NumberTemplate<Long> acceptedRequestsCount = Expressions.numberTemplate(Long.class,
                    "coalesce(count({0}), 0)", qRequest.id);

            BooleanExpression maxRequestsCondition = qRequest.event.eq(qEvent)
                    .and(qRequest.status.eq(RequestStatus.CONFIRMED))
                    .and(acceptedRequestsCount.lt(qEvent.participantLimit));

            conditions.add(maxRequestsCondition);
        }
        if (text != null && !text.isEmpty()) {
            conditions.add(qEvent.annotation.containsIgnoreCase(text)
                    .or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            conditions.add(qEvent.category.id.in(categoryIds));
        }
        if (paid != null) {
            if (paid) {
                conditions.add(qEvent.paid.isTrue());
            } else {
                conditions.add(qEvent.paid.isFalse());
            }
        }

        conditions.add(qEvent.eventState.eq(EventState.PUBLISHED));
        Comparator<EventShortDto> comparator = Comparator.comparing(EventShortDto::getId);
        Sort reqSort = Sort.unsorted();
        if (EventSort.VIEWS.equals(sort)) {
            comparator = Comparator.comparing(EventShortDto::getViews);
        } else if (EventSort.EVENT_DATE.equals(sort)) {
            reqSort = Sort.by("eventDate").descending();
        }
        PageRequest page = PageRequest.of(from / size, size, reqSort);
        Optional<BooleanExpression> finalCondition = conditions.stream()
                .reduce(BooleanExpression::and);
        List<Event> events = finalCondition.map(condition -> eventRepository.findAll(condition, page))
                .orElseGet(() -> eventRepository.findAll(page))
                .stream()
                .collect(Collectors.toList());


        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            stats = getViews(events, true);
        } else {
            stats = Collections.emptyMap();
        }

        statClient.addHit("ewm-main-service", uri, ip, LocalDateTime.now());


        Map<Long, Long> confirmedRequests = requestRepository.countByUser(events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()), RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(RequestCountByEventId::getEventId, RequestCountByEventId::getCount));

        List<EventShortDto> eventShortDtos = Collections.emptyList();
        if (EventSort.EVENT_DATE.equals(sort)) {
            eventShortDtos = events.stream()
                    .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L),
                            confirmedRequests.getOrDefault(e.getId(), 0L)))
                    .collect(Collectors.toList());
        } else {
            eventShortDtos = events.stream()
                    .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L),
                            confirmedRequests.getOrDefault(e.getId(), 0L)))
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }
        return eventShortDtos;
    }

    private Map<Long, Long> getViews(List<Event> events, boolean unique) {
        List<String> uris = new ArrayList<>();
        LocalDateTime minDate = events.stream()
                .peek(e -> uris.add("/events/" + e.getId()))
                .map(Event::getCreatedOn)
                .min((LocalDateTime::compareTo))
                .orElseGet(LocalDateTime::now);
        LocalDateTime maxDate = events.stream()
                .map(Event::getEventDate)
                .max((LocalDateTime::compareTo))
                .orElseGet(LocalDateTime::now);
        ResponseEntity<Object> response = statClient.getStats(minDate, maxDate, uris, unique);
        Map<Long, Long> stats = Collections.emptyMap();
        if (response.getBody() instanceof List<?>) {
            ObjectMapper objectMapper = new ObjectMapper();
            stats = ((List<?>) response.getBody()).stream()
                    .map(o -> objectMapper.convertValue(o, StatDto.class))
                    .filter(s -> s.getApp().equals("ewm-main-service"))
                    .collect(Collectors.toMap(s -> Long.valueOf(s.getUri().split("/")[2]), StatDto::getHits));
        }
        return stats;
    }
}
