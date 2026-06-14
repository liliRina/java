package web_project.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

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
            .guard(guards.onlyForType(Kaktus.KaktusType.TIMID))
            .action(context -> {
                actions.waterAction().execute(context);
            })
        .and()

            .withChoice()
            .source(Kaktus.KaktusState.EMOTIONAL_CHOICE)
            .first(Kaktus.KaktusState.IN_EMOTION, guards.timidInEmotionAndAlive())
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
            .first(Kaktus.KaktusState.IN_EMOTION, guards.timidInEmotionAndAlive())
            .then(Kaktus.KaktusState.NORMAL, guards.IsNormal())
            .then(Kaktus.KaktusState.THIRSTY, guards.IsAlive())
            .last(Kaktus.KaktusState.DIED);
    };
}