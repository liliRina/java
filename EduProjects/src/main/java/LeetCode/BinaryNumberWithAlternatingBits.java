package LeetCode;

class Bool {
    boolean value = false;

    Bool(boolean val){
        value = val;
    }
    protected void setValue(boolean val){
        value = val;
    }
    static Bool valueOf(boolean val){
        return new Bool(val);
    }
}
class Boolic extends Bool {
    boolean value = true;

    Boolic(boolean val){
        super(!val);
        value = val;
    }
    @Override
    public void setValue(boolean val){
        value = val;
        super.value = !val;
    }
    public String getName(){
        return "Привет, я булик";
    }
}

interface CheckerWithAlternatingBits <T> {
    public Bool hasAlternatingBits(T n);

}
interface IntegerChecker extends CheckerWithAlternatingBits<Integer> {
    default boolean hasAlternatingBits(int n){
        System.out.println("hasAlternatingBits in IntegerWithAlternatingBits");
        return true;
    }
}

public sealed class BinaryNumberWithAlternatingBits implements IntegerChecker permits BinaryOperationChecker, RecursionChecker{
    @Override
    public Bool hasAlternatingBits(Integer n) {
        IntegerChecker.super.hasAlternatingBits(n.intValue());
        System.out.println("hasAlternatingBits in BinaryNumberWithAlternatingBits");
        return null;
    }
}
final class BinaryOperationChecker extends BinaryNumberWithAlternatingBits {
    @Override
    public boolean hasAlternatingBits(int n){
        int highestBit = Integer.highestOneBit(n);
        int altBits = ~n & highestBit * 2 - 1;
        if (n % 2 == 1)
            n--;

        if(altBits * 2 == n)
            return true;
        return false;
    }
    @Override
    public Boolic hasAlternatingBits(Integer n) {
        int highestBit = Integer.highestOneBit(n);
        int altBits = ~n & highestBit * 2 - 1;
        if (n % 2 == 1)
            n--;

        if(altBits * 2 == n)
            return new Boolic(false);
        return new Boolic(true);
    }
}

non-sealed class RecursionChecker extends BinaryNumberWithAlternatingBits {
    Bool state;
    @Override
    public boolean hasAlternatingBits(int n){
        if (state == null)
            state = new Bool(n%2==0 ? true : false);
        if (n == 1){
            if (!state.value)
                return true;
            else
                return false;
        }

        if (n % 2 == 1 && !state.value || n % 2 == 0 && state.value){
            state.value = !state.value;
            return hasAlternatingBits(n/2);
        }
        return false;
    }
}
