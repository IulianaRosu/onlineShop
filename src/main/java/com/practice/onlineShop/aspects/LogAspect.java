package com.practice.onlineShop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
public class LogAspect {
    @Pointcut("execution(* com.practice.onlineShop.controllers.ProductController.addProduct(..))")
    public void addProductPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.ProductController.updateProduct(..))")
    public void updateProductPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.OrderController.addOrder(..))")
    public void addOrderPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.OrderController.deliver(..))")
    public void deliverPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.OrderController.cancelOrder(..))")
    public void cancelOrderPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.OrderController.returnOrder(..))")
    public void returnOrderPointcut(){
    }
    @Pointcut("execution(* com.practice.onlineShop.controllers.ProductController.addStock(..))")
    public void addStockPointcut(){
    }

    @Before("com.practice.onlineShop.aspects.LogAspect.addProductPointcut()")
    public void before (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date());
        System.out.println("ProductVO: " + joinPoint.getArgs()[0]);
        System.out.println("The user had id:" + joinPoint.getArgs()[1]);
    }
    @Before("com.practice.onlineShop.aspects.LogAspect.addStockPointcut()")
    public void beforeAddingStock (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date() + " before adding stock");
        System.out.println("Product code: " + joinPoint.getArgs()[0]);
        System.out.println("Product quantity: " + joinPoint.getArgs()[1]);
        System.out.println("The user had id:" + joinPoint.getArgs()[2]);
    }
    @Before("com.practice.onlineShop.aspects.LogAspect.deliverPointcut()")
    public void beforeDeliver (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date() +" for doing a delivery");
        System.out.println("OrderId: " + joinPoint.getArgs()[0]);
        System.out.println("The user had id:" + joinPoint.getArgs()[1]);
    }
    @Before("com.practice.onlineShop.aspects.LogAspect.cancelOrderPointcut()")
    public void beforeCancelingOrder (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date()+ "for doing a canceltion");
        System.out.println("OrderId: " + joinPoint.getArgs()[0]);
        System.out.println("The user had id:" + joinPoint.getArgs()[1]);
    }
    @Before("com.practice.onlineShop.aspects.LogAspect.returnOrderPointcut()")
    public void beforeReturningOrder (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date()+ "for doing a return");
        System.out.println("OrderId: " + joinPoint.getArgs()[0]);
        System.out.println("The user had id:" + joinPoint.getArgs()[1]);
    }
    @Before("com.practice.onlineShop.aspects.LogAspect.updateProductPointcut()")
    public void beforeUpdate (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date() + "for doing an update");
        System.out.println("ProductVO: " + joinPoint.getArgs()[0]);
        System.out.println("The user had id:" + joinPoint.getArgs()[1]);
    }
    @After("com.practice.onlineShop.aspects.LogAspect.updateProductPointcut()")
    public void after (JoinPoint joinPoint){
        System.out.println("In after aspects at " + new Date());
    }

    @Before("com.practice.onlineShop.aspects.LogAspect.addOrderPointcut()")
    public void beforeAddingAnOrder (JoinPoint joinPoint){
        System.out.println("In before aspects at " + new Date());
        System.out.println("OrderVO " + joinPoint.getArgs()[0]);

    }
}
