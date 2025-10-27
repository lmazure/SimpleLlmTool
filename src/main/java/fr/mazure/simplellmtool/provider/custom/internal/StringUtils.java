package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*
 * Utility class for string operations. 
 */
class StringUtils {
    
    /*
     * Adds line numbers to the input string.
     */
    public static String addLineNumbers(final String input) {
        final AtomicInteger counter = new AtomicInteger();
        return input.lines()
                    .map(line -> "%03d %s".formatted(Integer.valueOf(counter.incrementAndGet()), line))
                    .collect(Collectors.joining("\n"));
    }
}
