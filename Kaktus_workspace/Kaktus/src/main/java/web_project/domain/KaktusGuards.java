package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import static web_project.domain.Kaktus.*;

@Component
public class KaktusGuards{
    @Value("${app.features.showConstr}")
    private boolean showConstr;
    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("KaktusGuards родился");
    }
    Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> onlyForType(Kaktus.KaktusType requiredType) {
        return context -> {
            Message<KaktusEvent> message = context.getMessage();
            MessageHeaders headers = message.getHeaders();
            Kaktus.KaktusType actualType = headers.get("type", Kaktus. KaktusType.class);
            return requiredType == actualType;
        };
    }
    Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> IsNormal() {
        return context -> {
            Kaktus.KaktusContext kaktus = context.getExtendedState().get("context", Kaktus.KaktusContext.class);
            return kaktus.getHydrationLevel() > NORM_LINE;
        };
    }
    Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> IsAlive() {
        return context -> {
            Kaktus.KaktusContext kaktus = context.getExtendedState().get("context", Kaktus.KaktusContext.class);
            return kaktus.getHydrationLevel() > 0;
        };
    }

    public Guard<KaktusState, KaktusEvent> timidInEmotionAndAlive() {
        return context -> {
            KaktusContext kaktus = context.getExtendedState().get("context", KaktusContext.class);
            return kaktus.getEmotionalLevel() > CALM_LINE && kaktus.getHydrationLevel() > 0;
        };
    }
}