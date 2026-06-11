package web_project;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;
import web_project.domain.Kaktus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static web_project.domain.Kaktus.KaktusState;

@Component
public class Nursery implements Iterable<Kaktus> {
    @Lazy
    @Autowired
    private Nursery self;

    List<Kaktus> kaktusa = new ArrayList<>();
    private StateMachineFactory<KaktusState, Kaktus.KaktusEvent> factory;

    public Nursery(StateMachineFactory<KaktusState, Kaktus.KaktusEvent> factory){
        this.factory = factory;
        this.kaktusa.add(Kaktus.builder().name("nia").plant());
    }

    @Value("${app.features.showConstr}")
    private boolean showConstr;
    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("Nursery родился");
    }

    public Kaktus get(int id){
        return kaktusa.get(id);
    }

    public void dryKaktusa(){
        for (int i = 0; i < kaktusa.size(); i++)
            self.dryKaktus(i);
    }

    public void dryKaktus(int id){
        Kaktus kaktus = kaktusa.get(id);

        StateMachine<KaktusState, Kaktus.KaktusEvent> sm = factory.getStateMachine();
        KaktusState realState = kaktusa.get(id).getState();
        sm.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachine(
                    new DefaultStateMachineContext<>(
                            realState,
                            null,
                            null,
                            null,
                            null
                    )
            );
        });
        sm.getExtendedState().getVariables().put("context", kaktus.getContext());
        sm.start();

        Message<Kaktus.KaktusEvent> message = MessageBuilder
                .withPayload(Kaktus.KaktusEvent.DRY)
                .setHeader("type", Kaktus.KaktusType.TIMID)
                .build();

        sm.sendEvent(Kaktus.KaktusEvent.DRY);
        KaktusState newState = sm.getState().getId();
        kaktus.setState(newState);
    }

    public void waterKaktus(int id){
        Kaktus kaktus = kaktusa.get(id);

        StateMachine<KaktusState, Kaktus.KaktusEvent> sm = factory.getStateMachine();
        KaktusState realState = kaktusa.get(id).getState();

        sm.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachine(
                    new DefaultStateMachineContext<>(
                            realState,
                            null,
                            null,
                            null,
                            null
                    )
            );
        });
        sm.getExtendedState().getVariables().put("context", kaktus.getContext());
        sm.start();
        Message<Kaktus.KaktusEvent> message = MessageBuilder
                .withPayload(Kaktus.KaktusEvent.WATER)
                .setHeader("type", Kaktus.KaktusType.TIMID)
                .build();

        sm.sendEvent(message);
        KaktusState newState = sm.getState().getId();
        kaktus.setState(newState);
    }

    public String getImagePath(int id){
        Kaktus kaktus = kaktusa.get(id);
        return kaktus.getType().name().toLowerCase() + "_" + kaktus.getState().name().toLowerCase();
    }

    @Override
    public Iterator<Kaktus> iterator() {
        return kaktusa.iterator();
    }
}
