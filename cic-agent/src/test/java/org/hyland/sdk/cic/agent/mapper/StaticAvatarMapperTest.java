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
package org.hyland.sdk.cic.agent.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class StaticAvatarMapperTest {

    @Test
    void testDeserializeStaticAvatarList() {
        var json = """
                [{"fileName": "example.png", "preSignedUrl": "https://localhost/avatars/example.png"}]
                """;

        var avatars = MapperService.read(json, StaticAvatar.ListOf.class);

        assertEquals(1, avatars.size());
        assertEquals("example.png", avatars.get(0).fileName());
        assertEquals("https://localhost/avatars/example.png", avatars.get(0).preSignedUrl());
    }

    @Test
    void testDeserializeStaticAvatarListEmpty() {
        var avatars = MapperService.read("[]", StaticAvatar.ListOf.class);

        assertNotNull(avatars);
        assertTrue(avatars.isEmpty());
    }

    @Test
    void testDeserializeStaticAvatarListMultiple() {
        var json = """
                [
                  {"fileName": "Blue-Gold.png", "preSignedUrl": "https://localhost/avatars/Blue-Gold.png"},
                  {"fileName": "FishTeal.svg", "preSignedUrl": "https://localhost/avatars/FishTeal.svg"},
                  {"fileName": "AquaWave.jpg", "preSignedUrl": null}
                ]
                """;

        var avatars = MapperService.read(json, StaticAvatar.ListOf.class);

        assertEquals(3, avatars.size());
        assertEquals("Blue-Gold.png", avatars.get(0).fileName());
        assertEquals("https://localhost/avatars/Blue-Gold.png", avatars.get(0).preSignedUrl());
        assertEquals("FishTeal.svg", avatars.get(1).fileName());
        assertEquals("AquaWave.jpg", avatars.get(2).fileName());
        assertNull(avatars.get(2).preSignedUrl());
    }

    @Test
    void testMapperFactory() {
        var factory = new AgentMapperFactory();
        CICMapper<StaticAvatar.ListOf> mapper = factory.getMapper(StaticAvatar.ListOf.class);
        assertNotNull(mapper);
        assertInstanceOf(StaticAvatarMapper.ListMapper.class, mapper);
    }
}
