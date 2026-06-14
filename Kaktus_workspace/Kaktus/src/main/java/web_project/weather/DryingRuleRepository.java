package web_project.weather;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DryingRuleRepository {
    private final JdbcTemplate jdbcTemplate;

    public DryingRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

 // добавить логгирование
    public int getWaterLoss(int temp, int humidity) {
        String sql = "SELECT drying_value FROM drying_rules WHERE ? BETWEEN temp_min AND temp_max AND ? BETWEEN humidity_min AND humidity_max";
        return jdbcTemplate.queryForObject(sql, Integer.class, temp, humidity);
    }
}