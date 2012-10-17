package be.cytomine.utils;

import java.util.*;

/**
 * User: lrollus
 * Date: 6/02/12
 * GIGA-ULg
 * This class implement method
 */
public class Utils {

    /**
     * Add seconds to the actual date (seconds may be '-x')
     * @param secondes Number of seconds to add
     * @return Date equal to now + secondes
     */
    public static Date getDatePlusSecond(int secondes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, secondes);
        return cal.getTime();
    }

    /**
     * Comparator method allowing to sort a TreeMap by its values (not by its keys)
     */
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    /**
     * Comparator method allowing to sort a TreeMap by its values desc (not by its keys asc)
     */
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValuesDesc(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
