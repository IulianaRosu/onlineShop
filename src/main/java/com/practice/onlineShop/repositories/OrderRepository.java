package com.practice.onlineShop.repositories;

import com.practice.onlineShop.entities.Orders;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface OrderRepository extends CrudRepository<Orders, Long> {


}
