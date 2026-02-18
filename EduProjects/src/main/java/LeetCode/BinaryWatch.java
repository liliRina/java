package LeetCode;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BinaryWatch {
    class Watch {
        ArrayList<Integer> usedLights = new ArrayList<>();
        private List<String> ans = new ArrayList<>();
        static LocalTime curTime = LocalTime.of(0, 0);

        static boolean addIfValidated(int i) {
            if(i < 4){
                if(curTime.getHour() + Math.pow(2, i) < 12) {
                    curTime = curTime.plusHours((long)Math.pow(2, i));
                    return true;
                }
            }

            else{
                if(curTime.getMinute() + Math.pow(2, i - 4) < 59){
                    curTime = curTime.plusMinutes((long)Math.pow(2, i - 4));
                    return true;
                }
            }

            return false;
        }
        static void remove(int i) {
            if(i < 4)
                curTime = curTime.minusHours((long)Math.pow(2, i));
            else
                curTime = curTime.minusMinutes((long)Math.pow(2, i - 4));
        }
        void addMinutes(int turnedOn){
            if (turnedOn == 0)
                ans.add(new String(String.valueOf(curTime.getHour()))
                        + ":"
                        + String.format("%2d", curTime.getMinute()).replace(' ', '0'));

            for(int i = usedLights.isEmpty() ? 0 : usedLights.getLast(); i < 10; i++){
                if(!usedLights.contains(i)){
                    if(Watch.addIfValidated(i)){
                        usedLights.add(i);
                        addMinutes(turnedOn - 1);
                        usedLights.removeLast();
                        Watch.remove(i);
                    }
                }
            }
        }
    }

    public List<String> readBinaryWatch(int turnedOn) {
        Watch watch = new Watch();
        watch.addMinutes(turnedOn);
        Collections.sort(watch.ans,
                Comparator.comparingInt(time -> Integer.parseInt(((String)time).split(":")[0]))
                        .thenComparingInt(time -> Integer.parseInt(((String)time).split(":")[1])));
        return watch.ans;
    }
}

