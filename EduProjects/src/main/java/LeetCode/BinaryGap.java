package LeetCode;

public class BinaryGap {
    public int binaryGap(int n) {
        String bstr = Integer.toBinaryString(n);
        int i = bstr.indexOf('1');
        int maxDist = 0;
        while (i < bstr.length()){
            int next1 = bstr.indexOf("1", i + 1);
            if (next1 == -1)
                break;
            maxDist = Integer.max(next1 - i, maxDist);
            i = next1;
        }
        return maxDist;
    }
}
