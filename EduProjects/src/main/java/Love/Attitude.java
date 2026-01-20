package Love;

abstract public class Attitude {
    public enum Attitudes{
        Love,
        Hate,
        NotThatIntoYou
    }
    abstract String reply();

    @Override
    public String toString(){
        return reply();
    }
}
interface Degree{
    int bound = 5; // public static final
    int getDegree(); // по дефолту public
    void setDegree(int deg);
}

class Love extends Attitude implements Degree{
    private int degree ;

    @Override
    public String reply(){ // сделаю паблик
        if (degree < bound)
            return "Pfff";
        else
            return "And me!";
    }
    public int getDegree(){
        return degree;
    }
    public void setDegree(int deg){
        degree = deg;
    }
}

class Hate extends Attitude implements Degree{
    private int degree;

    @Override
    public String reply(){
        if (degree < bound)
            return "I'm sad!";
        else
            return "Adieu!";
    }
    public int getDegree(){
        return degree;
    }
    public void setDegree(int deg) {
        degree = deg;
    }
}

class NotThatIntoYou extends Attitude {
    public String reply(){
        return "Ohhhh(";
    }
}
