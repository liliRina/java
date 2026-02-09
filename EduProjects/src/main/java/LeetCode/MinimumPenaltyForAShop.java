package LeetCode;

public class MinimumPenaltyForAShop {
    public int bestClosingTime(String customers) {
        int[] AfterClose = new int[customers.length() + 1];
        int[] BeforeClose = new int[customers.length() + 1];
        BeforeClose[0] = 0;
        AfterClose[customers.length()] = 0;
        for (int i = 0; i < customers.length(); i++){
            BeforeClose[i + 1] = BeforeClose[i] + (customers.charAt(i) == 'Y' ? 0 : 1);
            AfterClose[customers.length() - i - 1] = AfterClose[customers.length() - i]
                    + (customers.charAt(customers.length() - i - 1) == 'Y' ? 1 : 0);
        }
        int minPenalty = Integer.MAX_VALUE;
        int indMinPenalty = 0;
        for (int i = 0; i < BeforeClose.length; i++){
            if (minPenalty > BeforeClose[i] + AfterClose[i]){
                minPenalty = BeforeClose[i] + AfterClose[i];
                indMinPenalty = i;
            }
        }
        return indMinPenalty;
    }
}