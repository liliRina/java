package com.example.EduWebProject;

import com.example.EduWebProject.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Init {
    private final ProductService productService;

    public void init() {
        System.out.println("init");
        productService.deleteAll();
        // 1. Увлажняющие крема с разными активными компонентами
        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с гиалуроновой кислотой",
                "La Roche-Posay",
                "вода, глицерин, гиалуроновая кислота, масло ши, ниацинамид",
                1899.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Интенсивный увлажняющий крем",
                "CeraVe",
                "вода, церамиды, глицерин, гиалуроновая кислота, сквалан",
                1299.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем для сухой кожи",
                "Avene",
                "термальная вода, сквалан, глицерин, масло карите, церамиды",
                2199.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Легкий увлажняющий крем-гель",
                "Neutrogena",
                "вода, глицерин, гиалуроновая кислота, аллантоин, пантенол",
                899.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Ночной увлажняющий крем",
                "Vichy",
                "термальная вода, гиалуроновая кислота, пептиды, сквалан, витамин Е",
                2499.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с церамидами",
                "Dr. Jart+",
                "вода, церамиды, глицерин, сквалан, масло ши, ниацинамид",
                2799.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем для чувствительной кожи",
                "Bioderma",
                "вода, глицерин, сквалан, аллантоин, термальная вода, церамиды",
                1599.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "24-часовой увлажняющий крем",
                "Nivea",
                "вода, глицерин, масло ши, пантенол, витамин Е",
                699.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с витамином С",
                "The Ordinary",
                "вода, глицерин, витамин С, гиалуроновая кислота, масло ши",
                1199.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с пептидами",
                "Estee Lauder",
                "вода, пептиды, гиалуроновая кислота, сквалан, церамиды, ниацинамид",
                5899.0
        ));

// 2. Крема с разными типами увлажнения
        productService.create(new ProductService.CreatingProductWithComposition(
                "Гиалуроновый увлажняющий крем",
                "The Inkey List",
                "вода, гиалуроновая кислота, глицерин, сквалан, аллантоин",
                799.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с маслом ши",
                "L'Occitane",
                "вода, масло ши, глицерин, сквалан, масло карите",
                3299.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Церамидный увлажняющий крем",
                "Cosrx",
                "вода, церамиды, глицерин, гиалуроновая кислота, аллантоин",
                1499.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Пантеноловый увлажняющий крем",
                "Bepanthen",
                "вода, пантенол, глицерин, масло ши, витамин Е",
                999.0
        ));

// 3. Крема для разных типов кожи
        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем для жирной кожи",
                "La Roche-Posay",
                "вода, глицерин, цинк, ниацинамид, гиалуроновая кислота",
                1799.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем для зрелой кожи",
                "Lancome",
                "вода, глицерин, пептиды, гиалуроновая кислота, ретинол, витамин С",
                6599.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем для обезвоженной кожи",
                "Clinique",
                "вода, глицерин, сквалан, церамиды, гиалуроновая кислота, аллантоин",
                2899.0
        ));

// 4. Корейские увлажняющие крема
        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с улиточным муцином",
                "Cosrx",
                "вода, улиточный муцин, глицерин, гиалуроновая кислота, церамиды",
                1899.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Рисовый увлажняющий крем",
                "I'm From",
                "рисовая вода, глицерин, масло ши, сквалан, церамиды",
                2799.0
        ));

// 5. Бюджетные варианты
        productService.create(new ProductService.CreatingProductWithComposition(
                "Базовый увлажняющий крем",
                "Simple",
                "вода, глицерин, сквалан, витамин Е",
                499.0
        ));

        productService.create(new ProductService.CreatingProductWithComposition(
                "Увлажняющий крем с алоэ",
                "Nature Republic",
                "сок алоэ, вода, глицерин, гиалуроновая кислота, аллантоин",
                899.0
        ));
    }
}
