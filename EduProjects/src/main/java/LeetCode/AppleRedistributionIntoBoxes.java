package LeetCode;

import java.util.Arrays;

public class AppleRedistributionIntoBoxes {
    public int minimumBoxes(int[] apple, int[] capacity) {
        int sumApple =  Arrays.stream(apple).sum();
        Arrays.sort(capacity);
        int i = capacity.length - 1;
        for (; i >= 0 && sumApple > 0; i--) {
            sumApple -= capacity[i];
        }
        return capacity.length - 1 - i;
    }
}
