package com.webdev.bloggingsystem.blog;

public class SimpleCategoryDto {
    private int id;
    private String name;

    public SimpleCategoryDto() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SimpleCategoryDto{" +
                "id= " + id +
                ", name= " + name +
                '}';
    }
}
