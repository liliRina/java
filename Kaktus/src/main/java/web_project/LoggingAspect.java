package web_project;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Value("${app.features.showConstr}")
    private boolean showConstr;

    @PostConstruct
    public void init() {
        if (showConstr)
            System.out.println("LoggingAspect родился");
    }
    @After("within(Nursery) && execution(* *Kaktus(int)) && args(id)")
    public void log(JoinPoint joinPoint, int id) {
        Object target = ((Nursery)joinPoint.getTarget()).get(id);
        System.out.println(target);
    }

    @After("@annotation(loggable)")
    public void logBefore(JoinPoint joinPoint, Loggable loggable) {
        System.out.println(loggable.value());
    }
}
