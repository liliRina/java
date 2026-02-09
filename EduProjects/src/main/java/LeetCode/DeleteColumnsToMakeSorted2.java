package LeetCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class DeleteColumnsToMakeSorted2 {
    public int minDeletionSize(String[] strs) {
        ArrayList<String> ss = new ArrayList<>(Arrays.asList(strs));
        TreeSet<Integer> deleted = new TreeSet<>();
        String prevS = ss.get(0);
        boolean beenDeleted = false;
        for(var j = 1; j < ss.size(); j++) {
            String s = ss.get(j);
            int i = 0;

            while(prevS.charAt(i)==s.charAt(i) || deleted.contains(i))
                i++;

            while(prevS.charAt(i)>s.charAt(i) || deleted.contains(i)) {
                if (prevS.charAt(i)>s.charAt(i)) {
                    deleted.add(i);
                    beenDeleted = true;
                }
                i++;
            }
            if (beenDeleted) {
                j = 0;
                prevS = ss.get(0);
                beenDeleted = false;
            }
            else {
                prevS = s;
            }
        }
        return deleted.size();
    }
}
