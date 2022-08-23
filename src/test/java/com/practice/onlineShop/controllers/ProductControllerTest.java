package com.practice.onlineShop.controllers;

import com.practice.onlineShop.entities.Address;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.entities.Users;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.repositories.UserRepository;
import com.practice.onlineShop.vos.ProductVO;
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
import org.springframework.web.client.RestTemplate;
import com.practice.onlineShop.utils.UtilsComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.practice.onlineShop.enums.Currencies.EUR;
import static com.practice.onlineShop.enums.Currencies.RON;
import static com.practice.onlineShop.utils.UtilsComponent.LOCALHOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIntegrationTest {

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
    private ProductController productController;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplateForPatch;

    @Autowired
    private UtilsComponent utilsComponent;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void  contextLoads(){
        assertThat(productController).isNotNull();
    }
    @Test
    public void addProduct_whenUserIsAdmin_shouldStoreTheProduct(){
        productRepository.deleteAll();
        Users userEntity = new Users();
        userEntity.setFirstname("adminFirstName");
        Collection<Roles> roles = new ArrayList<>();
        roles.add(Roles.ADMIN);
        userEntity.setRoles(roles);
        Address address = new Address();
        address.setCity("Bucuresti");
        address.setStreet("aWOnderfulStreet");
        address.setNumber(2);
        address.setZipcode("123");
        userEntity.setAddress(address);
        userRepository.save(userEntity);

        ProductVO productVO = new ProductVO();
        productVO.setCode("aProductCode123");
        productVO.setPrice(100);
        productVO.setCurrency(RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        testRestTemplate.postForEntity(LOCALHOST + port + "/product/" + userEntity.getId(), productVO, Void.class);

        Iterable<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);

        Product product = products.iterator().next();

        assertThat(product.getCode()).isEqualTo(productVO.getCode());
    }



    @Test
    public void addProduct_whenUserIsNotInDb_shouldThrowInvalidCustomerIdException(){
        ProductVO productVO = new ProductVO();
        productVO.setCode("aProductCode");
        productVO.setPrice(100);
        productVO.setCurrency(RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        ResponseEntity<String> response = testRestTemplate.postForEntity(LOCALHOST + port + "/product/123", productVO, String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Comanda dumneavoastra nu este asignata unui user valid");


    }

    @Test
    public void addProduct_whenUserIsNOTAdmin_shouldThrowInvalidOperationException(){
        Users userEntity = new Users();
        userEntity.setFirstname("adminFirstName");
        Collection<Roles> roles = new ArrayList<>();
        roles.add(Roles.CLIENT);
        userEntity.setRoles(roles);
        Address address = new Address();
        address.setCity("Bucuresti");
        address.setStreet("aWOnderfulStreet");
        address.setNumber(2);
        address.setZipcode("123");
        userEntity.setAddress(address);
        userRepository.save(userEntity);

        ProductVO productVO = new ProductVO();
        productVO.setCode("aProductCode");
        productVO.setPrice(100);
        productVO.setCurrency(RON);
        productVO.setStock(12);
        productVO.setDescription("A product description");
        productVO.setValid(true);

        ResponseEntity<String> response = testRestTemplate.postForEntity(LOCALHOST + port + "/product/" + userEntity.getId(), productVO, String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a exeuta aceasta operatiune");
    }

    @Test
    public void getProductByCode_whenCodeIsPresentInDb_shouldReturnTheProduct(){
        Product product = utilsComponent.storeTwoProductsInDatabase("aWonderfulCode", "aWonderfulCode2");


        ProductVO productResponse = testRestTemplate.getForObject(LOCALHOST + port + "/product/" + product.getCode(), ProductVO.class);
        assertThat(productResponse.getCode()).isEqualTo(product.getCode());

    }




    @Test
    public void getProductByCode_whenProductCodeIsNotPresent_shouldReturnErrorMessage(){
        String response = testRestTemplate.getForObject(LOCALHOST + port + "/product/123", String.class);
        assertThat(response).isEqualTo("Codul produsului trimis este invalid!");
    }
    @Test
    public void getProducts(){
        productRepository.deleteAll();
        utilsComponent.storeTwoProductsInDatabase("aWonderfulCode500", "aWonderfulCode200");
        ProductVO[] products = testRestTemplate.getForObject(LOCALHOST + port+"/product", ProductVO[].class );

        assertThat(products).hasSize(2);
        assertThat(products[0].getCode()).contains("aWonderfulCode500");
        assertThat(products[1].getCode()).contains("aWonderfulCode200");
    }
    @Test
    public void updateProductwhenUserIsAdmin_shouldUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct");
        productRepository.save(product);

        Users user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        ProductVO productVO = new ProductVO();
        productVO.setCode(product.getCode());
        productVO.setCurrency(EUR);
        productVO.setPrice(200L);
        productVO.setStock(200);
        productVO.setDescription("ae description");
        productVO.setValid(true);
        System.out.println(productVO.getDescription());

        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVO);

        Optional<Product> updatedProduct = productRepository.findByCode(productVO.getCode());
        System.out.println(updatedProduct.get().getDescription());

        assertThat(updatedProduct.get().getDescription()).isEqualTo(productVO.getDescription());
        assertThat(updatedProduct.get().getCurrency()).isEqualTo(productVO.getCurrency());
        assertThat(updatedProduct.get().getPrice()).isEqualTo(productVO.getPrice());
        assertThat(updatedProduct.get().getStock()).isEqualTo(productVO.getStock());
        assertThat(updatedProduct.get().isValid()).isEqualTo(productVO.isValid());
    }
    @Test
    public void updateProductwhenUserIsClient_shouldNOTUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct3444");
        productRepository.save(product);

        Users user = utilsComponent.saveUserWithRole(Roles.CLIENT);
        ProductVO productVO = new ProductVO();
        productVO.setCode(product.getCode());
        productVO.setCurrency(EUR);
        productVO.setPrice(200L);
        productVO.setStock(200);
        productVO.setDescription("ae description");
        productVO.setValid(true);
        System.out.println(productVO.getDescription());

        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVO);

        Optional<Product> updatedProduct = productRepository.findByCode(productVO.getCode());
        System.out.println(updatedProduct.get().getDescription());

        assertThat(updatedProduct.get().getDescription()).isEqualTo(product.getDescription());
        assertThat(updatedProduct.get().getCurrency()).isEqualTo(product.getCurrency());
        assertThat(updatedProduct.get().getPrice()).isEqualTo(product.getPrice());
        assertThat(updatedProduct.get().getStock()).isEqualTo(product.getStock());
        assertThat(updatedProduct.get().isValid()).isEqualTo(product.isValid());
    }
    @Test
    public void updateProduct_whenUserIsEditor_shouldUpdateTheProduct(){
        Product product = utilsComponent.generateProduct("aProduct100");
        productRepository.save(product);

        Users user = utilsComponent.saveUserWithRole(Roles.EDITOR);
        ProductVO productVO = new ProductVO();
        productVO.setCode(product.getCode());
        productVO.setCurrency(EUR);
        productVO.setPrice(200L);
        productVO.setStock(200);
        productVO.setDescription("ae description");
        productVO.setValid(true);
        System.out.println(productVO.getDescription());

        testRestTemplate.put(LOCALHOST + port + "/product/" + user.getId(), productVO);

        Optional<Product> updatedProduct = productRepository.findByCode(productVO.getCode());
        System.out.println(updatedProduct.get().getDescription());

        assertThat(updatedProduct.get().getDescription()).isEqualTo(productVO.getDescription());
        assertThat(updatedProduct.get().getCurrency()).isEqualTo(productVO.getCurrency());
        assertThat(updatedProduct.get().getPrice()).isEqualTo(productVO.getPrice());
        assertThat(updatedProduct.get().getStock()).isEqualTo(productVO.getStock());
        assertThat(updatedProduct.get().isValid()).isEqualTo(productVO.isValid());
    }
    @Test
    public void deleteProduct_whenUserIsAdmin_shouldDeleteTheProduct(){
        Product product = utilsComponent.generateProduct("aProductForDelete");
        productRepository.save(product);

        testRestTemplate.delete(LOCALHOST + port + "/product/" + product.getCode() + "/1");

        assertThat(productRepository.findByCode(product.getCode())).isNotPresent();
    }
    @Test
    public void deleteProduct_whenUserIsAdmin_shouldNOTDeleteTheProduct(){
        Product product = utilsComponent.generateProduct("aProductForDelete");
        productRepository.save(product);

        testRestTemplate.delete(LOCALHOST + port + "/product/" + product.getCode() + "/2");

        assertThat(productRepository.findByCode(product.getCode())).isPresent();
    }
    @Test
    public void addStock_whenAddingStockToAnItemByAdmin_shouldBeSavedInDB(){
        Product product = utilsComponent.generateProduct("aProductForAddingStock");
        productRepository.save(product);

        Users user = utilsComponent.saveUserWithRole(Roles.ADMIN);

        restTemplateForPatch.exchange(LOCALHOST + port + "/product/" + product.getCode() + "/3/" + user.getId(), HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        Product productFromDb = productRepository.findByCode(product.getCode()).get();
        assertThat(productFromDb.getStock()).isEqualTo(4);

    }



}