package com.example.EduWebProject.controller;

import com.example.EduWebProject.LoaderToDB;
import com.example.EduWebProject.entities.Product;
import com.example.EduWebProject.service.ProductService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class MainController {
    private final ProductService productService;
    private final LoaderToDB loaderToDB;

    public MainController(ProductService productService, LoaderToDB loaderToDB){
        this.productService = productService;
        this.loaderToDB = loaderToDB;
    }

    @GetMapping("/")
    public String mainPage() {
        return "main_page";
    }

    @PostMapping("/load_to_database")
    public String loadToDatabase(@RequestParam String file) {
        loaderToDB.loadFromFile(file);
        return "redirect:/choosing";
    }

    @GetMapping("/choosing")
    public String choosingPage(@SessionAttribute(name = "components", required = false) String components,
                               Model model) {
        System.out.println("choosing: " + components);
        model.addAttribute("components", components != null ? components : "");
        return "choosing";
    }

    @GetMapping("/found_products")
    public String foundProductsPage(Model model){
        if (model.getAttribute("products") == null)
            model.addAttribute("invitation_to_choosing", "Пожалуйста, выберите состав на /choosing");
        return "found_products";
    }

    @PostMapping("/choosing/search")
    public String findProducts(RedirectAttributes attributes, HttpSession session){
        List<Product> products = productService.findByComposition((String) session.getAttribute("components"));
        System.out.println(products);
        if (!products.isEmpty())
            attributes.addFlashAttribute("products", products);
        return "redirect:/found_products";
    }

    @PostMapping("/choosing/add")
    public String addComponent(@RequestParam String component, HttpSession session){
        System.out.println(component);
        if (session.getAttribute("components") == null)
            session.setAttribute("components", component);
        else
            session.setAttribute("components", (String) session.getAttribute("components") + ", " + component);
        System.out.println("session components: " + session.getAttribute("components"));
        return "redirect:/choosing";
    }

    @PostMapping("/choosing/clear")
    public String clearComponents(HttpSession session){
        System.out.println("clear");
        session.setAttribute("components",  null);
        return "redirect:/choosing";
    }
}