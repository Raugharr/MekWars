/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package mekwars.common.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Utility class for random number generation. */
public final class RandomUtils {

    private RandomUtils() {}

    /**
     * Returns a random number between 0 and (bound - 1).
     *
     * @param bound The upper bound (exclusive).
     * @return A random number between 0 and (bound - 1).
     * @throws IllegalArgumentException If bound is less than 1.
     */
    public static int getRandomNumber(int bound) {
        if (bound < 1) {
            throw new IllegalArgumentException("bound must be at least 1");
        }
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * Returns a random key from the given List.
     *
     * @param entryList A list of pairs where the key is an element, and the value is the weight
     *     assigned to that key.
     * @return A random key from entryList based on the weights provided.
     */
    public static <T> T getRandomItem(List<Map.Entry<T, Integer>> entryList) {
        int totalWeight = entryList.stream().mapToInt(Map.Entry<T, Integer>::getValue).sum();

        if (totalWeight < 1) {
            return null;
        }
        int randomWeight = getRandomNumber(totalWeight);

        for (Map.Entry<T, Integer> value : entryList) {
            randomWeight -= value.getValue();

            if (randomWeight <= 0) {
                return value.getKey();
            }
        }
        return null;
    }
}
