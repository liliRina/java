package LeetCode;

import java.util.TreeMap;
import java.util.stream.IntStream;

public class CheckIfaStringContainsAllBinaryCodesofSizeK {
    public boolean hasAllCodes(String s, int k) {
        TreeMap<String, String> mapa = new TreeMap<>();
        mapa = IntStream.range(0, s.length() - k + 1)
                .mapToObj(i -> s.substring(i, i+k))
                .peek(i -> System.out.println(i))
                //.collect(Collectors.toMap(Function.identity(), Function.identity(), (v1, v2) -> v1, TreeMap<String, String>::new));.
                .reduce(new TreeMap<String, String>(), (map, s2) -> {
                    map.putIfAbsent(s2, s2);
                    return map;
                }, (map, map2)->{
                    map.putAll(map2);
                    return map;});
        System.out.println(mapa.size());
        return mapa.size() == Math.pow(2, k);
    }
}
