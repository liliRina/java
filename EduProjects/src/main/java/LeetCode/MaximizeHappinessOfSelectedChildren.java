package LeetCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class MaximizeHappinessOfSelectedChildren {
    public long maximumHappinessSum(int[] happiness, int k) {
        ArrayList<Integer> happinessList = Arrays.stream(happiness)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        happinessList.sort(Comparator.naturalOrder());
        long sumHappiness = 0;
        for (int i = happinessList.size() - 1; i >= 0; i--){
            if (k == 0)
                break;
            sumHappiness += Math.max(0, happinessList.get(i) - (happinessList.size() - 1 - i));
            k--;
        }
        return sumHappiness;
    }
}