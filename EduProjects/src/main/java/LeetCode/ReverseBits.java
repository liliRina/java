package LeetCode;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReverseBits {
    public int reverseBits(int n) {
        String bitsInt = String.format("%32s", Integer.toBinaryString(n))
                .replace(' ', '0');
        String reverseBitsInt = IntStream.range(0, bitsInt.length())
                        .mapToObj(i -> String.valueOf(bitsInt.charAt(bitsInt.length() - 1 - i)))
                        .collect(Collectors.joining(""));
        return Integer.parseInt(reverseBitsInt, 2);
    }
}
