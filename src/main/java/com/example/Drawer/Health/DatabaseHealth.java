package com.example.Drawer.Health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealth implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealth(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health()
    {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up().withDetail("message", "Database is reachable").build();
        } catch (Exception e) {
            return Health.down().withDetail("message", "Database is not reachable").build();
        }
    }
}
