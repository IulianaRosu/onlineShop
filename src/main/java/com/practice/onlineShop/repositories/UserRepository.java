package com.practice.onlineShop.repositories;

import com.practice.onlineShop.entities.Users;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<Users, Long> {
}
