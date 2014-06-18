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
package garbler.library;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Library for available characters and their respective statistics
 *
 * @author Rogue <Alice Q>
 */
public class StatsLibrary extends CharMap<CharStats> {

    // THE LENGTH OF A WORD
    private OccurrenceList wordLength;

    // SORTED TREE SERVING AS A CACHE OF COMMONLY USED SHORT SNIPPETS
    private TreeMap<String, OccurrenceMap> primaryCache;

    // FIFO SECONDARY CACHE TO THE TREE INTENDED FOR HOLDING SINGLE-USE SNIPPETS UNTIL THEY EXPIRE OR ARE MOVED TO THE PRIMARY TrEE
    private LinkedList<Entry<String, OccurrenceMap>> secondaryCache;
    private final static int SECONDARY_CACHE_SIZE = 32; // THE MAX AMOUNT OF ENTRIES IN THE CACHE

    /**
     * Default constructor for a case sensitive StatsLibrary
     */
    public StatsLibrary() {
        this(true);
    }

    /**
     * Basic constructor which allows the user to specify the case sensitivity
     * from initialization
     *
     * @param caseSensitive false in order to ignore case sensitivity when
     * accessing data, false otherwise
     */
    public StatsLibrary(boolean caseSensitive) {
        wordLength = new OccurrenceList();
        primaryCache = new TreeMap();
        secondaryCache = new LinkedList();
        this.setCaseSensitive(caseSensitive);
    }
    // STATISTICS
    // - parseWord
    // - parseLine (2)
    // - getWordLengths
    // - getCorrelationsMatching
    /**
     * Method which parseLineLines an entire word and adds all the characters
     * within to the internal statistics tracking structures, as well as general
     * word statistics
     *
     * @param word
     */

    public void parseWord(String word) {
        // CASE SENSITIVITY
        if (!isCaseSensitive()) {
            word = word.toLowerCase(java.util.Locale.ROOT);
        }

        // WORD STATISTICS
        wordLength.increment(word.length() - 1);

        // CHARACTER STATISTICS - DO FOR EACH
        for (int i = 0; i < word.length(); i++) {
            char atIndex = word.charAt(i);
            CharStats cStat = get(atIndex);

            // MAKE SURE THAT IT EXISTS
            if (cStat == null) {
                cStat = new CharStats(atIndex);
                cStat.setCaseSensitive(isCaseSensitive());
                put(atIndex, cStat);
            }

            // ADD CHARACTER STATISTICS
            cStat.addWord(word, i);
        }
    }

    /**
     * Parses an entire line adding it to the internal statistics after
     * separating the sentence into words as specified by a regex string
     *
     * @param line A line of text
     * @param regex The regular expressions string specifying where to break
     * apart the line of text
     */
    public void parseLineRegex(String line, String regex) {
        for (String s : line.split(regex)) {
            parseWord(s);
        }
    }

    /**
     * Parses an entire line adding it to the internal statistics after
     * separating the sentence into words as specified by a regex string
     *
     * @param line a line of text
     * @param delim a string of delimiters to use in addition to whitespace
     */
    public void parseLine(String line, String delim) {
        parseLineRegex(line, "[" + delim.replace("]", "\\]").replace("[", "\\[").replace("%", "\\%") + "\\s]+");
    }

    /**
     * Parses an entire line adding it to the internal statistics, separating
     * the String into words on any whitespace
     *
     * @param line a line of text
     */
    public void parseLine(String line) {
        parseLineRegex(line, "\\s+");
    }

    /**
     * Method for retrieval of data regarding all the word lengths encountered.
     * Please note that this list has indeces offset by 1. That is, the value at
     * index 0 corresponds to word length 1.
     *
     * @return An OccurrenceList displaying all the word lengths that have been
     * found
     */
    public OccurrenceList getWordLengths() {
        return wordLength;
    }

