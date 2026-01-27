package com.example.EduWebProject;

import com.example.EduWebProject.service.ProductService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@RequiredArgsConstructor
public class LoaderToDB {
    private final ProductService productService;
    private final ObjectMapper mapper;

    public void loadFromFile(String fileName){
        productService.deleteAll();
        Resource resource = new ClassPathResource(fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            String[] components;
            System.out.println("Начата загрузка данных из файла");
            while ((line = reader.readLine()) != null) {
                try {
                    ProductService.CreatingProductWithComposition product = mapper.readValue(line, ProductService.CreatingProductWithComposition.class);
                    if (line.toLowerCase().contains("крем"))
                        productService.createCream(product);
                    if (line.toLowerCase().contains("тушь"))
                        productService.createMascara(product);
                } catch (ValidationException e) {
                    System.out.println("неверный формат данных " + line);
                } catch (JsonProcessingException e) {
                    System.out.println("ошибка чтения json " + line);
                    System.out.println(e);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException в LoaderToDB: " + e);
        }
        System.out.println("Закончена загрузка данных из файла");
    }
}
