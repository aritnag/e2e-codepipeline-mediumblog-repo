package com.aritra.ecs.demo;

import com.aritra.ecs.demo.dao.CategoryDao;
import com.aritra.ecs.demo.entity.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class CategoryDaoSQLImplTest {
    @Autowired
    @Qualifier("categories")
    private  CategoryDao categoryDao;

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

    @Before
    public void dropDB() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("drop_all.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void getCategoryByIdTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();
        int category_id = 1; // See test-data.sql

        jdbcTemplate = new JdbcTemplate(dataSource);
        Category category = categoryDao.getCategoryById(category_id);
        assertEquals(category.getId(), category_id);
    }

    @Test
    public void updateCategoryTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();
        int category_id = 1; // See test-data.sql

        jdbcTemplate = new JdbcTemplate(dataSource);

        String newCategoryName = "electronics";
        Category category;
        categoryDao.updateCategoryName(category_id, newCategoryName);
        category = categoryDao.getCategoryById(category_id);
        assertEquals(category.getName(), newCategoryName);
    }

    @Test
    public void addCategoryTest() {
        String categoryName = "toys";
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        categoryDao.addCategory(categoryName);


        List<Category> categories = jdbcTemplate.query(GET_ALL_CATEGORIES_SQL, new CategoryRowMapper());
        Category category = categories.get(0);

        assertEquals(categoryName, category.getName());
    }

    @Test
    public void getAllCategoriesTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .build();

        categoryDao.addCategory("toys");
        jdbcTemplate = new JdbcTemplate(dataSource);

        List<Category> categories = jdbcTemplate.query(GET_ALL_CATEGORIES_SQL, new CategoryRowMapper());
        assertEquals(1, categories.size());
    }
}
