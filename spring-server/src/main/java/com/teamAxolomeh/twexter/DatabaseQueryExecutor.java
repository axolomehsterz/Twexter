package com.teamAxolomeh.twexter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class DatabaseQueryExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryExecutor.class);
  private final JdbcTemplate jdbcTemplate;

  public DatabaseQueryExecutor(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Map<String, Object>> query(String sql, Object[] params) {
    logger.info("Executed query\n{}", sql);
    if (params != null && params.length > 0) {
      logger.info("with params: {}", (Object) params);
    }
    return jdbcTemplate.queryForList(sql, params);
  }
}
