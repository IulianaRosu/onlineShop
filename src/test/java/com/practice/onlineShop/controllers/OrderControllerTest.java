package com.practice.onlineShop.controllers;

import com.practice.onlineShop.entities.OrderItem;
import com.practice.onlineShop.entities.Orders;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.entities.Users;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.utils.UtilsComponent;
import com.practice.onlineShop.vos.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.*;

import static com.practice.onlineShop.utils.UtilsComponent.LOCALHOST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {


    @TestConfiguration
    static class ProductControllerIntegrationTestContextConfiguration{
        @Bean
        public RestTemplate restTemplateForPatch(){
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplateForPatch;

    @Autowired
    private UtilsComponent utilsComponent;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    public void addOrder_whenOrderIsValid_shouldAssItToDb(){
        Users user = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1", "code2");

        OrderVO orderVO = createOrderVO(user, product);

        testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO ,  Void.class);

        List<Orders> ordersIterable =(List<Orders>) orderRepository.findAll();

        Optional<OrderItem> orderItemOptional = ordersIterable.stream()
                .map(order -> ((List<OrderItem>)order.getOrderItems()))
                .flatMap(List::stream)
                .filter(orderItem -> orderItem.getProduct().getId() == product.getId())
                .findFirst();

        assertThat(orderItemOptional).isPresent();

    }



    @Test
    public void addOrder_whenRequestIsMadeByAdmin_shouldThrowAnException(){
        Users user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForAdmin", "code2ForAdmin");

        OrderVO orderVO = createOrderVO(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order",orderVO, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a exeuta aceasta operatiune");
    }
    @Test
    public void addOrder_whenRequestIsMadeByExpeditor_shouldThrowAnException(){
        Users user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditor", "code2ForExpeditor");

        OrderVO orderVO = createOrderVO(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order",orderVO, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a exeuta aceasta operatiune");

    }
    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldDeliverItByExpeditor(){
        Users expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliver", "code2ForExpeditorForDeliver");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, expeditor);

        orderRepository.save(orderWithProducts);


        restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        Orders orderFromDb  = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat( orderFromDb.isDelivered()).isTrue();
    }



    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldNOTDeliverItByAdmin(){
        Users adminAsExpeditor = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliverWhenAdmin", "code2ForExpeditorForDeliverWhenAdmin");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, adminAsExpeditor);

        orderRepository.save(orderWithProducts);

try {
    restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + adminAsExpeditor.getId(),
            HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
}catch (RestClientException exception){
    System.out.println(exception.getMessage());
    assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

}

    }
    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldNOTDeliverItByClient(){
        Users clientAsExpeditor = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliverWhenClient", "code2ForExpeditorForDeliverWhenClient");


        Orders orderWithProducts = utilsComponent.generateOrderItems(product, clientAsExpeditor);

        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + clientAsExpeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
        }catch (RestClientException exception){
            System.out.println(exception.getMessage());
            assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

        }
    }
    @Test
    public void deliver_whenHavingAnOrderWhichIsCanceled_shouldThrowAnException(){
        Users expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder", "code2ForExpeditorForCanceledOrder");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, expeditor);
        orderWithProducts.setCanceled(true);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
        }catch (RestClientException exception){
            System.out.println(exception.getMessage());
            assertThat(exception.getMessage()).isEqualTo("400 : \"Comanda a fost anulata\"");

        }
    }
    @Test
    public void cancel_whenValidOrder_shouldCancelIt(){

        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder1", "code2ForExpeditorForCanceledOrder1");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderRepository.save(orderWithProducts);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                HttpMethod.PATCH, HttpEntity.EMPTY, String.class);

        Orders orderFromDb  = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat( orderFromDb.isCanceled()).isTrue();
    }
    @Test
    public void cancel_whenOrderIsAlreadySent_shouldThrowAnException(){

        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder2", "code2ForExpeditorForCanceledOrder2");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderWithProducts.setDelivered(true);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
        }catch (RestClientException exception){
            System.out.println(exception.getMessage());
            assertThat(exception.getMessage()).isEqualTo("400 : \"Comanda a fost deja expediata\"");

        }

    }
    @Test
    public void cancel_whenUserIsAdmin_shouldThrowAnException(){
        Users admin = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder30", "code2ForExpeditorForCanceledOrder30");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, admin);

        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + admin.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
        }catch (RestClientException exception){
            System.out.println(exception.getMessage());
            assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

        }
    }
    @Test
    public void cancel_whenUserIsExpeditor_shouldThrowAnException(){
        Users expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder3", "code2ForExpeditorForCanceledOrder3");
        Orders orderWithProducts = utilsComponent.generateOrderItems(product, expeditor);

        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);
        }catch (RestClientException exception){
            System.out.println(exception.getMessage());
            assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

        }
    }
    @Test
    @Transactional
    public void return_whenOrderValid_shouldReturnIt(){
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForReturn", "code2ForExpeditorForReturn");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrder(client, product);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        Orders orderFromDb = orderRepository.findById(orderWithProducts.getId()).get();
        assertThat(orderFromDb.isReturned()).isTrue();
        assertThat(orderFromDb.getOrderItems().get(0).getProduct().getStock()).isEqualTo(product.getStock() + orderWithProducts.getOrderItems().get(0).getQuantity());
    }



    @Test
    public void return_whenOrderisNotDelivered_shouldThrowAnException(){
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForReturn33", "code2ForExpeditorForReturn33");
        Orders orderWithProducts = utilsComponent.saveOrder(client, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);
        }catch (RestClientException exception){
            assertThat(exception.getMessage()).isEqualTo("400 : \"Comanda nu a fost livrata\"");

        }

    }
    @Test
    public void return_whenOrderisCanceled_shouldThrowAnException(){
        Users client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForReturn333", "code2ForExpeditorForReturn333");
        Orders orderWithProducts = utilsComponent.saveCanceledAndDeliveredOrder(client, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);
        }catch (RestClientException exception){
            assertThat(exception.getMessage()).isEqualTo("400 : \"Comanda a fost anulata\"");

        }
    }
    @Test
    public void return_whenUserIsAdmin_shouldThrowAnException(){
        Users admin = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForReturn333w", "code2ForExpeditorForReturn333w");
        Orders orderWithProducts = utilsComponent.saveCanceledAndDeliveredOrder(admin, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + admin.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);
        }catch (RestClientException exception){
            assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

        }
    }
    @Test
    public void return_whenUserIsExpeditor_shouldThrowAnException(){
        Users expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForReturn333e", "code2ForExpeditorForReturn333e");
        Orders orderWithProducts = utilsComponent.saveCanceledAndDeliveredOrder(expeditor, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);
        }catch (RestClientException exception){
            assertThat(exception.getMessage()).isEqualTo("400 : \"Utilizatorul nu are permisiunea de a exeuta aceasta operatiune\"");

        }
    }


    private OrderVO createOrderVO(Users user, Product product) {
        OrderVO orderVO = new OrderVO();
        orderVO.setUserId((int) user.getId());

        Map<Integer,Integer> orderMap = new HashMap<>();
        orderMap.put((int) product.getId(), 1);
        orderVO.setProductsIdsToQuantity(orderMap);
        return orderVO;
    }



}