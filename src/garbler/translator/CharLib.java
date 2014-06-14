/*
 * The MIT License
 *
 * Copyright 2014 Rogue <Alice Q>.
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
package garbler.translator;

import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Library for available characters and their respective statistics
 *
 * @author Rogue <Alice Q>
 */
public class CharLib {

    // MAP OF ALL THE STORED CHARACTERS IN THE SYSTEM
    private TreeMap<Character, CharStats> lib;

    // LIST OF ALL THE KEYS BEING USED IN THE LIBRARY
    private ArrayList<Character> keys;

    /**
     * Default constructor
     */
    public CharLib() {
        lib = new TreeMap();
        keys = new ArrayList();
    }

    public static void main(String[] args) {
        System.out.println("Start run");

        CharLib lib = new CharLib();

        java.util.Random rand = new java.util.Random();

        CharStats stats = new CharStats('a');
        System.out.println(stats);

        for (char i = 'a'; i < 'h'; i++) {
            stats.addField(i);
        }

        System.out.println(stats.getOccurrenceList('a'));
        System.out.println(stats.addOccurrence('b', 8, 5));
        System.out.println(stats.getOccurrenceList('c'));

        System.out.println(stats);

        System.out.println("End run");
    }
}
