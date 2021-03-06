/*
 * MIT License
 *
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy) and contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package li.l1t.common.util.math;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Class that provides some static methods to deal with numbers.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 */
public final class NumberHelper {

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final Random RANDOM = new Random();
    private static final DecimalFormat SPACE_FORMAT;
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final Map<Class<? extends Number>, MathOperator<? extends Number>> CLASSES_TO_OPERATORS;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(' ');//hacky!
        SPACE_FORMAT = new DecimalFormat(",###", symbols);
        CLASSES_TO_OPERATORS =
                ImmutableMap.<Class<? extends Number>, MathOperator<? extends Number>>builder()
                        .put(Integer.class, MathOperator.INTEGER_MATH_OPERATOR)
                        .put(Long.class, MathOperator.LONG_MATH_OPERATOR)
                        .put(Double.class, MathOperator.DOUBLE_MATH_OPERATOR)
                        .put(Float.class, MathOperator.FLOAT_MATH_OPERATOR)
                        .build();

    }

    private NumberHelper() {

    }

    /**
     * Tries to get a predefined {@link MathOperator} for a class.
     *
     * @param clazz Target class - Must be the actual Object one, not the primitive.
     * @param <N>   number class to get an operator for
     * @return A MathOperator corresponding to {@code clazz}, if available.
     */
    @Nullable
    @SuppressWarnings("unchecked") //The Map is guaranteed to always have the corresponding object
    public static <N extends Number> MathOperator<N> getOperator(@Nonnull Class<N> clazz) {
        return (MathOperator<N>) CLASSES_TO_OPERATORS.get(clazz);
    }

    /**
     * Formats a number with spaces as group separator for use on signs. The number will be colored in black, if it's to short to actually
     * automatically be aligned right, it will be prefixed with grey underscores. Possible outputs: §7________§0123 123456789012345
     *
     * @param number The number to be formatted
     * @return A string to be used on Minecraft signs representing the input, aligned to the right.
     */
    public static String formatForSignRightAligned(int number) {
//        String rtrn = NumberFormat.getInstance(Locale.GERMAN).format(number);
        String rtrn = NumberHelper.SPACE_FORMAT.format(number);
        if (rtrn.length() > 10) {
            return rtrn;
        }
        return "§7" + StringUtils.leftPad("§0" + rtrn, 13, '_');
    }

    /**
     * Tries to extract an int from a String. If the extraction fails (i.e. if a {@link NumberFormatException} is thrown by
     * {@link Integer#parseInt(String)}),
     * {@code def} is returned.
     *
     * @param source String to get the int from.
     * @param def    int to return if {@code source} does not contain a valid int.
     * @return int contained in {@code source} or {@code def}.
     */
    public static int tryParseInt(final String source, final int def) {
        int result;
        try {
            result = Integer.parseInt(source);
        } catch (NumberFormatException e) {
            return def;
        }
        return result;
    }

    /**
     * Converts an array of bytes to its hexadecimal representation. http://stackoverflow.com/a/9855338/1117552
     *
     * @param bytes Byte array to convert.
     * @return Hexadecimal String representation of bytes.
     */
    public static String bytesToHex(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @return A random integer. Considered to be "securely" random.
     */
    public static BigInteger randomInteger() {
        return BigInteger.probablePrime(130, SECURE_RANDOM);
    }

    /**
     * Determines if a number {@code toCheck} is between or equal to one the boundaries specified. There is no special order of the boundaries
     * required, they can even be equal.
     *
     * @param toCheck   Target integer
     * @param boundary1 One of the boundaries
     * @param boundary2 One of the boundaries
     * @return {@code true}, If {@code toCheck} is between boundary1 and boundary2.
     */
    public static boolean isNumberBetween(int toCheck, int boundary1, int boundary2) {
        if (boundary1 > boundary2) {
            return boundary2 <= toCheck && toCheck <= boundary1;
        } else if (boundary1 < boundary2) {
            return boundary1 <= toCheck && toCheck <= boundary2;
        } else {
            return toCheck == boundary1;
        }
    }
}