    /**
     * Method to find the usage of specific characters following a certain
     * sequence. Note that this does not guarantee that the character has been
     * previously used following the sequence and is merely taken as a function
     * of previous estimates. The only OccurrenceMaps returned are those which
     * non-zero data at the index identified by the distance of each character
     * from the end.
     *
     * @param charSequence the string of characters before the sequence
     * @throws IllegalArgumentException when an invalid mode is used
     * @return A character-sorted map of OccurrenceLists demonstrating the
     * amount of influence each relevant character has on the word based on the
     * distance from the end as an integer. If a character has no influence it
     * is not included in the return value.
     */
    public OccurrenceMap generateInfluenceMap(String charSequence) {
        int length = charSequence.length();
        int position;

        OccurrenceMap results = new OccurrenceMap(isCaseSensitive());

        // GO THROUGH EACH CHARACTER IN THE SEQQUENCES
        for (int i = length - 1; i >= 0; i--) {
            char charAt = charSequence.charAt(i);
            position = length - i - 1;

            // FETCH THE ASSOCIATED CHARSTAT
            CharStats stats = get(charAt);
            if (stats == null) {
                continue;   // THERE ARE NO STATS SO IT DOESN'T MATTER
            }

            // GET ALL THE LISTS WITH IMPORTANT DATA
            OccurrenceMap relevantLists = stats.getCorrelationsAtIndex(position);

            // AND ADD THAT DATA TO THE RESULTS
            for (Entry<Character, OccurrenceList> entry : relevantLists.entrySet()) {
                char key = entry.getKey();
                OccurrenceList value = entry.getValue();

                // MAKE A NEW LIST IF NEEDED
                OccurrenceList existingList = results.get(key);
                if (existingList == null) {
                    existingList = new OccurrenceList();
                    results.put(key, existingList);
                }
                existingList.increment(position, value.get(position));
            }
        }

        return results;
    }

