package ru.practicum.ewm.compilation.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    List<EventShortDto> events;
    Boolean pinned;
    String title;
}
