package com.aritra.ecs.demo;

import com.aritra.ecs.demo.dao.ProductDao;
import com.aritra.ecs.demo.entity.Product;
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
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class ProductDaoSQLImplTest {
    @Autowired
    @Qualifier("products")
    private ProductDao productDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    final String GET_ALL_PRODUCTS_SQL = "SELECT id, name, price, category_id FROM products";

    private static class ProductRowMapper implements RowMapper<Product> {

        @Override
        public Product mapRow(ResultSet resultSet, int i) throws SQLException {
            Product product = new Product();
            product.setId(resultSet.getInt("id"));
            product.setName(resultSet.getString("name"));
            product.setCategoryId(resultSet.getInt("category_id"));
            product.setPrice(resultSet.getDouble("price"));
            return product;
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
    public void getProductByIdTest() {
        String productName = "iPhone";
        double price = 10.0;
        int category_id = 1; // See test-data.sql if you have concerns over FK.

        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        productDao.addProduct(productName, price, category_id);
        Collection<Product> products = productDao.getProductsByCategoryId(1);
        Product product = products.iterator().next();
        int productId = product.getId();

        Product sameProduct = productDao.getProductById(productId);
        assertEquals(sameProduct.getName(), product.getName());
    }

    @Test
    public void updateProductTest() {
        String productName = "iPhone";
        double price = 10.0;
        int category_id = 1; // See test-data.sql if you have concerns over FK.

        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        productDao.addProduct(productName, price, category_id);
        Collection<Product> products = productDao.getProductsByCategoryId(1);
        Product product = products.iterator().next();
        int productId = product.getId();

        String newProductName = "iPhone XS";
        double newProductPrice = 11.0;

        productDao.updateProduct(productId, newProductName, newProductPrice, 1);
        products = productDao.getProductsByCategoryId(1);
        product = products.iterator().next();
        assertEquals(product.getPrice(), newProductPrice, 0.8);
    }

    @Test
    public void getProductsByCategoryIdTest() {
        String productName = "iPhone";
        double price = 10.0;
        int category_id = 1; // See test-data.sql if you have concerns over FK.

        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        Collection<Product> products = productDao.getProductsByCategoryId(1);
        assertEquals(products.isEmpty(), true);

        productDao.addProduct(productName, price, category_id);
        products = productDao.getProductsByCategoryId(1);
        assertEquals(products.isEmpty(), false);
    }

    @Test
    public void addProductTest() {
        String productName = "iPhone";
        double price = 10.0;
        int category_id = 1; // See test-data.sql if you have concerns over FK.

        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        productDao.addProduct(productName, price, category_id);

        List<Product> products = jdbcTemplate.query(GET_ALL_PRODUCTS_SQL, new ProductRowMapper());
        Product product = products.get(0);
        assertEquals(productName, product.getName());
    }

    @Test
    public void deleteProductTest() {
        String productName = "iPhone";
        double price = 10.0;
        int category_id = 1; // See test-data.sql if you have concerns over FK.

        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:/schema.sql")
                .addScript("test-data.sql")
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
        productDao.addProduct(productName, price, category_id);

        List<Product> products = jdbcTemplate.query(GET_ALL_PRODUCTS_SQL, new ProductRowMapper());
        Product product = products.get(0);

        productDao.deleteProductById(product.getId());

        products = jdbcTemplate.query(GET_ALL_PRODUCTS_SQL, new ProductRowMapper());

        assertEquals(true, products.isEmpty());
    }
}
