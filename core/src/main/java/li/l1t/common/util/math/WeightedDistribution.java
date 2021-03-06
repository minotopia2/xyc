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

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stores a set of values with associated probabilities (relative to the other entries' probabilities) and provides a
 * method to retrieve a random element, respecting these probabilities. Does not support storage of null values.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-01-12
 */
public class WeightedDistribution<T> {
    private final NavigableMap<Double, T> probabilities = new TreeMap<>();
    private double cachedSum = 0D;

    public NavigableMap<Double, T> probabilities() {
        return probabilities;
    }

    /**
     * @return the sum of all probabilities stored in this distribution
     */
    public double probabilitySum() {
        return cachedSum;
    }

    /**
     * Puts given item into the distribution with given probability. Note that the probability is relative to the
     * other items in this distribution.
     *
     * @param item        the item to put
     * @param probability the probability of the item being selected, relative to the other items
     */
    public void put(T item, double probability) {
        Preconditions.checkNotNull(item, "item");
        Preconditions.checkArgument(probability > 0, "probability must be positive");
        cachedSum = cachedSum + probability;
        probabilities.put(cachedSum, item);
    }

    /**
     * Selects a random item using given random.
     *
     * @param random the random to get the random number from
     * @return the selected item, never null
     * @throws IllegalStateException if there are no items in this distribution
     */
    public T next(Random random) {
        Preconditions.checkNotNull(random, "random");
        double randomValue = random.nextDouble() * cachedSum;
        Map.Entry<Double, T> entry = probabilities.ceilingEntry(randomValue);
        if (entry == null) {
            if (probabilities.isEmpty()) {
                throw new IllegalStateException("no items defined");
            } else {
                throw new AssertionError("no item matched");
            }
        } else {
            return entry.getValue();
        }
    }

    /**
     * Selects a random item using the current thread's {@link ThreadLocalRandom}.
     * @return the selected item, never null
     * @throws IllegalStateException if there are no items in this distribution
     * @see #next(Random)
     */
    public T next() {
        return next(ThreadLocalRandom.current());
    }
}
