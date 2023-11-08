package ru.practicum.ewm.stat;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.hit.HitDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    public void create(HitDto hitDto) {
        Stat newStat = StatMapper.mapToStat(hitDto);
        statRepository.create(newStat);
    }

    @Override
    public List<StatDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("Дата начала позже даты конца периода");
        }
        return statRepository.findStatsByStartDateAndEndDate(start, end, uris, unique);
    }
}
