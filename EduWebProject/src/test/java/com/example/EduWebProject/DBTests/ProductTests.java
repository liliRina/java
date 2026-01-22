package com.example.EduWebProject.DBTests;

import com.example.EduWebProject.entities.Product;
import com.example.EduWebProject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class ProductTests {

    private final ProductService productService;

    @Test
    void findByCompositionTest(){
        productService.create(new ProductService.CreatingProductWithComposition("dsf", "dsf", "sdf", 3.0));
        List<Product> products = productService.findByComposition("ретинол, витамин С");
        assertThat(products.size()).isEqualTo(1);
    }
}
