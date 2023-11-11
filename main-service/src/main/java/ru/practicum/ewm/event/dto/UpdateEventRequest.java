package ru.practicum.ewm.event.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.EventLocation;
import ru.practicum.ewm.event.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    String title;
    String annotation;
    String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    EventLocation location;
    Long category;
    Boolean paid;
    EventStateAction stateAction;
    Integer participantLimit;
    Boolean requestModeration;
}
