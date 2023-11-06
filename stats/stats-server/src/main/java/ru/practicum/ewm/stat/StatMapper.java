package ru.practicum.ewm.stat;

import ru.practicum.ewm.hit.HitDto;

public class StatMapper {
    public static Stat mapToStat(HitDto hitDto) {
        return Stat.builder()
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .uri(hitDto.getUri())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
