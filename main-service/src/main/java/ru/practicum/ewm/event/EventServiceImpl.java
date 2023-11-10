package ru.practicum.ewm.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final StatClient statClient;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<String> uris = new ArrayList<>();
        List<Event> events = eventRepository.findByInitiatorId(userId, page)
                .stream()
                .peek(e -> uris.add("/events/" + e.getId()))
                .collect(Collectors.toList());
        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            LocalDateTime minDate = events.stream()
                    .map(Event::getCreatedOn)
                    .min((LocalDateTime::compareTo))
                    .orElseGet(LocalDateTime::now);
            stats = getViews(minDate, uris, true);
        } else {
            stats = Collections.emptyMap();
        }
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return events.stream()
                .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (LocalDateTime.now().plusHours(2).isAfter(newEventDto.getEventDate())) {
            throw new ConflictException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(ObjectNotFoundException::new);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(ObjectNotFoundException::new);
        Event event = EventMapper.mapToEvent(user, newEventDto, category);
        return EventMapper.mapToEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(ObjectNotFoundException::new);
        Long views = getViews(event.getCreatedOn(), List.of("/events/" + eventId), false)
                .getOrDefault(eventId, 0L);
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return EventMapper.mapToEventFullDto(event, views);
    }

    @Override
    public EventFullDto getEvent(Long eventId, String ip, String uri) {
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        Long views = getViews(event.getCreatedOn(), List.of("/events/" + eventId), false)
                .getOrDefault(eventId, 0L);
        statClient.addHit("ewm-main-service", uri, ip, LocalDateTime.now());
        return EventMapper.mapToEventFullDto(event, views);
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
        //TODO: возможно надо проверять чтобы StateAction был подходящим
        if (updEvent.getEventDate() != null && LocalDateTime.now().plusHours(2).isAfter(updEvent.getEventDate())) {
            throw new ConflictException();
        }
        Category category = null;
        if (updEvent.getCategory() != null) {
            category = categoryRepository.findById(updEvent.getCategory())
                    .orElseThrow(ObjectNotFoundException::new);
        }
        EventMapper.mapToUpdatedEvent(event, updEvent, category);
        event = eventRepository.save(event);
        Long views = getViews(event.getCreatedOn(), List.of("/events/" + eventId), false)
                .getOrDefault(eventId, 0L);
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return EventMapper.mapToEventFullDto(event, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventRequest updEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
            throw new ConflictException();
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
        Long views = getViews(event.getCreatedOn(), List.of("/events/" + eventId), false)
                .getOrDefault(eventId, 0L);
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return EventMapper.mapToEventFullDto(event, views);
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
        List<String> uris = new ArrayList<>();
        List<Event> events = finalCondition.map(condition -> eventRepository.findAll(condition, page))
                .orElseGet(() -> eventRepository.findAll(page))
                .stream()
                .peek(e -> uris.add("/events/" + e.getId()))
                .collect(Collectors.toList());

        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            LocalDateTime minDate = events.stream()
                    .map(Event::getCreatedOn)
                    .min((LocalDateTime::compareTo))
                    .orElseGet(LocalDateTime::now);
            stats = getViews(minDate, uris, true);
        } else {
            stats = Collections.emptyMap();
        }
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return events.stream()
                .map(e -> EventMapper.mapToEventFullDto(e, stats.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsByFilter(String text, List<Long> categoryIds, boolean paid, boolean onlyAvailable,
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
            //TODO: Добавить тут проверку на неисчерпанный лимит заявок
        }
        if (text != null && !text.isEmpty()) {
            conditions.add(qEvent.annotation.containsIgnoreCase(text)
                    .or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            conditions.add(qEvent.category.id.in(categoryIds));
        }
        if (paid) {
            conditions.add(qEvent.paid.isTrue());
        } else {
            conditions.add(qEvent.paid.isFalse());
        }
        conditions.add(qEvent.eventState.eq(EventState.PUBLISHED));
        PageRequest page = PageRequest.of(from / size, size);
        Optional<BooleanExpression> finalCondition = conditions.stream()
                .reduce(BooleanExpression::and);
        List<String> uris = new ArrayList<>();
        List<Event> events = finalCondition.map(condition -> eventRepository.findAll(condition, page))
                .orElseGet(() -> eventRepository.findAll(page))
                .stream()
                .peek(e -> uris.add("/events/" + e.getId()))
                .collect(Collectors.toList());


        Map<Long, Long> stats;
        if (!events.isEmpty()) {
            LocalDateTime minDate = events.stream()
                    .map(Event::getCreatedOn)
                    .min((LocalDateTime::compareTo))
                    .orElseGet(LocalDateTime::now);
            stats = getViews(minDate, uris, true);
        } else {
            stats = Collections.emptyMap();
        }

        statClient.addHit("ewm-main-service", uri, ip, LocalDateTime.now());

        Comparator<EventShortDto> comparator;
        if (EventSort.VIEWS.equals(sort)) {
            comparator = Comparator.comparing(EventShortDto::getViews);
        } else {
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        }
        //TODO: тут будет нужен вызов метода с подсчетом подтвержденных заявок
        return events.stream()
                .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getViews(LocalDateTime minDate, List<String> uris, boolean unique) {
        ResponseEntity<Object> response = statClient.getStats(minDate, LocalDateTime.now(), uris, unique);
        Map<Long, Long> stats = Collections.emptyMap();
        if (response.getBody() instanceof List<?>) {
            stats = ((List<?>) response.getBody()).stream()
                    .map(o -> (StatDto) o)
                    .filter(s -> s.getApp().equals("ewm-main-service"))
                    .collect(Collectors.toMap(s -> Long.valueOf(s.getUri().split("/")[3]), StatDto::getHits));
        }
        return stats;
    }
}
