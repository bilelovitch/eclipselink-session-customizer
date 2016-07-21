/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.jpa.eclipselink.customizer.databasemapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.reflections.ReflectionUtils;
import org.zalando.jpa.eclipselink.customizer.databasemapping.support.ColumnFieldInspector;
import org.zalando.jpa.eclipselink.customizer.databasemapping.support.EntityFieldInspector;

import com.google.common.collect.Iterables;

abstract class FieldInspector {

    // TODO, check this, we have the same in AbstractColumnNameCustomizer
    static EntityFieldInspector<? extends Annotation> getFieldInspector(final DatabaseMapping databaseMapping) {
        final String attributeName = databaseMapping.getAttributeName();
        final Class<?> entityClass = databaseMapping.getDescriptor().getJavaClass();

        Set<Field> fieldsWithName = ReflectionUtils.getAllFields(entityClass, ReflectionUtils.withName(attributeName));

        final Field field = Iterables.get(fieldsWithName, 0);
        // final Field field = ReflectionUtils.findField(entityClass,
        // attributeName);
        return new ColumnFieldInspector(field);
    }

}
