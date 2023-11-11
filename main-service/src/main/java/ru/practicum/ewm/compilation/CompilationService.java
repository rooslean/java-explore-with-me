package ru.practicum.ewm.compilation;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getCompilations(int from, int size, Boolean pinned);
    CompilationDto getCompilation(Long compilationId);
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);
    void deleteCompilation(Long compilationId);
    CompilationDto updateCompilation(Long compilationId, UpdateCompilationDto updateCompilationDto);
}
