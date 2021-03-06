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
package org.zalando.jpa.eclipselink.customizer.databasemapping.support;

import java.sql.SQLException;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import org.postgresql.util.PGobject;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class EnumTypeConverter implements Converter {
    private static final long serialVersionUID = -3691595677010300063L;

    private Class<Enum> enumClass;
    private String pgTypeName;

    public EnumTypeConverter() {
        //
    }

    public EnumTypeConverter(final Class clazz, final String columnDefinition) {
        Preconditions.checkNotNull(clazz, "Class should never be null");
        this.enumClass = clazz;
        this.pgTypeName = columnDefinition;
        checkPgTypeName();
    }

    @Override
    public Object convertObjectValueToDataValue(final Object objectValue, final Session session) {

        // we will transfer a PGobject.
        final PGobject object = new PGobject();
        try {
            if (objectValue == null) {
                object.setValue(null);
            } else {
                object.setValue(((Enum) objectValue).name());
            }

            object.setType(pgTypeName);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        return object;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convertDataValueToObjectValue(final Object dataValue, final Session session) {

        if (dataValue == null) {
            return null;
        }

        if (dataValue instanceof PGobject) {
            final PGobject object = (PGobject) dataValue;
            if (object.getValue() == null) {
                return null;
            }

            return Enum.valueOf(enumClass, object.getValue());
        }

        throw new RuntimeException("Unknown dataValue type: " + dataValue);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(final DatabaseMapping mapping, final Session session) {
        enumClass = mapping.getAttributeClassification();
        pgTypeName = mapping.getField().getColumnDefinition();
        checkPgTypeName();
// if (Strings.isNullOrEmpty(pgTypeName)) {
// pgTypeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, enumClass.getSimpleName());
// }
    }

    protected void checkPgTypeName() {
        if (Strings.isNullOrEmpty(pgTypeName)) {
            pgTypeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, enumClass.getSimpleName());
        }
    }
}
