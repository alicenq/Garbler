/*
 * The MIT License
 *
 * Copyright 2014 Rogue <Alice Q>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, addAll, publish, distribute, sublicense, and/or sell
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

/**
 * Class intended to hold statistics for a single character
 *
 * @author Rogue <Alice Q>
 */
public class CharStats {

    // THE NAME OF THE CHARACTER BEING REPRESENTED BY THESE STATISTICS
    private Character name;

    // THE NUMBER OF TIMES THIS CHARACTER HAS BEEN ENCOUNTERED
    private int occurrences;

    // THE DISTANCE FROM THE START AND END OF A WORD
    private OccurrenceList startDistances;
    private OccurrenceList endDistances;

    // MAP OF ALL THE TIMES THIS CHARACTER OCCURS IN THE SAME WORD AS OTHER CHARACTERS, AND THEIR DISTANCES. NOTE THAT THIS ONLY TRACKS DISTANCE FROM
    private CharMap<OccurrenceList> correlations;

    // CONSTRUCTORS
    /**
     * Usage of this constructor is not recommended, please use CharStats(char).
     * This constructor makes a new CharStats for the null character
     */
    public CharStats() {
        this('\0');
    }

    /**
     * Basic constructor for a specific character
     *
     * @param c The character this entry is for
     */
    public CharStats(char c) {
        name = c;

        occurrences = 0;

        startDistances = new OccurrenceList();
        endDistances = new OccurrenceList();

        correlations = new CharMap<OccurrenceList>() {
            // WHEN A MERGE IS REQUIRED SIMPLY ADD ALL THE NEW VALUES TO THE OLD VALUES
            @Override
            public void merge(OccurrenceList oldValue, OccurrenceList newValue) {
                oldValue.addAll(newValue);
            }
        };
    }

    // GET-SETS
    // - setCaseSensitive
    // - isCaseSensitive
    // - getDictionary
    /**
     * Method to set the case sensitivity of the internal character-sorted
     * structures
     *
     * @param active false in order to ignore case sensitivity when accessing
     * data, false otherwise
     */
    public void setCaseSensitive(boolean active) {
        correlations.setCaseSensitive(active);
    }

    /**
     * Method for retrieving the case sensitivity of the internal
     * character-sorted structures
     *
     * @return true if case is taken into consideration when accessing data,
     * false if case is ignored.
     */
    public boolean isCaseSensitive() {
        return correlations.isCaseSensitive();
    }

    /**
     * @return A Collection of all the characters being currently used
     */
    public java.util.Collection<Character> getAlphabet() {
        return correlations.getAlphabet();
    }

    // STAT TRACKING
// - addInstance
// - addPositionFromStart
// - addPositionFromEnd
// - addCharacterCorrelation
    /**
     * Adds a single encounter to the internal counter
     */
    public void addInstance() {
        occurrences++;
    }

    /**
     * Adds a position occurrence from the start of a word
     *
     * @param distance The distance from the start of the word
     * @throws ArrayIndexOutOfBoundsException When distance is less than 0
     */
    public void addPositionFromStart(int distance) {
        startDistances.increment(distance);
    }

    /**
     * Adds a position occurrence from the end of a word
     *
     * @param distance The distance from the end of the word
     * @throws ArrayIndexOutOfBoundsException When distance is less than 0
     */
    public void addPositonFromEnd(int distance) {
        endDistances.increment(distance);
    }

    /**
     * Adds a character occurrence between this character and another, making
     * the list if necessary
     *
     * @param c The character occurrence to add
     * @param distanceTo The distance to the passed character. Note that this
     * must be greater than 0.
     * @throws ArrayIndexOutOfBoundsException When distanceTo is less than 1
     */
    public void addCharacterCorrelation(char c, int distanceTo) {
        OccurrenceList list = correlations.get(c);

        if (list == null) {
            list = new OccurrenceList();
            correlations.put(c, list);
        }

        list.increment(distanceTo - 1);
    }

    // STAT FETCHING
    // - getCount
    // - getDistancesFromEnd
    // - getDistancesFromStart
    // - getCorrelations
    // - getAllCorrelations
    /**
     * The number of occurrences
     *
     * @return The total number of times this character has been encountered
     */
    public int getCount() {
        return occurrences;
    }

    /**
     * Distance from the start of a word
     *
     * @return An OccurrenceList representation of the distances between this
     * character and the start of each tracked word
     */
    public OccurrenceList getDistancesFromStart() {
        return startDistances;
    }

    /**
     * Distance from the end of a word
     *
     * @return An OccurrenceList representation of the distances between this
     * character and the end of each tracked word
     */
    public OccurrenceList getDistancesFromEnd() {
        return endDistances;
    }

    /**
     * The correlations between two characters. In other words the number of
     * times the passed character has been found after this character in a word,
     * and the distance between the two
     *
     * @param c The character to test for
     * @return An OccurrenceList representation of the number of tracked
     * correlations, or null if none exists
     */
    public OccurrenceList getCorrelations(char c) {
        return correlations.get(c);
    }

    /**
     * The correlations between this character and other respectively tracked
     * characters. This does not guarantee that there is an entry for every
     * single character, just the ones which have been tracked.
     *
     * @return A Collection of OccurrenceLists each representing a correlation
     */
    public java.util.Collection<OccurrenceList> getAllCorrelations() {
        return correlations.values();
    }

    // STRUCTURE MODIFIERS
    // - reset(2)
    // - prepare
    // - compact
    // - addAll
    /**
     * Method for resetting all statistics regarding a second character with
     * regards to this one
     *
     * @param c The character to clear
     */
    public void reset(char c) {
        OccurrenceList list = correlations.get(c);

        if (list != null) {
            list.clear();
        }
    }

    /**
     * Resets all the internally contained data
     */
    public void reset() {
        occurrences = 0;
        startDistances.clear();
        endDistances.clear();
        correlations.clear();
    }

    /**
     * Prepares a new character for statistic tracking by making an empty field
     * corresponding to the character with respect to this one. This method does
     * nothing if a field already exists.
     *
     * @param c The new character to make a field for
     */
    public void prepare(char c) {
        OccurrenceList list = correlations.get(c);

        if (list == null) {
            list = new OccurrenceList();
            correlations.put(c, list);
        }
    }

    /**
     * Method to compact the internal data structures where necessary
     */
    public void collapse() {
        correlations.compact();
    }

    /**
     * Merges this Object's tracked counts with another one's
     *
     * @param stats The second CharStats to inherit values from
     */
    public void addAll(CharStats stats) {
        // MAP OF CORRELATIONS
        correlations.addAll(stats.correlations);
        if (stats.isCaseSensitive()) {
            correlations.setCaseSensitive(true);
        }

        // DISTANCE FROM START AND END
        startDistances.addAll(stats.startDistances);
        endDistances.addAll(stats.endDistances);

        // TOTAL COUNT
        occurrences += stats.occurrences;
    }
    
    @Override
    public String toString(){
        return correlations.getAlphabet().toString() + " " + occurrences;
    }
}
