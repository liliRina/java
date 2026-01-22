package com.example.EduWebProject.entities;
import jakarta.persistence.*;

import lombok.RequiredArgsConstructor;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type") // столбец-метка
@RequiredArgsConstructor
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false)

    @lombok.NonNull
    protected String name;

    @Column(nullable = false)
    @lombok.NonNull
    protected String producer;

    @lombok.NonNull
    @Column(nullable = false)
    protected String composition;

    @lombok.NonNull
    @Column(name = "price", nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")  // не кроссплатформенный
    protected Double price;

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || Product.class.isAssignableFrom(obj.getClass()))
            return false;
        Product product = (Product) obj;
        if (product.name.equals(name)
                && product.producer.equals(producer)
                && product.composition.equals(composition)
                && product.price.equals(price))
            return true;

        return false;
    }

    public Product(){
        this("", "","",  100.0);
    }
}
