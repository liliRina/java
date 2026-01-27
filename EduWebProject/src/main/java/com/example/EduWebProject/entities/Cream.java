package com.example.EduWebProject.entities;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Cream extends Product {
    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    public Cream(String name, String brand, String composition, Double price) {
        super(name, brand, composition, price);
    }

    public Cream(String name, String brand, Double price) {
        super(name, brand, "", price);
    }

    public enum SkinType {
        Oily,
        Dry,
        Sensitive,
        Combination
    }
}

