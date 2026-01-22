package com.example.EduWebProject.entities;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
final public class Cream extends Product {

    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    public Cream(String name, String producer, String composition, Double price) {
        super(name, producer, composition, price);
    }

    public Cream(String name, String producer, Double price) {
        super(name, producer, "", price);
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Cream c)
            return super.equals(obj) && c.skinType.equals(skinType);

        return false;
    }
    @Override
    public String toString() {
        return name + " " +
                producer + " " +
                price.toString();
    }

    public enum SkinType {
        Oily,
        Dry,
        Sensitive,
        Combination
    }
}

