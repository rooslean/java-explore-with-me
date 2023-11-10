package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EventService eventService;

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserDto>> findAll(@RequestParam(required = false) List<Long> ids,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(userService.findAll(ids, from, size), HttpStatus.OK);
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto) {
        return new ResponseEntity<>(userService.createUser(userDto), HttpStatus.CREATED);
    }

    @DeleteMapping("admin/users/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<List<EventShortDto>> findUserEvents(@PathVariable Long userId,
                                                              @RequestParam(defaultValue = "0") int from,
                                                              @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(eventService.getUserEvents(userId, from, size), HttpStatus.OK);
    }

    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        return new ResponseEntity<>(eventService.createEvent(userId, newEventDto), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return new ResponseEntity<>(eventService.getUserEvent(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                                    @RequestBody @Valid UpdateEventRequest updateEventRequest) {
        return new ResponseEntity<>(eventService.updateEvent(userId, eventId, updateEventRequest), HttpStatus.OK);
    }
}
