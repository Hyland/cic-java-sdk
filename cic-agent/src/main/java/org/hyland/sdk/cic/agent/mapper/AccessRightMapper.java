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

import org.hyland.sdk.cic.agent.object.AccessRight;
import org.hyland.sdk.cic.agent.object.PrincipalType;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AccessRightMapper implements CICMapper<AccessRight> {

    @Override
    public AccessRight fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new AccessRight(PrincipalType.fromValue(obj.getStringOrThrow("type")), obj.getStringOrNull("id"));
    }

    @Override
    public CICObject toCICNode(AccessRight accessRight) {
        var obj = CICObject.create();
        obj.putString("type", accessRight.type().value());
        if (accessRight.id() != null) {
            obj.putString("id", accessRight.id());
        }
        return obj;
    }
}
