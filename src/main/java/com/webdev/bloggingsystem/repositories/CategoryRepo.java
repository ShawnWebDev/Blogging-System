package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepo extends CrudRepository<Category, Integer> {
}
