package com.practice.onlineShop.entities;

import com.practice.onlineShop.enums.Roles;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.util.Collection;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter

public class Users {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;
    private String username;
    private String password;
    private String surname;
    private String firstname;
    @Embedded
    private Address address;
    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    private Collection<Roles> roles;

}
