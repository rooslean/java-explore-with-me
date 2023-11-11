package ru.practicum.ewm.compilation;

import lombok.AllArgsConstructor;
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
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;

    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getCompilations(@RequestParam(defaultValue = "0") int from,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) Boolean pinned) {
        return new ResponseEntity<>(compilationService.getCompilations(from, size, pinned), HttpStatus.OK);
    }

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable Long compId) {
        return new ResponseEntity<>(compilationService.getCompilation(compId), HttpStatus.OK);
    }

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return new ResponseEntity<>(compilationService.addCompilation(newCompilationDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public ResponseEntity<Object> deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable Long compId,
                                                            @RequestBody @Valid UpdateCompilationDto updateCompilationDto) {
        return new ResponseEntity<>(compilationService.updateCompilation(compId, updateCompilationDto), HttpStatus.OK);
    }
}
