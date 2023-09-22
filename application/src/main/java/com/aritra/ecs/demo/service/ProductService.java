package com.aritra.ecs.demo.service;

import com.aritra.ecs.demo.dao.ProductDao;
import com.aritra.ecs.demo.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ProductService {
    @Autowired
    @Qualifier("products")
    private ProductDao productDao;


    public void insertProduct(String name, double price, int category_id) {
        productDao.addProduct(name, price, category_id);
    }

    public Product getProductById(int id) {
        return productDao.getProductById(id);
    }

    public void deleteProductById(int id) {
        productDao.deleteProductById(id);
    }

    public void updateProduct(int id, String name, double price, int category_id) {
        productDao.updateProduct(id, name, price, category_id);
    }

    public Collection<Product> getProductsByCategoryId(int category_id) {
        return productDao.getProductsByCategoryId(category_id);
    }
}
