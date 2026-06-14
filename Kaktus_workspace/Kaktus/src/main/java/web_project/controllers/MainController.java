package web_project.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web_project.Nursery;
import web_project.weather.WeatherUpdateService;

import java.util.Map;

@Controller
@AllArgsConstructor
public class MainController {
    Nursery nursery;
    WeatherUpdateService weatherUpdateService;

    @GetMapping("/")
    public String mainPage( @RequestParam(required = false) String imagePath, Model model){
        for (var n: nursery) {
            model.addAttribute("imagePath", n.getImagePath());
            model.addAttribute("hydrationLevel", n.getContext().getHydrationLevel());
            model.addAttribute("weather", weatherUpdateService.getWeather());
        }
        return "main";
    }

    @PostMapping("/water")
    public String water(RedirectAttributes attributes) {
        nursery.waterKaktus(0);
        return "redirect:/";
    }

    @GetMapping("/get_time")
    @ResponseBody
    public Map<String, Object> dry() {
        //System.out.println("Обновляем тайм");
        nursery.dryKaktusa();
        return Map.of("imagePath", nursery.get(0).getImagePath(),
                "hydrationLevel", nursery.get(0).getContext().getHydrationLevel(),
                "weather", weatherUpdateService.getWeather());
    }
}