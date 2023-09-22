package com.aritra.ecs.demo.dao;

import com.aritra.ecs.demo.entity.Category;

import java.util.Collection;
import java.util.Optional;

public interface CategoryDao {
    Collection<Category> getAllCategories();
    Category getCategoryById(int id);
    void addCategory(String name);
    void updateCategoryName(int id, String name);
}
