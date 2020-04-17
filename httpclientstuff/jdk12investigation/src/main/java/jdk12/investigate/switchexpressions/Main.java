package jdk12.investigate.switchexpressions;

import java.time.DayOfWeek;

public class Main {

    /**
     * java -cp  jdk12investigation/target/jdk12investigation-1.0-SNAPSHOT.jar jdk12/investigate/switchexpressions/Main
     * Relax
     *
     * @param args
     */
    public static void main(String[] args) {

        DayOfWeek day = DayOfWeek.SATURDAY;

        String activity = switch (day) {
            case SATURDAY, SUNDAY -> "Relax";
            case MONDAY -> "Try to work";
            default -> "Work";
        };

        System.out.println(activity);

    }
}
