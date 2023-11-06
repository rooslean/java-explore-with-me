package ru.practicum.ewm.stat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatRepositoryImpl implements StatRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;


    @Autowired
    public StatRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Stat create(Stat stat) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("stats")
                .usingGeneratedKeyColumns("id");
        long id = insert.executeAndReturnKey(stat.toMap()).longValue();
        stat.setId(id);
        return stat;
    }

    @Override
    public List<StatDto> findStatsByStartDateAndEndDate(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("app, uri, ");
        query.append("COUNT(").append(unique ? "DISTINCT ip" : "uri").append(") AS hits ");
        query.append("FROM stats ");
        query.append("WHERE created BETWEEN :start AND :end");
        if (uris != null && !uris.isEmpty()) {
            query.append(" AND uri IN (:uris)");
        }
        query.append(" GROUP BY uri");
        query.append(" ORDER BY hits DESC");

        query.append(";");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end)
                .addValue("uris", uris);

        return namedJdbcTemplate.query(query.toString(), parameters, this::mapRowToStatDto);
    }

    private StatDto mapRowToStatDto(ResultSet resultSet, int rowNum) throws SQLException {
        return StatDto.builder()
                .app(resultSet.getString("app"))
                .uri(resultSet.getString("uri"))
                .hits(resultSet.getLong("hits"))
                .build();
    }
}
