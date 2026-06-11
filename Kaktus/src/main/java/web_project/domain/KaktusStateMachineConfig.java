package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import static web_project.domain.Kaktus.NORM_LINE;

@Configuration
@EnableStateMachineFactory
class KaktusStateMachineConfig
        extends StateMachineConfigurerAdapter<Kaktus.KaktusState, Kaktus.KaktusEvent> {
    @Autowired
    private KaktusActions actions;
    @Autowired
    private KaktusGuards guards;

    @Value("${app.features.showConstr}")
    private boolean showConstr;
    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("KaktusStateMachineConfig родился");
    }

    @Override
    public void configure(StateMachineStateConfigurer<Kaktus.KaktusState, Kaktus.KaktusEvent> states)
            throws Exception {
        states
                .withStates()
                .initial(Kaktus.KaktusState.THIRSTY)
                .state(Kaktus.KaktusState.NORMAL)
                .choice(Kaktus.KaktusState.CHOICE)
                .choice(Kaktus.KaktusState.EMOTIONAL_CHOICE)
                .state(Kaktus.KaktusState.IN_EMOTION)
                .end(Kaktus.KaktusState.DIED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<Kaktus.KaktusState, Kaktus.KaktusEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
            .source(Kaktus.KaktusState.THIRSTY)
            .target(Kaktus.KaktusState.EMOTIONAL_CHOICE)
            .event(Kaktus.KaktusEvent.WATER)
            .guard(onlyForType(Kaktus.KaktusType.TIMID))
            .action(context -> {
                actions.waterAction().execute(context);
            })
        .and()

            .withChoice()
            .source(Kaktus.KaktusState.EMOTIONAL_CHOICE)
            .first(Kaktus.KaktusState.IN_EMOTION, guards.timidInEmotionAlive())
            .last(Kaktus.KaktusState.NORMAL)
        .and()

            .withExternal()
            .source(Kaktus.KaktusState.NORMAL)
            .target(Kaktus.KaktusState.CHOICE)
            .event(Kaktus.KaktusEvent.DRY)
            .action(context -> {
                actions.dryAction().execute(context);
            })
        .and()

            .withExternal()
            .source(Kaktus.KaktusState.THIRSTY)
            .target(Kaktus.KaktusState.CHOICE)
            .event(Kaktus.KaktusEvent.DRY)
            .action(context -> {
                actions.dryAction().execute(context);  // явно вызываем
            }) //.action(actions.dryAction()) //aop вызывается 1 раз при создании
        .and()

            .withChoice()
            .source(Kaktus.KaktusState.CHOICE)
            .first(Kaktus.KaktusState.IN_EMOTION, guards.timidInEmotionAlive())
            .then(Kaktus.KaktusState.NORMAL, dryAndNormal())
            .then(Kaktus.KaktusState.THIRSTY, dryAndAlive())
            .last(Kaktus.KaktusState.DIED);
    };

    private Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> onlyForType(Kaktus.KaktusType requiredType) {
        return context -> {
            Message<Kaktus.KaktusEvent> message = context.getMessage();
            MessageHeaders headers = message.getHeaders();
            Kaktus.KaktusType actualType = headers.get("type", Kaktus. KaktusType.class);
            return requiredType == actualType;
        };
    }
    private Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> dryAndNormal() {
        return context -> {
            Kaktus.KaktusContext kaktus = context.getExtendedState().get("context", Kaktus.KaktusContext.class);
            return kaktus.getHydrationLevel() > NORM_LINE;
        };
    }
    private Guard<Kaktus.KaktusState, Kaktus.KaktusEvent> dryAndAlive() {
        return context -> {
            Kaktus.KaktusContext kaktus = context.getExtendedState().get("context", Kaktus.KaktusContext.class);
            return kaktus.getHydrationLevel() > 0;
        };
    }
}