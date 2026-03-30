/*
 * (C) Copyright 2026 Hyland (https://hyland.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.http.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @since 1.0.0
 */
public final class ListUtils {

    private ListUtils() {
        // utility class
    }

    @SafeVarargs
    public static <T> List<T> asList(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static <T> List<T> asList(T head, T[] tail) {
        var list = new ArrayList<T>(tail.length + 1);
        list.add(head);
        Collections.addAll(list, tail);
        return list;
    }

    public static List<Boolean> asList(boolean[] values) {
        var list = new ArrayList<Boolean>(values.length);
        for (var v : values) {
            list.add(v);
        }
        return list;
    }

    public static List<Boolean> asList(boolean head, boolean[] tail) {
        var list = new ArrayList<Boolean>(tail.length + 1);
        list.add(head);
        for (var v : tail) {
            list.add(v);
        }
        return list;
    }

    public static List<Integer> asList(int[] values) {
        var list = new ArrayList<Integer>(values.length);
        for (var v : values) {
            list.add(v);
        }
        return list;
    }

    public static List<Integer> asList(int head, int[] tail) {
        var list = new ArrayList<Integer>(tail.length + 1);
        list.add(head);
        for (var v : tail) {
            list.add(v);
        }
        return list;
    }

    public static List<Long> asList(long[] values) {
        var list = new ArrayList<Long>(values.length);
        for (var v : values) {
            list.add(v);
        }
        return list;
    }

    public static List<Long> asList(long head, long[] tail) {
        var list = new ArrayList<Long>(tail.length + 1);
        list.add(head);
        for (var v : tail) {
            list.add(v);
        }
        return list;
    }

    public static List<Double> asList(double[] values) {
        var list = new ArrayList<Double>(values.length);
        for (var v : values) {
            list.add(v);
        }
        return list;
    }

    public static List<Double> asList(double head, double[] tail) {
        var list = new ArrayList<Double>(tail.length + 1);
        list.add(head);
        for (var v : tail) {
            list.add(v);
        }
        return list;
    }
}
