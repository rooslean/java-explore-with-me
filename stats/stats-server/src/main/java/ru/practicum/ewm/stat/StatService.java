package ru.practicum.ewm.stat;

import ru.practicum.ewm.hit.HitDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    void create(HitDto hitDto);

    List<StatDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
