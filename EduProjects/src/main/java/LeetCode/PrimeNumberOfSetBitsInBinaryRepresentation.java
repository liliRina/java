package LeetCode;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.ArrayList;

interface CheckPrime{
    default boolean isPrime(Integer i){
        return IntStream.range(2, 21)
                .dropWhile(j -> i % j != 0)
                .count() == 0;
    }
}

public class PrimeNumberOfSetBitsInBinaryRepresentation implements CheckPrime {
    Set<Bool> primeNum = new TreeSet<>();
    static{

    }
//    public int countPrimeSetBits(int left, int right) {
//
//    }
//    private void initPrime(){
//        for (int i = 0; i < 21; i++)
//            primeNum.add(i, Bool.valueOf(isPrime(i)));
//    }
}
//1000 1
//1001 2
//1010 2
//1011 3
//1100 2
//1101 3
//1110 3
//1111 4

//100 1
//101 2
//110 2
//111 3

//10000
//10001
//10010
//10011
//10100
//10101
//10110
//10111
//11000
//11001
//11010
//11011
//11100

