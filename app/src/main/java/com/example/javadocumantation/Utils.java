package com.example.javadocumantation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class providing various string manipulation methods.
 */
public class Utils {

    private static final int INDEX_NOT_FOUND = -1;
    private static final String EMPTY = "";

    /**
     * Abbreviates a string by given lower and upper boundaries and appends a specified string at the end.
     *
     * @param str          The string to abbreviate.
     * @param lower        The lower boundary for abbreviation.
     * @param upper        The upper boundary for abbreviation.
     * @param appendToEnd  String to append to the end of the abbreviated string.
     * @return The abbreviated string.
     *
     * Example:
     * abbreviate("Hello World", 3, 7, "...") => "Hel..."
     */
    public static String abbreviate(final String str, int lower, int upper, final String appendToEnd) {
        isTrue(upper >= -1, "upper value cannot be less than -1");
        isTrue(upper >= lower || upper == -1, "upper value is less than lower value");
        if (isEmpty(str)) {
            return str;
        }

        if (lower > str.length()) {
            lower = str.length();
        }

        if (upper == -1 || upper > str.length()) {
            upper = str.length();
        }

        final StringBuilder result = new StringBuilder();
        final int index = indexOf(str, " ", lower);
        if (index == -1) {
            result.append(str, 0, upper);
            if (upper != str.length()) {
                result.append(defaultString(appendToEnd));
            }
        } else {
            result.append(str, 0, Math.min(index, upper));
            result.append(defaultString(appendToEnd));
        }

        return result.toString();
    }

