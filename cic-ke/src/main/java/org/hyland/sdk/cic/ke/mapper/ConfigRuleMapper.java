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
package org.hyland.sdk.cic.ke.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.ConfigRule;

/**
 * @since 1.1.0
 */
class ConfigRuleMapper implements CICMapper<ConfigRule> {

    private final ProcessingOptionsMapper optionsMapper = new ProcessingOptionsMapper();

    private final RuleConditionMapper conditionMapper = new RuleConditionMapper();

    @Override
    public ConfigRule fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var builder = ConfigRule.builder();

        cicObject.getOptionalString("id").ifPresent(builder::id);
        cicObject.getOptionalString("name").ifPresent(builder::name);

        builder.conditions(
                cicObject.getOptionalArray("conditions")
                         .map(arr -> arr.toListObject().stream().map(conditionMapper::fromCICObject).toList())
                         .orElseGet(List::of));

        cicObject.getOptionalObject("config").ifPresent(configObj -> {
            builder.config(optionsMapper.fromCICNode(configObj));
        });

        return builder.build();
    }

    @Override
    public CICNode toCICNode(ConfigRule rule) {
        var cicObject = CICObject.create();

        if (rule.name() != null) {
            cicObject.putString("name", rule.name());
        }

        if (!rule.conditions().isEmpty()) {
            var conditionsArray = CICArray.create();
            for (var condition : rule.conditions()) {
                var condObj = CICObject.create();
                condObj.putString("field", condition.field());
                condObj.putString("value", condition.value());
                conditionsArray.addObject(condObj);
            }
            cicObject.putArray("conditions", conditionsArray);
        }

        if (rule.config() != null) {
            cicObject.putNode("config", optionsMapper.toCICNode(rule.config()));
        }

        return cicObject;
    }

    static class ListMapper implements CICMapper<ConfigRule.ListOf> {

        private final ConfigRuleMapper innerMapper = new ConfigRuleMapper();

        @Override
        public ConfigRule.ListOf fromCICNode(CICNode cicNode) {
            var cicArray = (CICArray) cicNode;
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(ConfigRule.ListOf::new));
        }
    }
}
