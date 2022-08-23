package com.practice.onlineShop.vos;

import com.practice.onlineShop.enums.Currencies;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Enumerated;

import static javax.persistence.EnumType.STRING;

@Data
public class ProductVO {
    private long id;
    @Column(unique = true)
    private String code;
    private String description;
    private double price;
    private int stock;
    private boolean valid;
    private Currencies currency;
}
