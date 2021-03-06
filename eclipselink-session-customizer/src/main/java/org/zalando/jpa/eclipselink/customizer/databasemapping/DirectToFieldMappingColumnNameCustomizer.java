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

import java.lang.reflect.Field;

import javax.persistence.Column;

import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.Session;
import org.zalando.jpa.eclipselink.customizer.NameUtils;
import org.zalando.jpa.eclipselink.customizer.databasemapping.support.EntityFieldInspector;

/**
 * Supports Column-Name-Customization for DirectToFieldMappings.
 *
 * @author  jbellmann
 */
public class DirectToFieldMappingColumnNameCustomizer extends AbstractColumnNameCustomizer<DirectToFieldMapping> {

    public DirectToFieldMappingColumnNameCustomizer() {
        super(DirectToFieldMapping.class);
    }

    // TODO, when we use Embeddables there is no tableName, it is null, how can we fix this
    @Override
    public void customizeColumnName(final String tableName, final DirectToFieldMapping databaseMapping,
            final Session session) {

        logDatabaseMapping(databaseMapping, session);

        String newFieldName = null;
        EntityFieldInspector<?> entityFieldInspector = FieldInspector.getFieldInspector(databaseMapping);
        if (shouldCreateBooleanFieldName(entityFieldInspector)) {

            newFieldName = NameUtils.buildBooleanFieldName(tableName, databaseMapping.getAttributeName());
        } else if (!entityFieldInspector.isNameValueSet()) {

            // default
            newFieldName = NameUtils.buildFieldName(tableName, databaseMapping.getAttributeName());
        } else if (entityFieldInspector.isNameValueSet()) {

            // column-annotation name value is set
            newFieldName = NameUtils.buildFieldName(tableName, databaseMapping.getField().getName());
        }

        databaseMapping.getField().setName(newFieldName);
        logFine(session, "set new field-name to {0}", newFieldName);

    }

    protected boolean shouldCreateBooleanFieldName(final EntityFieldInspector<?> entityFieldInspector) {
        if (Boolean.class.equals(entityFieldInspector.getFieldType())
                || boolean.class.equals(entityFieldInspector.getFieldType())) {
            if (entityFieldInspector.isNameValueSet()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    protected String getColumnAnnotationNameValue(final Field field) {
        String result = "";
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            result = columnAnnotation.name();
        }

        return result;
    }

//    // TODO, check this, we have the same in AbstractColumnNameCustomizer
//    protected EntityFieldInspector<? extends Annotation> getFieldInspector(final DirectToFieldMapping databaseMapping) {
//        final String attributeName = databaseMapping.getAttributeName();
//        final Class<?> entityClass = databaseMapping.getDescriptor().getJavaClass();
//
//        Set<Field> fieldsWithName = ReflectionUtils.getAllFields(entityClass, ReflectionUtils.withName(attributeName));
//        
//        final Field field = Iterables.get(fieldsWithName, 0);
////        final Field field = ReflectionUtils.findField(entityClass, attributeName);
//        return new ColumnFieldInspector(field);
//    }

}
