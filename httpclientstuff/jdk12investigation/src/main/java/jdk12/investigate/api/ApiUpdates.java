package jdk12.investigate.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApiUpdates {

    public static void main(String[] args) {

        var ints = Stream.of(10, 20, 30, 40);
        long average =
                ints.collect( Collectors.teeing(
                        Collectors.summingInt(Integer::valueOf),
                        Collectors.counting(),
                        (sum, count) -> sum / count
                ) );
        System.out.println("average " + average);

    }
}
