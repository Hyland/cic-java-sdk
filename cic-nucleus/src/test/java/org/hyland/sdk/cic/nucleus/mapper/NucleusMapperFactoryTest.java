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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberPage;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.GroupOutputPage;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;
import org.hyland.sdk.cic.nucleus.object.InteractiveUserPage;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMappingPage;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembershipPage;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.SystemOutputPage;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingPage;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * @since 1.0.0
 */
class NucleusMapperFactoryTest {

    private final NucleusMapperFactory factory = new NucleusMapperFactory();

    @Test
    void testGetMapperForAttribute() {
        assertInstanceOf(AttributeMapper.class, factory.getMapper(Attribute.class));
    }

    @Test
    void testGetMapperForInteractiveUserPage() {
        assertInstanceOf(InteractiveUserPageMapper.class, factory.getMapper(InteractiveUserPage.class));
    }

    @Test
    void testGetMapperForInteractiveUser() {
        assertInstanceOf(InteractiveUserMapper.class, factory.getMapper(InteractiveUser.class));
    }

    @Test
    void testGetMapperForAttributeListOf() {
        assertInstanceOf(AttributeMapper.ListMapper.class, factory.getMapper(Attribute.ListOf.class));
    }

    @Test
    void testGetMapperForSystemOutputPage() {
        assertInstanceOf(SystemOutputPageMapper.class, factory.getMapper(SystemOutputPage.class));
    }

    @Test
    void testGetMapperForGroupOutputPage() {
        assertInstanceOf(GroupOutputPageMapper.class, factory.getMapper(GroupOutputPage.class));
    }

    @Test
    void testGetMapperForGroupMemberPage() {
        assertInstanceOf(GroupMemberPageMapper.class, factory.getMapper(GroupMemberPage.class));
    }

    @Test
    void testGetMapperForUserMappingPage() {
        assertInstanceOf(UserMappingPageMapper.class, factory.getMapper(UserMappingPage.class));
    }

    @Test
    void testGetMapperForPrincipalUserMappingPage() {
        assertInstanceOf(PrincipalUserMappingPageMapper.class, factory.getMapper(PrincipalUserMappingPage.class));
    }

    @Test
    void testGetMapperForPrincipalUserMembershipPage() {
        assertInstanceOf(PrincipalUserMembershipPageMapper.class, factory.getMapper(PrincipalUserMembershipPage.class));
    }

    @Test
    void testGetMapperForSystemOutput() {
        assertInstanceOf(SystemOutputMapper.class, factory.getMapper(SystemOutput.class));
    }

    @Test
    void testGetMapperForGroupOutput() {
        assertInstanceOf(GroupOutputMapper.class, factory.getMapper(GroupOutput.class));
    }

    @Test
    void testGetMapperForGroupMember() {
        assertInstanceOf(GroupMemberMapper.class, factory.getMapper(GroupMember.class));
    }

    @Test
    void testGetMapperForUserMapping() {
        assertInstanceOf(UserMappingMapper.class, factory.getMapper(UserMapping.class));
    }

    @Test
    void testGetMapperForPrincipalUserMapping() {
        assertInstanceOf(PrincipalUserMappingMapper.class, factory.getMapper(PrincipalUserMapping.class));
    }

    @Test
    void testGetMapperForPrincipalUserMembership() {
        assertInstanceOf(PrincipalUserMembershipMapper.class, factory.getMapper(PrincipalUserMembership.class));
    }

    @Test
    void testGetMapperForUserMappingReplaceInput() {
        assertInstanceOf(UserMappingReplaceInputMapper.class, factory.getMapper(UserMappingReplaceInput.class));
    }

    @Test
    void testGetMapperReturnsNullForUnknownType() {
        assertNull(factory.getMapper(String.class));
    }
}
