package LeetCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class N_RepeatedElementInSize2NArray {
    public int repeatedNTimes(int[] nums) {
        int n = nums.length/2;
        HashMap<Integer, Integer> numberMap = new HashMap<>();
        Arrays.stream(nums).forEach(num -> numberMap.put(num, numberMap.getOrDefault(num, 0) + 1));
        for (Map.Entry en: numberMap.entrySet()){
            if (en.getValue().equals(n))
                return (int)en.getKey();
        }
        return 0;
    }
}
