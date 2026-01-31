package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import org.springframework.data.repository.CrudRepository;

public interface BlogEntryRepo extends CrudRepository<BlogEntry, Integer> {
}
