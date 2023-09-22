package com.aritra.ecs.demo;

import com.aritra.ecs.demo.dao.UserDao;
import com.aritra.ecs.demo.entity.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class UserDaoSQLImplTest {
    @Autowired
    @Qualifier("users")
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void dropDB() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("drop_all.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void findByUsernameTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);

        String username = "ozer";
        String password = "123456";

        userDao.registerUser(username, password);
        User user = userDao.findByUsername(username);
        assertEquals(user.getUsername(), username);
    }

    @Test
    public void registerUserTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);

        String username = "ozer";
        String password = "123456";

        userDao.registerUser(username, password);
        User user = userDao.findByUsername(username);
        assertEquals(user.getUsername(), username);
    }

    @Test
    public void checkCredentialsMatchTest() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        final String username = "ozercevikaslan";
        final String password = "123456";
        userDao.registerUser(username, password);
        Boolean credentialsMatch = userDao.checkCredentialsMatch(username, password);
        assertEquals(credentialsMatch, true);
    }
}
