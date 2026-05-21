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

import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * @since 1.0.0
 */
public class NucleusMapperFactory implements MapperService.MapperFactory {

    private static final List<Map.Entry<Class<?>, CICMapper<?>>> MAPPERS = List.of(
            Map.entry(Attribute.ListOf.class, new AttributeMapper.ListMapper()),
            Map.entry(Attribute.class, new AttributeMapper()),
            Map.entry(AttributeInput.ListOf.class, new AttributeInputMapper.ListMapper()),
            Map.entry(AttributeInput.class, new AttributeInputMapper()),
            Map.entry(SystemOutput.PaginatedListOf.class, new SystemOutputMapper.PaginatedListMapper()),
            Map.entry(SystemOutput.class, new SystemOutputMapper()),
            Map.entry(GroupOutput.PaginatedListOf.class, new GroupOutputMapper.PaginatedListMapper()),
            Map.entry(GroupOutput.class, new GroupOutputMapper()),
            Map.entry(GroupMember.PaginatedListOf.class, new GroupMemberMapper.PaginatedListMapper()),
            Map.entry(GroupMember.class, new GroupMemberMapper()),
            Map.entry(UserMapping.PaginatedListOf.class, new UserMappingMapper.PaginatedListMapper()),
            Map.entry(UserMapping.class, new UserMappingMapper()),
            Map.entry(PrincipalUserMapping.PaginatedListOf.class, new PrincipalUserMappingMapper.PaginatedListMapper()),
            Map.entry(PrincipalUserMapping.class, new PrincipalUserMappingMapper()),
            Map.entry(PrincipalUserMembership.PaginatedListOf.class,
                    new PrincipalUserMembershipMapper.PaginatedListMapper()),
            Map.entry(PrincipalUserMembership.class, new PrincipalUserMembershipMapper()),
            Map.entry(GroupCreateInput.ListOf.class, new GroupCreateInputMapper.ListMapper()),
            Map.entry(GroupCreateInput.class, new GroupCreateInputMapper()),
            Map.entry(GroupMemberAssignmentInput.ListOf.class, new GroupMemberAssignmentInputMapper.ListMapper()),
            Map.entry(GroupMemberAssignmentInput.class, new GroupMemberAssignmentInputMapper()),
            Map.entry(UserMappingCreateInput.ListOf.class, new UserMappingCreateInputMapper.ListMapper()),
            Map.entry(UserMappingCreateInput.class, new UserMappingCreateInputMapper()),
            Map.entry(UserMappingReplaceInput.class, new UserMappingReplaceInputMapper()));

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        for (var entry : MAPPERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return (CICMapper<T>) entry.getValue();
            }
        }
        return null;
    }
}
