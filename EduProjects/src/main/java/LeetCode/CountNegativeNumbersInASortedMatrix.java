package LeetCode;

public class CountNegativeNumbersInASortedMatrix {
    public int countNegatives(int[][] grid) {
        int n = grid.length;
        int m = grid[0].length;
        int leftBound = 0;
        int countNegative = 0;
        for (int i = n - 1; i >= 0; i--){
            for (int j = leftBound; j < m ; j++){
                if (grid[i][j] < 0){
                    leftBound = j;
                    countNegative += m - j;
                    break;
                }
            }
        }
        return countNegative;
    }
}
