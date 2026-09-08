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
import java.util.List;
import java.util.Objects;

/**
 * A configuration rule with conditions and processing config.
 *
 * @since 1.1.0
 */
public final class ConfigRule {

    private final String id;

    private final String name;

    private final List<RuleCondition> conditions;

    private final ProcessingOptions config;

    private ConfigRule(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.conditions = builder.conditions == null ? List.of() : List.copyOf(builder.conditions);
        this.config = builder.config;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<RuleCondition> conditions() {
        return conditions;
    }

    public ProcessingOptions config() {
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConfigRule that = (ConfigRule) obj;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(conditions, that.conditions) && Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, conditions, config);
    }

    /**
     * @since 1.1.0
     */
    public static class ListOf extends ArrayList<ConfigRule> {
    }

    public static final class Builder {

        private String id;

        private String name;

        private List<RuleCondition> conditions;

        private ProcessingOptions config;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder conditions(List<RuleCondition> conditions) {
            this.conditions = conditions;
            return this;
        }

        public Builder addCondition(String field, String value) {
            if (this.conditions == null) {
                this.conditions = new ArrayList<>();
            }
            this.conditions.add(new RuleCondition(field, value));
            return this;
        }

        public Builder config(ProcessingOptions config) {
            this.config = config;
            return this;
        }

        public ConfigRule build() {
            return new ConfigRule(this);
        }
    }
}
