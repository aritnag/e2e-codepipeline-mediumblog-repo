package com.aritra.ecs.demo.dao;

import com.aritra.ecs.demo.entity.Category;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository("categories")
public class CategoryDaoSQLImpl implements CategoryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    final String GET_ALL_CATEGORIES_SQL = "SELECT id, name FROM categories";

    private static class CategoryRowMapper implements RowMapper<Category> {

        @Override
        public Category mapRow(ResultSet resultSet, int i) throws SQLException {
            Category category = new Category();
            category.setId(resultSet.getInt("id"));
            category.setName(resultSet.getString("name"));
            return category;
        }
    }

    @Override
    public void addCategory(String name) {
        final String sql = "INSERT INTO categories (name) VALUES (?)";
        jdbcTemplate.update(sql, new Object[]{name});
    }

    @Override
    public Collection<Category> getAllCategories() {
        List<Category> categories = jdbcTemplate.query(GET_ALL_CATEGORIES_SQL, new CategoryRowMapper());
        return categories;
    }

    @Override
    public void updateCategoryName(int id, String name) {
        jdbcTemplate.update("UPDATE categories SET name = ? WHERE id = ?", name, id);
    }

    @Override
    public Category getCategoryById(int id) {
        final String sql = "SELECT id, name FROM categories WHERE id=?";
        List<Category> categories = jdbcTemplate.query(sql, new Object[]{id}, new CategoryRowMapper());
        System.out.println(categories.size());
        if (categories.isEmpty()) return null;
        Category category = categories.get(0);
        return category;
    }
}
