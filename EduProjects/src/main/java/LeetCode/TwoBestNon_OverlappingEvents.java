package LeetCode;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.max;

public class TwoBestNon_OverlappingEvents {
    public int maxTwoEvents(int[][] events) {
        TreeMap<Integer, Integer> maxForStartAfterInd = new TreeMap<>();
        System.out.println(events.length);

        Arrays.stream(events).forEach(e ->maxForStartAfterInd.put(e[0], max(e[2],
                maxForStartAfterInd.getOrDefault(e[0], 0).intValue())));
        
        int maxValue = maxForStartAfterInd.get(maxForStartAfterInd.lastKey());

        for (var entry : maxForStartAfterInd.descendingMap().entrySet()) {
            entry.setValue(max(maxValue, entry.getValue()));
            maxValue = max(maxValue, entry.getValue());
            //maxForStartAfterInd.remove((entry.getKey()));
            //ConcurrentModificationException
            //нельзя удалять/добавлять в foreach, потому то не изменяет итератор
        }
        int maxValuePair = 0;
        int maxValueSingle = 0;
        int maxEnd = 0;
        for (var e : events) {
            if (maxForStartAfterInd.ceilingEntry(e[1] + 1) != null)
                maxValuePair = max(maxValuePair, e[2] + maxForStartAfterInd.ceilingEntry(e[1] + 1).getValue());
            else
                maxValueSingle = max(maxValueSingle, e[2]);
            maxEnd = Math.max(Math.max(maxValue ,maxValuePair), maxEnd);
        }
        return maxEnd;
    }

    int[][] readFromFile() {
        InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("LeetCode/EventsArray.txt");
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        if (in == null)
            return new int[][]{{0, 0, 0}};
        try (Scanner scanner = new Scanner(in)) {
            scanner.useDelimiter("[^0-9-]+");
            int[] currentArray = new int[3];
            int count = 0;
            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                    currentArray[count] = scanner.nextInt();
                    count++;
                    if (count % 3 == 0) {
                        result.add(new ArrayList<>(Arrays.stream(currentArray)
                                .boxed()
                                .collect(Collectors.toList())));
                        count = 0;
                    }
                } else {
                    scanner.next();
                }
            }
        }
        int[][] matrix = result.stream()
                .map(list -> list.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new);

        return matrix;
    }
}