    /**
     * Searches the cache to see if an influence map already exists in the
     * cache, reorganizing the two caches if required
     *
     * @param charSequence a sequence of characters to get the map for
     * @return an OccurrenceMap of influence if one was found in the cache,
     * otherwise null if it wasn't
     */
    public OccurrenceMap getInfluenceMapFromCache(String charSequence) {
        // FIRST SEE IF THE SNIPPET ALREADY EXISTS IN THE CACHE
        OccurrenceMap cached = primaryCache.get(charSequence);
        if (cached != null) {
            // IF IT EXISTS JUST RETURN IT
            System.out.println(charSequence + " found in primary cache");
            return cached;
        }
        // IT'S NOT IN THE PRIMARY SO GET IT FROM THE SECONDARY
        for (Entry<String, OccurrenceMap> entry : secondaryCache) {
            // IT WAS FOUND IN THE SECONDARY SO MOVE THIS TO THE PRIMARY AND RETURN IT
            if (entry.getKey().equals(charSequence)) {
                System.out.println(charSequence + " found in secondary cache");
                primaryCache.put(entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        // IT'S NOT IN THE SECONDARY EITHER SO RETURN NULL
        return null;
    }

    public OccurrenceMap getInfluenceMapCached(String charSequence) {
        // SEE IF THE MAP IS IN ONE OF THE CACHES
        OccurrenceMap cached = getInfluenceMapFromCache(charSequence);

        // IF IT WAS, RETURN IT
        if (cached != null) {
            return cached;
        }

        // IF IT WASN'T, GENERATE A NEW ONE
        System.out.println(charSequence + " generated");
        OccurrenceMap generated = generateInfluenceMap(charSequence);

        // ADD IT TO THE SECONDARY CACHE, REMOVING THE OLDEST ENTRY IF FULL
        secondaryCache.push(new java.util.AbstractMap.SimpleImmutableEntry(charSequence, generated));
        if (secondaryCache.size() > SECONDARY_CACHE_SIZE) {
            secondaryCache.pop();
        }

        // AND RETURN IT
        return generated;
    }

    /**
     * Method which compacts an influence map into percentages for each
     * character
     *
     * @param influenceMap the influence map to compact
     * @param decay the amount of decay that influence has the further away each
     * character is from the end of the word. A value above 0.5 favors newer
     * entries while a value below 0.5 favors older entries.
     * @return a character-sorted map of probabilities for each character with a
     * net sum of 1.0f
     */
    public CharMap<Float> compactInfluenceMap(OccurrenceMap influenceMap, float decay) {
        boolean caseSensitivity = isCaseSensitive();
        // RESULTANT MAP OF THE PERCENTAGE OF INFLUENCE ON EACH CHARACTER
        CharMap<Float> results = new CharMap<Float>(caseSensitivity) {
            @Override
            public Float merge(Float oldValue, Float newValue) {
                return oldValue + newValue;
            }
        };

        // FIRST GET EACH RELATIVE INFLUENCE
        float totalSum = 0.0f;
        for (Entry<Character, OccurrenceList> entry : influenceMap.entrySet()) {
            OccurrenceList list = entry.getValue();
            Character key = entry.getKey();

            // FIRST GET THE DENOMINATOR
            int size = list.size();

            // THEN GET THE LOCAL INFLUENCE
            float influence = list.getCount(size - 1);
            float decayInv = 1 - decay;
            for (int i = size - 2; i >= 0; i--) {
                influence = influence * decayInv + list.getCount(i) * decay;
            }

            // THESE ARE ALL GOING TO BE UNIQUE
            results.put(key, influence);
            totalSum += influence;
        }

        // THEN TURN ALL THE RELATIVE PERCENTS INTO ABSOLUTES
        for (Entry<Character, Float> entry : results.entrySet()) {
            results.put(entry.getKey(), entry.getValue() / totalSum);
        }

        return results;
    }

    /**
     * Method which compacts an influence map into percentages for each
     * character
     *
     * @param influenceMap the influence map to compact
     * @param threshold the raw percentage threshold to cutoff. Anything below
     * this will not be counted towards the total.
     * @param decay the amount of decay that influence has the further away each
     * character is from the end of the word. A value above 0.5 favors newer
     * entries while a value below 0.5 favors older entries.
     *
     * @return a character-sorted map of probabilities for each character with a
     * net sum of 1.0f
     */
    public CharMap<Float> compactInfluenceMap(OccurrenceMap influenceMap, float decay, float threshold) {
        boolean caseSensitivity = isCaseSensitive();
        // RESULTANT MAP OF THE PERCENTAGE OF INFLUENCE ON EACH CHARACTER
        CharMap<Float> results = new CharMap<Float>(caseSensitivity) {
            @Override
            public Float merge(Float oldValue, Float newValue) {
                return oldValue + newValue;
            }
        };

        // FIRST GET EACH RELATIVE INFLUENCE
        float totalSum = 0.0f;
        for (Entry<Character, OccurrenceList> entry : influenceMap.entrySet()) {
            OccurrenceList list = entry.getValue();
            Character key = entry.getKey();

            // FIRST GET THE DENOMINATOR
            int size = list.size();

            // THEN GET THE LOCAL INFLUENCE
            float influence = list.getCount(size - 1);
            float decayInv = 1 - decay;
            for (int i = size - 2; i >= 0; i--) {
                influence = influence * decayInv + list.getCount(i) * decay;
            }

            // THESE ARE ALL GOING TO BE UNIQUE
            results.put(key, influence);
            totalSum += influence;
        }

        // REMOVE THE ENTRIES THAT DON'T MAKE THE CUROFF
        LinkedList<Character> trashbin = new LinkedList();

        float newSum = totalSum;
        for (Entry<Character, Float> entry : results.entrySet()) {
            if (entry.getValue() / totalSum < threshold) {
                trashbin.add(entry.getKey());
                newSum -= entry.getValue();
            }
        }
        for (Character c : trashbin) {
            results.remove(c);
        }
        totalSum = newSum;

        // THEN TURN ALL THE RELATIVE PERCENTS INTO ABSOLUTES
        for (Entry<Character, Float> entry : results.entrySet()) {
            results.put(entry.getKey(), entry.getValue() / totalSum);
        }

        return results;
    }

    // OVERWRITTEN METHODS
// - merge
// - setCaseSensitive
    @Override
    public CharStats merge(CharStats oldValue, CharStats newValue) {
        return oldValue.addAll(newValue);
    }

    @Override
    public final void setCaseSensitive(boolean active) {
        super.setCaseSensitive(active);
        for (CharStats stats : values()) {
            stats.setCaseSensitive(active);
        }
    }

    @Override
    public void clear() {
        super.clear();
        wordLength.clear();
    }
}