package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import web_project.Loggable;
import web_project.weather.DryingRuleRepository;
import web_project.weather.WeatherDTO;
import web_project.weather.WeatherUpdateService;

@DependsOn("loggingAspect") // чтобы аспект делался раньше экшенов
@Component
public class KaktusActions {
    private final int emotionLoss = 10;
    private int waterLoss = 10;
    private final DryingRuleRepository dryingRuleRepository;

    @Value("${app.features.showConstr}")
    private boolean showConstr;

    public KaktusActions(DryingRuleRepository dryingRuleRepository){
        this.dryingRuleRepository = dryingRuleRepository;
    }

    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("KaktusActions родился");
    }
    @Loggable("Кактус: Сохну... 🌵🥀")
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> dryAction() {
        return context -> {
            int curHydrationLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getHydrationLevel();
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setHydrationLevel(curHydrationLevel - waterLoss);
            int curEmotionalLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getEmotionalLevel();
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setEmotionalLevel(Math.max(0, curEmotionalLevel - emotionLoss));

        };
    }

    @Loggable("Кактус: Спасибо! 🌵💧")
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> waterAction() {
        return context -> {
            int curEmotionalLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getEmotionalLevel();
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setEmotionalLevel(curEmotionalLevel + emotionLoss);
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setHydrationLevel(100);
        };
    }
    @Loggable
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> timidAction() {
        return context -> {
            System.out.println("Aaa, меня полили! 🌵");
        };
    }
    @EventListener
    public void onWeatherUpdated(WeatherUpdateService.WeatherUpdatedEvent event) {
        WeatherDTO weather = event.getWeather();
        try{
            waterLoss = dryingRuleRepository.getWaterLoss(weather.getTemperature(), weather.getHumidity());
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении погоды " + e);
        }
        System.out.println("\uD83C\uDF24 WaterLoss: " + waterLoss);
    }
}