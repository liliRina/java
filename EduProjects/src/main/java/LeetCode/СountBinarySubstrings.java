package LeetCode;

public class СountBinarySubstrings {
    public int countBinarySubstrings(String s) {
        int first = 0, second = 0, endSecondNum;
        char firstNum, secondNum;
        int ans = 0;
        while (first < s.length()){
            firstNum = s.charAt(first);
            secondNum  = firstNum == '0' ? '1' : '0';

            second = s.indexOf(String.valueOf(secondNum), first);
            if (second == -1)
                break;

            endSecondNum = s.indexOf(String.valueOf(firstNum), second);
            if (endSecondNum == -1)
                endSecondNum = s.length();
            ans += Integer.min(second - first, endSecondNum - second);

            first = second;
        }
        return ans;
    }
}

//0011

//1100

//0110
//0101
//1010
//10
//01