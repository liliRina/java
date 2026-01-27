package com.example.EduWebProject.DBTests;

import com.example.EduWebProject.entities.Product;
import com.example.EduWebProject.service.ProductService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class ProductTests {
    private final ProductService productService;

    @Test
    void findByCompositionTest(){
        List<Product> products = productService.findByComposition("ретинол, витамин С");
        assertThat(products.size()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("productSource")
    void createProductTest(String name, String brand, String composition, Double price){
        assertThrows(ConstraintViolationException.class, () -> {
            productService.createCream(new ProductService.CreatingProductWithComposition(name, brand, composition, price));
        });
        assertThrows(ConstraintViolationException.class, () -> {
            productService.createMascara(new ProductService.CreatingProductWithComposition(name, brand, composition, price));
        });
        System.out.println("passed");
    }
    static Stream<Arguments> productSource(){
        return Stream.of(
                Arguments.of("", "brand", "composition", 3.0),
                Arguments.of("name", "", "composition", 3.0),
                Arguments.of("name", "brand", "", 3.0),
                Arguments.of("name", "brand", "composition", -4.0));
    }
}
