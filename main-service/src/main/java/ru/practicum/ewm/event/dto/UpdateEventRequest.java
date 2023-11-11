package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.event.EventLocation;
import ru.practicum.ewm.event.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    @Length(min = 3, max = 120)
    String title;
    @Length(min = 20, max = 2000)
    String annotation;
    @Length(min = 20, max = 7000)
    String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    EventLocation location;
    Long category;
    Boolean paid;
    EventStateAction stateAction;
    Integer participantLimit;
    Boolean requestModeration;
}
