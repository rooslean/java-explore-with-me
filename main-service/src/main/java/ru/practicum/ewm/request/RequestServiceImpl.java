package ru.practicum.ewm.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.dto.RequestStatusUpdateDto;
import ru.practicum.ewm.request.dto.RequestStatusUpdateResultDto;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        return requestRepository.findByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(RequestMapper::mapToRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestStatusUpdateResultDto changeUserEventRequestsStatus(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return new RequestStatusUpdateResultDto(Collections.emptyList(), Collections.emptyList());
        }
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedCount > event.getParticipantLimit()) {
            throw new ConflictException();
        }
        List<Request> requests = requestRepository.findByIdIn(requestStatusUpdateDto.getRequestIds());
        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();
        for (Request request : requests) {
            if (!RequestStatus.PENDING.equals(request.getStatus())) {
                throw new ConflictException();
            }
            if (confirmedCount <= event.getParticipantLimit() && RequestStatus.CONFIRMED.equals(requestStatusUpdateDto.getStatus())) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(request);
                confirmedCount++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(request);
            }
        }
        confirmed = requestRepository.saveAll(confirmed);
        rejected = requestRepository.saveAll(rejected);
        return RequestMapper.mapToRequestStatusUpdateResultDto(confirmed, rejected);
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException();
        }
        return requestRepository.findById(userId)
                .stream()
                .map(RequestMapper::mapToRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(ObjectNotFoundException::new);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        boolean isLimited = event.getParticipantLimit() > 0 && event.getRequestModeration();
        boolean isLimitReached = isLimited && confirmedCount == event.getParticipantLimit();
        if (event.getInitiator().getId().equals(user.getId())
                || !EventState.PUBLISHED.equals(event.getEventState())
                || isLimitReached
                || requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException();
        }
        Request request = requestRepository.save(Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(isLimited ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build());
        return RequestMapper.mapToRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(ObjectNotFoundException::new);
        request.setStatus(RequestStatus.REJECTED);
        request = requestRepository.save(request);

        return RequestMapper.mapToRequestDto(request);
    }
}
