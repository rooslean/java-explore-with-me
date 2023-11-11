package ru.practicum.ewm.compilation;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

public class CompilationMapper {
    public static CompilationDto mapToCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .title(compilation.getTitle())
                .events(events)
                .pinned(compilation.getPinned())
                .build();
    }
    public static Compilation mapToCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events)
                .build();
    }
    public static void mapToUpdateCompilation(Compilation compilation, UpdateCompilationDto updComp, List<Event> events) {
        if (updComp.getTitle() != null && !updComp.getTitle().isEmpty()) {
            compilation.setTitle(updComp.getTitle());
        }
        if (updComp.getPinned() != null) {
            compilation.setPinned(updComp.getPinned());
        }
        if (events != null) {
            compilation.setEvents(events);
        }
    }
}
