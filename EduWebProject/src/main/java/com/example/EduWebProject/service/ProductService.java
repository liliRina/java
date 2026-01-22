package com.example.EduWebProject.service;

import com.example.EduWebProject.entities.Cream;
import com.example.EduWebProject.entities.Product;
import com.example.EduWebProject.repository.ProductRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public record CreatingProduct(
            @NotBlank String name,
            @NotBlank String producer,
            @Min(0) Double price
    ) {}

    public record CreatingProductWithComposition(
            @NotBlank String name,
            @NotBlank String producer,
            @NotBlank String composition,
            @Min(0) Double price
    ) {}

    @PostConstruct
    public void init() {
        System.out.println(productRepository.count());
    }
    public void create(CreatingProduct creatingProduct){
        Product prod = new Cream(creatingProduct.name, creatingProduct.producer, creatingProduct.price);
        productRepository.save(prod);
    }
    public void create(CreatingProductWithComposition creatingProduct){
        Product prod = new Cream(creatingProduct.name, creatingProduct.producer, creatingProduct.composition, creatingProduct.price);
        productRepository.save(prod);
    }

    public void deleteAll(){
        productRepository.deleteAll();
    }

    public List<Product> findByComposition(String composition){

        Specification<Product> spec = (root, query, cb) -> {
            String[] components = composition.split(", ");
            Predicate[] predicates = new Predicate[components.length];
            for (int i = 0; i < components.length; i++) {
                predicates[i] = cb.like(root.get("composition"), "%" + components[i] + "%");
            }
            return cb.and(predicates);
        };
        return productRepository.findAll(spec);
    }
}
