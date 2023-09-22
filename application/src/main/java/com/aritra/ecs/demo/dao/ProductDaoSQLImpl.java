package com.aritra.ecs.demo.dao;

import com.aritra.ecs.demo.entity.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository("products")
public class ProductDaoSQLImpl implements ProductDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Override
    public Product getProductById(int id) {
        final String sql = "SELECT id, name, category_id, price FROM products WHERE id=?";
        List<Product> products = jdbcTemplate.query(sql, new Object[] { id }, new ProductRowMapper());
        if(products.isEmpty()) return null;
        Product product = products.get(0);
        return product;
    }

    @Override
    public void updateProduct(int id, String name, double price, int category_id) {
        jdbcTemplate.update("UPDATE products SET name = ?, price = ?, category_id = ? WHERE id = ?", name, price, category_id, id);
    }

    @Override
    public void deleteProductById(int id) {
        jdbcTemplate.execute("DELETE FROM products WHERE id ="+id);
    }

    @Override
    public void addProduct(String name, double price, int category_id) {
        final String sql = "INSERT INTO products (name, price, category_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{ name, price, category_id });
    }

    @Override
    public Collection<Product> getProductsByCategoryId(int category_id) {
        final String sql = "SELECT id, name, category_id, price FROM products WHERE category_id=?";
        List<Product> products = jdbcTemplate.query(sql, new Object[] { category_id }, new ProductRowMapper());
        return products;
    }
}
