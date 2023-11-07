package ru.practicum.ewm.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.hit.HitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatClient extends BaseClient {
    DateTimeFormatter dtf;

    @Autowired
    public StatClient(@Value("${ewm-stat-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
        this.dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public ResponseEntity<Object> addHit(String appName, String uri, String ip, LocalDateTime timestamp) {
        HitDto hitDto = HitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("start", start.format(this.dtf));
        parameters.add("end", end.format(this.dtf));
        parameters.addAll("uris", uris);
        parameters.add("unique", String.valueOf(unique));

        return get("/stats", parameters);
    }
}
