package com.example.EduWebProject.service;

import com.example.EduWebProject.entities.Cream;
import com.example.EduWebProject.entities.Mascara;
import com.example.EduWebProject.entities.Product;
import com.example.EduWebProject.repository.ProductRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ProductService {
    private final ProductRepository productRepository;

    public record CreatingProduct(
            @NotBlank String name,
            @NotBlank String brand,
            @Min(0) Double price
    ) {}

    public record CreatingProductWithComposition(
            @NotBlank String name,
            @NotBlank String brand,
            @NotBlank String composition,
            @Min(0) Double price
    ) {}

    @PostConstruct
    public void init() {
        System.out.println("Сейчас в базе данных: " + productRepository.count() + " продуктов");
    }

    public void createCream(@Valid CreatingProductWithComposition creatingProduct){
        Product prod = new Cream(creatingProduct.name, creatingProduct.brand, creatingProduct.composition, creatingProduct.price);
        productRepository.save(prod);
    }
    public void createMascara(@Valid CreatingProductWithComposition creatingProduct){
        Product prod = new Mascara(creatingProduct.name, creatingProduct.brand, creatingProduct.composition, creatingProduct.price);
        productRepository.save(prod);
    }

    public void deleteAll(){
        productRepository.deleteAll();
    }

    public List<Product> findByComposition(String composition){
        if (composition == null)
            return Collections.emptyList();
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
