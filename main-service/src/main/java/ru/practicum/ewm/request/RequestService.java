package ru.practicum.ewm.request;

import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.dto.RequestStatusUpdateDto;
import ru.practicum.ewm.request.dto.RequestStatusUpdateResultDto;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequestsForUserEvent(Long userId, Long eventId);

    RequestStatusUpdateResultDto changeUserEventRequestsStatus(Long userId, Long eventId,
                                                               RequestStatusUpdateDto requestStatusUpdateDto);

    List<RequestDto> getUserRequests(Long userId);

    RequestDto addRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);
}
