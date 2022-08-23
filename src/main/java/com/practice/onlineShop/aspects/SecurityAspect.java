package com.practice.onlineShop.aspects;

import com.practice.onlineShop.entities.Users;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.exceptions.InvalidCustomerIdException;
import com.practice.onlineShop.exceptions.InvalidOperationException;
import com.practice.onlineShop.repositories.UserRepository;
import com.practice.onlineShop.vos.OrderVO;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.jws.soap.SOAPBinding;
import java.util.Collection;
import java.util.Optional;

import static com.practice.onlineShop.enums.Roles.*;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {
    private final UserRepository userRepository;

    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.addProduct(..))")
    public void addProduct(){

    }
    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.updateProduct(..))")
    public void updateProduct(){

    }
    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.deleteProduct(..))")
    public void deleteProduct(){

    }
    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.addOrder(..))")
    public void addOrderPointCut(){

    }
    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.deliver(..))")
    public void deliverPointCut(){

    }@Pointcut("execution(* com.practice.onlineShop.services.OrderService.cancelOrder(..))")
    public void cancelOrderPointCut(){

    }@Pointcut("execution(* com.practice.onlineShop.services.OrderService.returnOrder(..))")
    public void returnOrderPointCut(){

    }
    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.addStock(..))")
    public void addStockPointCut(){

    }


    @Before("com.practice.onlineShop.aspects.SecurityAspect.addProduct()")
    public void checkSecurityBeforeAddingProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
        throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToAddProduct(user.getRoles())){
        throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }
    @Before("com.practice.onlineShop.aspects.SecurityAspect.updateProduct()")
    public void checkSecurityBeforeUpdatingProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToUpdateProduct(user.getRoles())){
            throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }
    @Before("com.practice.onlineShop.aspects.SecurityAspect.addStockPointCut()")
    public void checkSecurityBeforeAddingStock(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[2];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToAddStock(user.getRoles())){
            throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }



    @Before("com.practice.onlineShop.aspects.SecurityAspect.deleteProduct()")
    public void checkSecurityBeforeDeletingProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToDeleteProduct(user.getRoles())){
            throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.addOrderPointCut()")
    public void checkSecurityBeforeAddingAnOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        OrderVO orderVO = (OrderVO) joinPoint.getArgs()[0];

        if(orderVO.getUserId() == null){
            throw new InvalidCustomerIdException();
        }
        Optional<Users> userOptional = userRepository.findById(orderVO.getUserId().longValue());

        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();

        if(userIsNotAllowesToAddAnOrder(user.getRoles())){
            throw new InvalidOperationException();
        }

    }
    @Before("com.practice.onlineShop.aspects.SecurityAspect.deliverPointCut()")
    public void checkSecurityBeforeDeliver(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToDeliver(user.getRoles())){
            throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }
    @Before("com.practice.onlineShop.aspects.SecurityAspect.cancelOrderPointCut()")
    public void checkSecurityBeforeCancelingOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> userOptional = userRepository.findById(customerId);
        if(! userOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = userOptional.get();
        if(userIsNotAllowesToCancel(user.getRoles())){
            throw new InvalidOperationException();
        }
        System.out.println(customerId);
    }
    @Before("com.practice.onlineShop.aspects.SecurityAspect.returnOrderPointCut()")
    public void checkSecurityBeforeReturningOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<Users> usersOptional = userRepository.findById(customerId);

        if(!usersOptional.isPresent()){
            throw new InvalidCustomerIdException();
        }
        Users user = usersOptional.get();
        
        if(userIsNotAllowedToReturnOrder(user.getRoles())){
            throw new InvalidOperationException();
        }
    }

    private boolean userIsNotAllowedToReturnOrder(Collection<Roles> roles) {
        return !roles.contains(CLIENT);
    }

    private boolean userIsNotAllowesToCancel(Collection<Roles> roles) {
        return !roles.contains(CLIENT);
    }

    private boolean userIsNotAllowesToDeliver(Collection<Roles> roles) {
        return !roles.contains(EXPEDITOR);
    }

    private boolean userIsNotAllowesToAddAnOrder(Collection<Roles> roles) {
        return !roles.contains(CLIENT);
    }

    private boolean userIsNotAllowesToAddProduct(Collection<Roles> roles) {
        return !(roles.contains(ADMIN));
    }
    private boolean userIsNotAllowesToDeleteProduct(Collection<Roles> roles) {
        return !(roles.contains(ADMIN));
    }
    private boolean userIsNotAllowesToUpdateProduct(Collection<Roles> roles) {
        return (!roles.contains(ADMIN) && !roles.contains(EDITOR));
    }
    private boolean userIsNotAllowesToAddStock(Collection<Roles> roles) {
        return !roles.contains(ADMIN);
    }

}
