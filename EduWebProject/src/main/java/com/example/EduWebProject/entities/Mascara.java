package com.example.EduWebProject.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Mascara extends Product {
    @Column(nullable = true)
    private String color;

    public Mascara(String name, String brand, String composition, Double price) {
        super(name, brand, composition, price);
    }
    public Mascara(String name, String brand, Double price) {
        super(name, brand, "", price);
    }

}