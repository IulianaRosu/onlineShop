package com.practice.onlineShop.utils;

import com.practice.onlineShop.entities.*;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.practice.onlineShop.enums.Currencies.RON;

@Component
@RequiredArgsConstructor
public class UtilsComponent {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public static final String LOCALHOST = "http://localhost:";


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Users saveUserWithRole(Roles role) {
        Users userEntity = new Users();
        userEntity.setFirstname("adminFirstName");
        Collection<Roles> roles = new ArrayList<>();
        roles.add(role);
        userEntity.setRoles(roles);
        Address address = new Address();
        address.setCity("Bucuresti");
        address.setStreet("aWOnderfulStreet");
        address.setNumber(2);
        address.setZipcode("123");
        userEntity.setAddress(address);
        userRepository.save(userEntity);
        return userEntity;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)

    public Product generateProduct(String productCode) {
        Product product = new Product();
        product.setCode(productCode);
        product.setCurrency(RON);
        product.setPrice(100L);
        product.setStock(1);
        product.setDescription("a description");
        product.setValid(true);
        return product;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)

    public Product storeTwoProductsInDatabase(String code1, String code2) {
        Product product = generateProduct(code1);

        Product product2 = generateProduct(code2);

        ArrayList<Product> products = new ArrayList<>();
        products.add(product);
        products.add(product2);
        productRepository.saveAll(products);
        return product;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Orders saveDeliveredOrder(Users client, Product product) {
        Orders orderWithProducts = generateOrderItems(product, client);
        orderWithProducts.setDelivered(true);
        orderRepository.save(orderWithProducts);
        return orderWithProducts;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Orders saveOrder(Users client, Product product) {
        Orders orderWithProducts = generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);
        return orderWithProducts;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Orders saveCanceledAndDeliveredOrder(Users client, Product product) {
        Orders orderWithProducts = generateOrderItems(product, client);
        orderWithProducts.setCanceled(true);
        orderWithProducts.setDelivered(true);
        orderRepository.save(orderWithProducts);
        return orderWithProducts;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Orders generateOrderItems(Product product, Users user) {
        Orders order = new Orders();
        order.setUser(user);
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem orderItem = generateOrderItem(product);
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        return order;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderItem generateOrderItem(Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(1);
        orderItem.setProduct(product);
        return orderItem;
    }
}
