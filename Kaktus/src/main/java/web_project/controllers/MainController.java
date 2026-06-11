package web_project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web_project.Nursery;

import java.util.Map;

@Controller
public class MainController {
    @Autowired
    Nursery nursery;

    @GetMapping("/")
    public String mainPage( @RequestParam(required = false) String imagePath, Model model){
        for (var n: nursery) {
            model.addAttribute("imagePath", n.getImagePath());
            model.addAttribute("hydrationLevel", n.getContext().getHydrationLevel());
        }
        return "main";
    }

    @GetMapping("/blue")
    public String hello(RedirectAttributes attributes) { // только с пост, потому что изменяет сессию, не идемпотентными
        nursery.waterKaktus(0);
        return "redirect:/";
    }

    @PostMapping("/post")
    public String click(){
        return "";
    }

    @GetMapping("/get_time")
    @ResponseBody
    public Map<String, Object> getStatus() {
        System.out.println("Обновляем тайм");
        nursery.dryKaktusa();
        return Map.of("imagePath", nursery.get(0).getImagePath(),
                "hydrationLevel", nursery.get(0).getContext().getHydrationLevel());
    }
}

