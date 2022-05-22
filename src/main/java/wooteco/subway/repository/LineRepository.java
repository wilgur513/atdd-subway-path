package wooteco.subway.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.Line;

@Repository
public class LineRepository {

    public static final RowMapper<Line> ROW_MAPPER = (rs, rn) -> {
        Long newId = rs.getLong("id");
        String name = rs.getString("name");
        String color = rs.getString("color");
        int extraFare = rs.getInt("extra_fare");
        return new Line(newId, name, color, extraFare);
    };

    private final JdbcTemplate jdbcTemplate;

    public LineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Line save(Line line) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                .prepareStatement("INSERT INTO LINE(name, color, extra_fare) VALUES(?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, line.getName());
            ps.setString(2, line.getColor());
            ps.setInt(3, line.getExtraFare());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Line(id, line.getName(), line.getColor(), line.getExtraFare());
    }

    public List<Line> findAll() {
        return jdbcTemplate.query("SELECT id, name, color, extra_fare FROM LINE", ROW_MAPPER);
    }

    public Optional<Line> findById(Long id) {
        try {
            Line line = jdbcTemplate
                .queryForObject("SELECT id, name, color, extra_fare FROM LINE WHERE id = ?",
                    ROW_MAPPER, id);
            return Optional.of(line);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT id FROM LINE WHERE id = ?)", Boolean.class, id);
    }

    public boolean existByName(String name) {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT id FROM LINE WHERE name = ?)", Boolean.class, name);
    }

    public Line update(Line line) {
        jdbcTemplate.update("UPDATE LINE SET name = ?, color = ?, extra_fare = ? WHERE id = ?",
            line.getName(),
            line.getColor(), line.getExtraFare(), line.getId());
        return line;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM LINE WHERE id = ?", id);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM LINE");
    }
}
