/*
 * The MIT License
 *
 * Copyright 2014 Rogue <Alice Q.>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package garbler.structure;

import garbler.library.CharMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Basic wrapper class for a CharMap<Integer> providing the basic skeleton code
 * and methods required
 *
 * @author Rogue <Alice Q.>
 */
public class BasicIntegerCharMap extends CharMap<Integer> {

    /**
     * Wrapper for CharMap constructor which specifies the default case
     * sensitivity
     *
     * @param caseSensitivity true if case should be ignored, false otherwise
     */
    public BasicIntegerCharMap(boolean caseSensitivity) {
        super(caseSensitivity);
    }

    @Override
    public Integer mergeValues(Integer oldValue, Integer newValue) {
        return oldValue + newValue;
    }

    // ADDITIONAL METHODS
    // - getSum
    // - toDecimalMap
    // - increment
    // - decrement
    // - reset
    /**
     * Method to get the sum of all the internally held values
     *
     * @return an integer representation of the sum of all internal values
     */
    public Integer getSum() {
        int sum = 0;
        for (int i : values()) {
            sum += i;
        }
        return sum;
    }

    /**
     * Conversion method from integer to float.
     *
     * @return a new CharMap of equivalent floats with the same key set as this
     * map
     */
    public BasicDecimalCharMap toDecimalMap() {
        BasicDecimalCharMap map = new BasicDecimalCharMap(super.isCaseSensitive());
        for (Entry<Character, Integer> entry : entrySet()) {
            map.put(entry.getKey(), entry.getValue().floatValue());
        }
        return map;
    }

    /**
     * Increments the count at a certain key, initializing it if it doesn't
     * exist
     *
     * @param key the character key to get
     * @param quantity the amount to increment by
     * @return the new value held at the provided key
     */
    public int increment(Character key, Integer quantity) {
        Integer currentValue = get(key);
        if (currentValue == null) {
            currentValue = 0;
        }
        put(key, quantity + currentValue);
        return quantity + currentValue;
    }

    /**
     * Decrements the count at a certain key, initializing it if it doesn't
     * exist
     *
     * @param key the character key to get
     * @param quantity the amount to increment by
     * @return the new value held at the provided key
     */
    public int decrement(Character key, Integer quantity) {
        Integer currentValue = get(key);
        if (currentValue == null) {
            currentValue = 0;
        }
        put(key, quantity - currentValue);
        return quantity - currentValue;
    }

    /**
     * Resets the value at a given key to 0 without deleting the key. However,
     * this will not create a new key if one doesn't exist.
     *
     * @param key the character key to get
     */
    public void reset(Character key) {
        if (containsKey(key)) {
            put(key, 0);
        }
    }

    // STATIC METHODS
    // - trimMap   
    /**
     * Method to trim any low-valued items inside a character-keyed decimal map
     *
     * @param map the map to trim
     * @param threshold the lower threshold of acceptable values
     * @return the number of deleted items
     * @throws IllegalArgumentException when the threshold is less than or equal
     * to 0
     */
    public static int trimMap(CharMap<Integer> map, int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be greater than 0");
        }
        LinkedList<Character> trash = new LinkedList();
        // SEEK LOW-VALUED ENTRIES
        for (Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() <= threshold) {
                trash.push(entry.getKey());
            }
        }
        // EMPTY THE TRASH
        int removed = trash.size();
        for (Character c : trash) {
            map.remove(c);
        }
        return removed;
    }
}
