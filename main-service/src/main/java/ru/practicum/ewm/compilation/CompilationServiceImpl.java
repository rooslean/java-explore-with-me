package ru.practicum.ewm.compilation;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventMapper;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.request.RequestStatus;
import ru.practicum.ewm.request.dto.RequestCountByEventId;
import ru.practicum.ewm.stat.StatDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final StatClient statClient;

    @Override
    public List<CompilationDto> getCompilations(int from, int size, Boolean pinned) {
        ru.practicum.ewm.compilation.QCompilation qCompilation = ru.practicum.ewm.compilation.QCompilation.compilation;
        Optional<BooleanExpression> cond = Optional.empty();
        if (pinned != null) {
            if (pinned) {
                cond = Optional.of(qCompilation.pinned.isTrue());
            } else {
                cond = Optional.of(qCompilation.pinned.isFalse());
            }
        }
        PageRequest page = PageRequest.of(from / size, size);
        Page<Compilation> compilations = cond.map(booleanExpression -> compilationRepository
                        .findAll(booleanExpression, page)).
                orElseGet(() -> compilationRepository.findAll(page));

        List<CompilationDto> compilationDtos = new ArrayList<>();
        for (Compilation comp : compilations.getContent()) {
            List<EventShortDto> events = getEventShortDtos(comp);
            compilationDtos.add(CompilationMapper.mapToCompilationDto(comp, events));
        }
        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilation(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(ObjectNotFoundException::new);
        return CompilationMapper.mapToCompilationDto(compilation, getEventShortDtos(compilation));
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto, events);
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.mapToCompilationDto(compilation, getEventShortDtos(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(ObjectNotFoundException::new);
        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(ObjectNotFoundException::new);
        List<Event> events = null;
        if (updateCompilationDto.getEvents() != null) {
            events = eventRepository.findAllById(updateCompilationDto.getEvents());
        }
        CompilationMapper.mapToUpdateCompilation(compilation, updateCompilationDto, events);

        return CompilationMapper.mapToCompilationDto(compilation, getEventShortDtos(compilation));

    }

    private Map<Long, Long> getViews(LocalDateTime minDate, List<String> uris, boolean unique) {
        ResponseEntity<Object> response = statClient.getStats(minDate, LocalDateTime.now(), uris, unique);
        Map<Long, Long> stats = Collections.emptyMap();
        if (response.getBody() instanceof List<?>) {
            stats = ((List<?>) response.getBody()).stream()
                    .map(o -> (StatDto) o)
                    .filter(s -> s.getApp().equals("ewm-main-service"))
                    .collect(Collectors.toMap(s -> Long.valueOf(s.getUri().split("/")[3]), StatDto::getHits));
        }
        return stats;
    }

    private List<EventShortDto> getEventShortDtos(Compilation comp) {
        Map<Long, Long> stats;
        if (!comp.getEvents().isEmpty()) {
            List<String> uris = new ArrayList<>();
            LocalDateTime minDate = comp.getEvents().stream()
                    .peek(e -> uris.add("/events/" + e.getId()))
                    .map(Event::getCreatedOn)
                    .min((LocalDateTime::compareTo))
                    .orElseGet(LocalDateTime::now);
            stats = getViews(minDate, uris, true);
        } else {
            stats = Collections.emptyMap();
        }
        Map<Long, Long> confirmedRequests = requestRepository.countByUser(comp.getEvents().stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()), RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(RequestCountByEventId::getEventId, RequestCountByEventId::getCount));
        return comp.getEvents().stream()
                .map(e -> EventMapper.mapToEventShortDto(e, stats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }
}