    /**
     * Extracts initials from a given string, using the specified delimiters to identify word boundaries.
     *
     * @param str        The string from which to extract initials.
     * @param delimiters Characters used to identify word boundaries.
     * @return A string containing the initials.
     *
     * Example:
     * initials("John A. Doe", ' ', '.') => "JAD"
     */
    public static String initials(final String str, final char... delimiters) {
        if (isEmpty(str)) {
            return str;
        }
        if (delimiters != null && delimiters.length == 0) {
            return EMPTY;
        }
        final Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen / 2 + 1];
        int count = 0;
        boolean lastWasGap = true;
        for (int i = 0; i < strLen; ) {
            final int codePoint = str.codePointAt(i);

            if (delimiterSet.contains(codePoint) || delimiters == null && Character.isWhitespace(codePoint)) {
                lastWasGap = true;
            } else if (lastWasGap) {
                newCodePoints[count++] = codePoint;
                lastWasGap = false;
            }

            i += Character.charCount(codePoint);
        }
        return new String(newCodePoints, 0, count);
    }

    /**
     * Swaps the case of a string, converting uppercase to lowercase and vice versa.
     *
     * @param str The string whose case is to be swapped.
     * @return The string with its case swapped.
     *
     * Example:
     * swapCase("Hello World") => "hELLO wORLD"
     */
    public static String swapCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        boolean whitespace = true;
        for (int index = 0; index < strLen; ) {
            final int oldCodepoint = str.codePointAt(index);
            final int newCodePoint;
            if (Character.isUpperCase(oldCodepoint) || Character.isTitleCase(oldCodepoint)) {
                newCodePoint = Character.toLowerCase(oldCodepoint);
                whitespace = false;
            } else if (Character.isLowerCase(oldCodepoint)) {
                if (whitespace) {
                    newCodePoint = Character.toTitleCase(oldCodepoint);
                    whitespace = false;
                } else {
                    newCodePoint = Character.toUpperCase(oldCodepoint);
                }
            } else {
                whitespace = Character.isWhitespace(oldCodepoint);
                newCodePoint = oldCodepoint;
            }
            newCodePoints[outOffset++] = newCodePoint;
            index += Character.charCount(newCodePoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * Wraps a string based on the specified wrap length, new line string, and a string to wrap on.
     *
     * @param str           The string to wrap.
     * @param wrapLength    The length to wrap at.
     * @param newLineStr    The string to use for line breaks.
     * @param wrapLongWords Indicates if long words should be wrapped.
     * @param wrapOn        The string to wrap on.
     * @return The wrapped string.
     *
     * Example:
     * wrap("Hello World, have a nice day!", 10, "\n", true, " ") =>
     * "Hello
     * World,
     * have a
     * nice
     * day!"
     */
    public static String wrap(final String str,
                              int wrapLength,
                              String newLineStr,
                              final boolean wrapLongWords,
                              String wrapOn) {
        if (str == null) {
            return null;
        }
        if (newLineStr == null) {
            newLineStr = System.lineSeparator();
        }
        if (wrapLength < 1) {
            wrapLength = 1;
        }
        if (isBlank(wrapOn)) {
            wrapOn = " ";
        }
        final Pattern patternToWrapOn = Pattern.compile(wrapOn);
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);
        int matcherSize = -1;

        while (offset < inputLineLength) {
            int spaceToWrapAt = -1;
            Matcher matcher = patternToWrapOn.matcher(str.substring(offset,
                    Math.min((int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L), inputLineLength)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    matcherSize = matcher.end();
                    if (matcherSize != 0) {
                        offset += matcher.end();
                        continue;
                    }
                    offset += 1;
                }
                spaceToWrapAt = matcher.start() + offset;
            }

            if (inputLineLength - offset <= wrapLength) {
                break;
            }

            while (matcher.find()) {
                spaceToWrapAt = matcher.start() + offset;
            }

            if (spaceToWrapAt >= offset) {
                wrappedLine.append(str, offset, spaceToWrapAt);
                wrappedLine.append(newLineStr);
                offset = spaceToWrapAt + 1;
            } else if (wrapLongWords) {
                if (matcherSize == 0) {
                    offset--;
                }
                wrappedLine.append(str, offset, wrapLength + offset);
                wrappedLine.append(newLineStr);
                offset += wrapLength;
                matcherSize = -1;
            } else {
                matcher = patternToWrapOn.matcher(str.substring(offset + wrapLength));
                if (matcher.find()) {
                    matcherSize = matcher.end() - matcher.start();
                    spaceToWrapAt = matcher.start() + offset + wrapLength;
                }

                if (spaceToWrapAt >= 0) {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, spaceToWrapAt);
                    wrappedLine.append(newLineStr);
                    offset = spaceToWrapAt + 1;
                } else {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, str.length());
                    offset = inputLineLength;
                    matcherSize = -1;
                }
            }
        }

        if (matcherSize == 0 && offset < inputLineLength) {
            offset--;
        }

        wrappedLine.append(str, offset, str.length());

        return wrappedLine.toString();
    }

    /**
     * Generates a set of delimiters.
     *
     * @param delimiters Array of delimiter characters.
     * @return A set containing code points of the delimiters.
     */
    private static Set<Integer> generateDelimiterSet(final char[] delimiters) {
        final Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters == null || delimiters.length == 0) {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
            }

            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    /**
     * Checks if an expression is true and throws an exception with the provided message if not.
     *
     * @param expression Boolean expression to be checked.
     * @param message    Exception message if the expression is false.
     */
    private static void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Returns the length of the provided char sequence or 0 if it's null.
     *
     * @param cs The character sequence.
     * @return The length of the character sequence.
     */
    private static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Checks if a char sequence is empty.
     *
     * @param cs The character sequence to check.
     * @return true if the char sequence is empty or null, false otherwise.
     */
    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks if a char sequence is blank (empty or only contains whitespace).
     *
     * @param cs The character sequence to check.
     * @return true if the char sequence is blank, false otherwise.
     */
    private static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the string itself or an empty string if the input is null.
     *
     * @param str The string to check.
     * @return The original string or an empty string if it's null.
     */
    private static String defaultString(final String str) {
        return Objects.toString(str, EMPTY);
    }

    /**
     * Finds the index of a sub-sequence in a char sequence starting from a specified position.
     *
     * @param seq       The char sequence to be searched.
     * @param searchSeq The sub-sequence to find.
     * @param startPos  The starting position for the search.
     * @return The index of the sub-sequence or -1 if not found.
     */
    private static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        if (seq instanceof String) {
            return ((String) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuilder) {
            return ((StringBuilder) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuffer) {
            return ((StringBuffer) seq).indexOf(searchSeq.toString(), startPos);
        }
        return seq.toString().indexOf(searchSeq.toString(), startPos);
    }
}
