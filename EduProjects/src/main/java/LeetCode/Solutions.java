package LeetCode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

public class Solutions {
    public static void main(String[] args) {
        SortIntegersbyTheNumberof1Bits sol = new SortIntegersbyTheNumberof1Bits();
        System.out.println(Arrays.toString(sol.sortByBits(new int[]{1024,512,256,128,64,32,16,8,4,2,1})));

    }

}