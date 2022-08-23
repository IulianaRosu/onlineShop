package com.practice.onlineShop.repositories;


import com.practice.onlineShop.entities.Product;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.practice.onlineShop.enums.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
class ProductRepositoryIntegrationTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestEntityManager testEntityManager;
    @Test
    public void findByCode_whenCOdeIsPresentInDb_shouldReturnTheProduct(){
        Product product = new Product();
        product.setCode("aProductCode");
        product.setPrice(100);
        product.setStock(1);
        product.setValid(true);
        product.setCurrency(USD);
        product.setDescription("a bad product");

        Product product2 = new Product();
        product2.setCode("aProductCode2");
        product2.setPrice(100);
        product2.setStock(1);
        product2.setValid(true);
        product2.setCurrency(USD);
        product2.setDescription("a bad product");

        testEntityManager.persist(product);
        testEntityManager.persist(product2);
        testEntityManager.flush();

        Optional<Product> productFromDb = productRepository.findByCode(product.getCode());

        assertThat(productFromDb).isPresent();
        assertThat(productFromDb.get().getCode()).isEqualTo(product.getCode());


    }

    @Test
    public void findByCode_whenCodeIsNotPresentInDb_shouldReturnEmpty(){
        Optional<Product> productFromDb = productRepository.findByCode("asd");

        assertThat(productFromDb).isNotPresent();
    }


}