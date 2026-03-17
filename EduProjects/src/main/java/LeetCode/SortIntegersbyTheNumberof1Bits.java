package LeetCode;

import java.util.Arrays;
import java.util.Comparator;

public class SortIntegersbyTheNumberof1Bits {
    public int[] sortByBits(int[] arr) {
        Integer[] ints = new Integer[arr.length];
        Arrays.setAll(ints, i -> arr[i]);
        ints = Arrays.stream(arr)
                .boxed()
                .toArray(Integer[]::new);

        Arrays.sort(ints, Comparator
                .<Integer, Integer>comparing(i -> Integer.bitCount(i))  // сначала по количеству битов
                .thenComparing(i -> i));               // потом по самому числу

        int[] arr2 = Arrays.stream(ints)
                .<Integer>mapToInt(Integer::intValue)
                .toArray();

        return arr2;
        //Arrays.sort(arr, Comparator.comparingInt(i -> Integer.valueOf(i)));
    }
}
