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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Context API v2 process request. Actions are structured as a map of action name to per-action configuration, following
 * the v2 format where each action can carry its own classes, instructions, and other parameters.
 *
 * @since 1.0.0
 */
public final class ProcessRequest {

    public static final String VERSION_V2 = "context.api/v2";

    private final String version;

    private final List<ObjectKeyPath> objectKeys;

    private final Map<String, ActionConfig> actions;

    private ProcessRequest(Builder builder) {
        this.version = builder.version;
        this.objectKeys = List.copyOf(builder.objectKeys);
        this.actions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.actions));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String version() {
        return version;
    }

    public List<ObjectKeyPath> objectKeys() {
        return objectKeys;
    }

    /**
     * Returns the actions map where each key is the camelCase action name and the value is the per-action
     * configuration.
     */
    public Map<String, ActionConfig> actions() {
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProcessRequest that = (ProcessRequest) obj;
        return Objects.equals(version, that.version) && Objects.equals(objectKeys, that.objectKeys)
                && Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, objectKeys, actions);
    }

    public static final class Builder {

        private String version = VERSION_V2;

        private final List<ObjectKeyPath> objectKeys = new ArrayList<>();

        private final Map<String, ActionConfig> actions = new LinkedHashMap<>();

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder objectKey(String path) {
            objectKeys.add(new ObjectKeyPath(path));
            return this;
        }

        public Builder objectKeys(List<ObjectKeyPath> objectKeys) {
            this.objectKeys.addAll(objectKeys);
            return this;
        }

        /**
         * Adds an action with empty configuration.
         */
        public Builder action(Action action) {
            actions.put(action.value(), ActionConfig.empty());
            return this;
        }

        /**
         * Adds an action with a pre-built configuration.
         */
        public Builder action(Action action, ActionConfig config) {
            actions.put(action.value(), config);
            return this;
        }

        /**
         * Adds an action with configuration built via a consumer.
         */
        public Builder action(Action action, Consumer<ActionConfig.Builder> consumer) {
            var builder = ActionConfig.builder();
            consumer.accept(builder);
            actions.put(action.value(), builder.build());
            return this;
        }

        /**
         * Adds an action by name with empty configuration.
         */
        public Builder action(String actionName) {
            actions.put(actionName, ActionConfig.empty());
            return this;
        }

        /**
         * Adds an action by name with a pre-built configuration.
         */
        public Builder action(String actionName, ActionConfig config) {
            actions.put(actionName, config);
            return this;
        }

        /**
         * Adds an action by name with configuration built via a consumer.
         */
        public Builder action(String actionName, Consumer<ActionConfig.Builder> consumer) {
            var builder = ActionConfig.builder();
            consumer.accept(builder);
            actions.put(actionName, builder.build());
            return this;
        }

        /**
         * Copies all actions from the given map.
         */
        public Builder actions(Map<String, ActionConfig> actions) {
            this.actions.putAll(actions);
            return this;
        }

        public ProcessRequest build() {
            if (objectKeys.isEmpty()) {
                throw new IllegalArgumentException("At least one objectKey is required");
            }
            return new ProcessRequest(this);
        }
    }
}
