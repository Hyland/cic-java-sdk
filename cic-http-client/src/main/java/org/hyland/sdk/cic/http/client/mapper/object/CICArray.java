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
package org.hyland.sdk.cic.http.client.mapper.object;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0.0
 */
public interface CICArray extends CICNode {

    CICArray getArray(int index);

    boolean getBoolean(int index);

    int getInt(int index);

    long getLong(int index);

    CICObject getObject(int index);

    String getString(int index);

    void addArray(CICArray value);

    void addBoolean(boolean value);

    void addInt(int value);

    void addLong(long value);

    void addObject(CICObject value);

    void addString(String value);

    List<CICObject> toListObject();

    static CICArray create() {
        var array = new ArrayList<CICNode>();
        return new CICArray() {

            @Override
            public CICArray getArray(int index) {
                return (CICArray) array.get(index);
            }

            @Override
            public boolean getBoolean(int index) {
                return ((CICPrimitive.CICBoolean) array.get(index)).value();
            }

            @Override
            public int getInt(int index) {
                return ((CICPrimitive.CICInt) array.get(index)).value();
            }

            @Override
            public long getLong(int index) {
                return ((CICPrimitive.CICLong) array.get(index)).value();
            }

            @Override
            public CICObject getObject(int index) {
                return (CICObject) array.get(index);
            }

            @Override
            public String getString(int index) {
                return ((CICPrimitive.CICString) array.get(index)).value();
            }

            @Override
            public void addArray(CICArray value) {
                array.add(value);
            }

            @Override
            public void addBoolean(boolean value) {
                array.add(new CICPrimitive.CICBoolean(value));
            }

            @Override
            public void addInt(int value) {
                array.add(new CICPrimitive.CICInt(value));
            }

            @Override
            public void addLong(long value) {
                array.add(new CICPrimitive.CICLong(value));
            }

            @Override
            public void addObject(CICObject value) {
                array.add(value);
            }

            @Override
            public void addString(String value) {
                array.add(new CICPrimitive.CICString(value));
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<CICObject> toListObject() {
                return (List<CICObject>) ((List<?>) array);
            }
        };
    }
}
