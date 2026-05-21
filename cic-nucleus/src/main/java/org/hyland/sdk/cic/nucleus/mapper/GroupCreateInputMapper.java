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
package org.hyland.sdk.cic.nucleus.mapper;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;

/**
 * @since 1.0.0
 */
class GroupCreateInputMapper implements CICMapper<GroupCreateInput> {

    private final AttributeInputMapper attributeInputMapper = new AttributeInputMapper();

    @Override
    public CICNode toCICNode(GroupCreateInput input) {
        var obj = CICObject.create();
        obj.putString("externalGroupId", input.externalGroupId());
        if (input.attributes() != null) {
            var array = CICArray.create();
            input.attributes().forEach(attr -> array.addObject((CICObject) attributeInputMapper.toCICNode(attr)));
            obj.putArray("attributes", array);
        }
        return obj;
    }

    static class ListMapper implements CICMapper<GroupCreateInput.ListOf> {

        private final GroupCreateInputMapper innerMapper = new GroupCreateInputMapper();

        @Override
        public CICNode toCICNode(GroupCreateInput.ListOf list) {
            var array = CICArray.create();
            list.forEach(item -> array.addObject((CICObject) innerMapper.toCICNode(item)));
            return array;
        }
    }
}
