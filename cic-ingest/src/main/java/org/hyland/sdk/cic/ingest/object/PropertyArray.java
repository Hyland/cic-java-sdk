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
 *     Damian Ujma <damian.ujma@hyland.com>
 */
package org.hyland.sdk.cic.ingest.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * An immutable, typed array of property values for use in {@link IngestEventProperties}.
 * <p>
 * Use the typed {@code of(...)} factories to construct instances — element types are enforced at build time, preventing
 * runtime failures during serialization.
 *
 * @since 1.0.0
 * @deprecated since 1.1.0, in favor of the typed {@link IngestEventProperty} model, see {@link IngestEventProperties}.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class PropertyArray {

    private static final PropertyArray EMPTY = new PropertyArray(List.of());

    private final List<Object> elements;

    @SuppressWarnings("unchecked")
    private PropertyArray(List<?> elements) {
        this.elements = (List<Object>) elements;
    }

    public List<Object> elements() {
        return elements;
    }

    public static PropertyArray empty() {
        return EMPTY;
    }

    public static PropertyArray of(String... values) {
        Objects.requireNonNull(values, "values cannot be null");
        return fromObjects(values);
    }

    public static PropertyArray of(int... values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0) {
            return EMPTY;
        }
        return new PropertyArray(IntStream.of(values).boxed().toList());
    }

    public static PropertyArray of(long... values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0) {
            return EMPTY;
        }
        return new PropertyArray(LongStream.of(values).boxed().toList());
    }

    public static PropertyArray of(double... values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0) {
            return EMPTY;
        }
        return new PropertyArray(DoubleStream.of(values).boxed().toList());
    }

    public static PropertyArray of(boolean... values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0) {
            return EMPTY;
        }
        var list = new ArrayList<Object>(values.length);
        for (var v : values) {
            list.add(v);
        }
        return new PropertyArray(List.copyOf(list));
    }

    public static PropertyArray of(IngestEventProperties... values) {
        Objects.requireNonNull(values, "values cannot be null");
        return fromObjects(values);
    }

    private static <T> PropertyArray fromObjects(T[] values) {
        if (values.length == 0) {
            return EMPTY;
        }
        var list = new ArrayList<>(values.length);
        for (var v : values) {
            list.add(Objects.requireNonNull(v, "array element cannot be null"));
        }
        return new PropertyArray(List.copyOf(list));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertyArray other)) {
            return false;
        }
        return elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
