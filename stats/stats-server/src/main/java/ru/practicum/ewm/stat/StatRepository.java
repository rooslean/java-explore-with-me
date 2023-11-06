package ru.practicum.ewm.stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository {
    Stat create(Stat stat);

    List<StatDto> findStatsByStartDateAndEndDate(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
