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

import org.hyland.sdk.cic.agent.object.AgentAvatar;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class AgentAvatarMapperTest {

    @Test
    void testDeserializeAgentAvatarList() {
        var json = """
                [
                  {
                    "agentId": "31a01094-e01a-4cc5-830b-ccca11e07a49",
                    "avatarUrl": "https://localhost/avatars/example.png"
                  },
                  {
                    "agentId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                    "avatarUrl": "https://localhost/avatars/other.png"
                  }
                ]
                """;

        var avatars = MapperService.read(json, AgentAvatar.List.class);

        assertEquals(2, avatars.size());
        assertEquals("31a01094-e01a-4cc5-830b-ccca11e07a49", avatars.get(0).agentId());
        assertEquals("https://localhost/avatars/example.png", avatars.get(0).avatarUrl());
        assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", avatars.get(1).agentId());
        assertEquals("https://localhost/avatars/other.png", avatars.get(1).avatarUrl());
    }

    @Test
    void testDeserializeAgentAvatarListEmpty() {
        var avatars = MapperService.read("[]", AgentAvatar.List.class);

        assertNotNull(avatars);
        assertTrue(avatars.isEmpty());
    }

    @Test
    void testDeserializeAgentAvatarWithNullAvatarUrl() {
        var json = """
                [{"agentId": "31a01094-e01a-4cc5-830b-ccca11e07a49", "avatarUrl": null}]
                """;

        var avatars = MapperService.read(json, AgentAvatar.List.class);

        assertEquals(1, avatars.size());
        assertEquals("31a01094-e01a-4cc5-830b-ccca11e07a49", avatars.get(0).agentId());
        assertNull(avatars.get(0).avatarUrl());
    }

    @Test
    void testMapperFactory() {
        var factory = new AgentMapperFactory();
        CICMapper<AgentAvatar.List> mapper = factory.getMapper(AgentAvatar.List.class);
        assertNotNull(mapper);
        assertInstanceOf(AgentAvatarMapper.ListMapper.class, mapper);
    }
}
