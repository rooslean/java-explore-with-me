package ru.practicum.ewm.stat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Stat {
    Long id;
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("app", app);
        values.put("uri", uri);
        values.put("ip", ip);
        values.put("created", timestamp);
        return values;
    }
}
