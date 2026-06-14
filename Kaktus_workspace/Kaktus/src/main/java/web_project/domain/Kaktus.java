package web_project.domain;

import lombok.*;

@Builder (
    //builderMethodName
    buildMethodName = "plant",
    //builderClassName
    access = AccessLevel.PUBLIC
)
@ToString
@Getter
@Setter
public class Kaktus {

    static public final int NORM_LINE = 50;
    static public final int CALM_LINE = 30;

    private final int id = IdName.generateId();

    @Builder.Default
    private String name = "Kaktus";
    @Builder.Default
    private TypeThorns typeThorns = TypeThorns.Beautiful;
    @Builder.Default
    private KaktusType type = KaktusType.TIMID;
    @Builder.Default
    private KaktusState state = KaktusState.THIRSTY;
    @Builder.Default
    KaktusContext context = new KaktusContext();

    @Getter
    @Setter
    static public class KaktusContext {
        private int hydrationLevel = 100;
        private int emotionalLevel = 0;
        @Override
        public String toString(){
            return "\n" + hydrationLevel + " " + emotionalLevel;
        }

    }

    @Override
    public String toString(){
        return "Кактус: " + context.getHydrationLevel();
    }
    public String getImagePath(){
        return getType().name().toLowerCase() + "_" + getState().name().toLowerCase() + ".jpg";
    }

    static class IdName {
        static int id = 8;
        static int generateId(){
            return id++;
        }
    }

    enum TypeThorns { // всегда статик, не видит нестатик полей внешнего, но видит приват статик
        Beautiful,
        Ugly
    }

    public enum KaktusState{
        THIRSTY,
        NORMAL,
        IN_EMOTION,
        DIED,
        CHOICE,
        EMOTIONAL_CHOICE;
    }
    public enum KaktusEvent {
        WATER,
        DRY,
        COMPLIMENT,
        NEGLECT
    }

    public enum KaktusType {
        TIMID,
        NORMAL,      // обычный
        GRUMPY,      // ворчливый (может отказаться)
        DRAMATIC,    // драматичный (обижается)
        PHILOSOPHER  // философ (читает лекции)
    }

}
