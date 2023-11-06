package ru.practicum.ewm.stat;

import ru.practicum.ewm.hit.HitDto;

public class StatMapper {
    public static Stat mapToStat(HitDto hitDto) {
        Stat stat = Stat.builder()
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .uri(hitDto.getUri())
                .timestamp(hitDto.getTimestamp())
                .build();
        return stat;
    }

    public static HitDto mapToHitDto(Stat stat) {
        HitDto hit = HitDto.builder()
                .app(stat.getApp())
                .ip(stat.getIp())
                .uri(stat.getUri())
                .timestamp(stat.getTimestamp())
                .build();
        return hit;
    }
}
