package com.example.EduWebProject.entities;
import jakarta.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;

import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type") // столбец-метка
@RequiredArgsConstructor()
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false)
    @NonNull
    protected String name;

    @Column(nullable = false)
    @NonNull
    protected String brand;

    @NonNull
    @Column(nullable = false)
    protected String composition;

    @NonNull
    @Column(name = "price", nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")  // не кроссплатформенный
    protected Double price;

    public Product(){
        this("", "","",  100.0);
    }

    @Override
    public String toString() {
        return name + " " +
                brand + " " +
                price.toString();
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || Product.class.isAssignableFrom(obj.getClass()))
            return false;
        Product product = (Product) obj;
        if (product.name.equals(name)
                && product.brand.equals(brand)
                && product.composition.equals(composition)
                && product.price.equals(price))
            return true;

        return false;
    }
    @Override
    public int hashCode(){
        return Objects.hash(name, brand, composition, price);
    }
}
