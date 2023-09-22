package com.aritra.ecs.demo.dao;

import com.aritra.ecs.demo.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

@Repository("users")
public class UserDaoSQLImpl implements UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            return user;
        }
    }

    @Override
    public User findByUsername(String username) {
        final String sql = "SELECT id, username, password FROM users WHERE username=?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{username}, new UserRowMapper());
        if (users.isEmpty()) return null;
        User user = users.get(0);
        return user;
    }

    @Override
    public void registerUser(String username, String password) {
        final String sql = "INSERT INTO users (username,password) VALUES (?, ?)";
        jdbcTemplate.update(sql, new Object[]{username, password});
    }

    @Override
    public Boolean checkCredentialsMatch(String username, String password) {
        final String sql = "SELECT id, username, password FROM users WHERE (username=? AND password = ?)";
        User user = jdbcTemplate.queryForObject(sql, new Object[]{username, password}, new UserRowMapper());
        if (Objects.isNull(user)) {
            return false;
        }
        return true;
    }
}
