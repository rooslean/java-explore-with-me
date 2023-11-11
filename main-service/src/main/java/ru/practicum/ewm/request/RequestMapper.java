package ru.practicum.ewm.request;

import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.dto.RequestStatusUpdateResultDto;

import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    public static RequestDto mapToRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .build();
    }

    public static RequestStatusUpdateResultDto mapToRequestStatusUpdateResultDto(List<Request> confirmed, List<Request> rejected) {
        return RequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmed.stream()
                        .map(RequestMapper::mapToRequestDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(rejected.stream()
                        .map(RequestMapper::mapToRequestDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
