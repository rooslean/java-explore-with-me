package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.CommentService;
import ru.practicum.ewm.comment.CommentSort;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventFullDtoWithComments;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UpdateEventRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
public class EventController {
    private final EventService eventService;
    private final CommentService commentService;

    @GetMapping("/admin/events")
    public ResponseEntity<List<EventFullDto>> findEvents(@RequestParam(required = false) List<Long> users,
                                                         @RequestParam(required = false) List<EventState> states,
                                                         @RequestParam(required = false) List<Long> categories,
                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(eventService.getEventsByFilter(users, states, categories, rangeStart, rangeEnd, from, size), HttpStatus.OK);
    }

    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId, @RequestBody @Valid UpdateEventRequest updateEventRequest) {
        return new ResponseEntity<>(eventService.updateEvent(eventId, updateEventRequest), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventShortDto>> findEvents(@RequestParam(required = false) String text,
                                                          @RequestParam(required = false) List<Long> categories,
                                                          @RequestParam(required = false) Boolean paid,
                                                          @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                          @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                          @RequestParam(defaultValue = "0") int from,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          HttpServletRequest request) {
        return new ResponseEntity<>(eventService.getEventsByFilter(text, categories, paid,
                onlyAvailable, sort, rangeStart, rangeEnd, from, size, request.getRemoteAddr(), request.getRequestURI()), HttpStatus.OK);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventFullDtoWithComments> getEvent(@PathVariable Long id, HttpServletRequest request,
                                                             @RequestParam(defaultValue = "0") int from,
                                                             @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(eventService.getEvent(id, request.getRemoteAddr(), request.getRequestURI(), from, size), HttpStatus.OK);
    }

    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> getEventComments(@PathVariable Long eventId, @RequestParam(defaultValue = "0") int from,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "CREATED_DESC") CommentSort sort) {
        return new ResponseEntity<>(commentService.getEventComments(eventId, from, size, sort), HttpStatus.OK);
    }

}
