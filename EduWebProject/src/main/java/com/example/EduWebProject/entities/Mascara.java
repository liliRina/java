package com.example.EduWebProject.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
final public class Mascara extends Product {

    @Column(nullable = true)
    private String color;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mascara m)
            return super.equals(obj) && m.color.equals(color);

        return false;
    }
}