package com.aritra.ecs.demo.entity;

public class Product {
    private int id;
    private int category_id;
    private double price;
    private String name;


    public Product() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return category_id;
    }

    public void setCategoryId(int category_id) {
        this.category_id = category_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice (double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}

