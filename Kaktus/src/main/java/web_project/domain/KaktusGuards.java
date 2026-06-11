package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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

    public Guard<KaktusState, KaktusEvent> timidInEmotionAlive() {
        return context -> {
            KaktusContext kaktus = context.getExtendedState().get("context", KaktusContext.class);
            return kaktus.getEmotionalLevel() > CALM_LINE && kaktus.getHydrationLevel() > 0;
        };
    }
}


//                .and()
//                .withExternal()
//                .source(KaktusState.HYDRATED)
//                .target(KaktusState.THIRSTY)
//                .event(KaktusEvent.TIME_PASSES)
//
//                // ========== ДРАМАТИЧНЫЙ (обида) ==========
//                .and()
//                .withExternal()
//                .source(KaktusState.THIRSTY)
//                .target(KaktusState.DEPRESSED)
//                .event(KaktusEvent.NEGLECT)
//                .guard(guards.getsTimid())
//                .action(waterAction());

//@Bean
//public Action<KaktusState, KaktusEvent> grumpyRefusalAction() {
//    return context -> {
//        System.out.println("🌵 (ворчливо): Не хочу воду!");
//        context.getExtendedState().getVariables().put("mood", "BAD");
//    };
//}
//
//@Bean
//public Action<KaktusState, KaktusEvent> dramaticOffenseAction() {
//    return context -> {
//        System.out.println("🌵 (драматично): Ты забыл меня полить! Я увядаю! 💔");
//    };
//}

//@Component
//class KaktusGuards {
//    // Guard для драматичной обиды
//    public Guard<KaktusState, KaktusEvent> getsTimid() {
//        return context -> {
//            String idCharacter = context.getExtendedState()
//                    .get( "idCharacter", String.class);
//
//            if (idCharacter != "timid") {
//                return false;
//            }
//
//            Integer days = context.getExtendedState()
//                    .get("daysWithoutWater", Integer.class);
//
//            // драматичный обижается после 1 дня без воды
//            return days != null && days >= 1;
//        };
//    }
//}

