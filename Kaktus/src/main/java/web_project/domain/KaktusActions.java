package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import web_project.Loggable;

@DependsOn("loggingAspect") // чтобы аспект делался раньше экшенов
@Component
public class KaktusActions {
    @Value("${app.features.showConstr}")
    private boolean showConstr;
    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("KaktusActions родился");
    }

    @Loggable("🌵🥀 Кактус: Сохну...")
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> dryAction() {
        return context -> {
            int curHydrationLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getHydrationLevel();

            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setHydrationLevel(curHydrationLevel - 10);
            int curEmotionalLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getEmotionalLevel();
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setEmotionalLevel(Math.max(0, curEmotionalLevel - 10));

        };
    }

    @Loggable("Кактус: Спасибо! 🌵💧")
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> waterAction() {
        return context -> {
            int curEmotionalLevel = ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).getEmotionalLevel();
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setEmotionalLevel(curEmotionalLevel + 20);
            ((Kaktus.KaktusContext) context.getExtendedState()
                    .get("context", Kaktus.KaktusContext.class)).setHydrationLevel(100);
        };
    }

    @Loggable
    public Action<Kaktus.KaktusState, Kaktus.KaktusEvent> timidAction() {
        return context -> {
            System.out.println("🌵 Aaa, меня полили!");
        };
    }
}
