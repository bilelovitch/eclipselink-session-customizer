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
package org.zalando.jpa.eclipselink.customizer.classdescriptor;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.persistence.annotations.ChangeTrackingType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.descriptors.changetracking.DeferredChangeDetectionPolicy;
import org.eclipse.persistence.descriptors.changetracking.ObjectChangePolicy;
import org.eclipse.persistence.descriptors.changetracking.ObjectChangeTrackingPolicy;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zalando.jpa.eclipselink.PersistenceUnitProperties;
import org.zalando.jpa.eclipselink.Slf4jSessionLog;

/**
 * @author  ahartmann
 */
@RunWith(Parameterized.class)
public class ClassDescriptorCustomizeChangeTrackingPolicyTest {

    private ClassDescriptorCustomizer classDescriptorCustomizer;
    private Session sessionMock;
    private ClassDescriptor classDescriptorMock;

    private String changeTrackingTypePropertyValue;
    private Class<? extends ObjectChangePolicy> changePolicyClass;

    public ClassDescriptorCustomizeChangeTrackingPolicyTest(final String changeTrackingTypePropertyValue,
            final Class<? extends ObjectChangePolicy> changePolicyClass) {
        this.changeTrackingTypePropertyValue = changeTrackingTypePropertyValue;
        this.changePolicyClass = changePolicyClass;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        final ArrayList<Object[]> data = newArrayList();
        data.add(forPropertyValue(ChangeTrackingType.ATTRIBUTE.toString()).expect(AttributeChangeTrackingPolicy.class));
        data.add(forPropertyValue(ChangeTrackingType.DEFERRED.toString()).expect(DeferredChangeDetectionPolicy.class));
        data.add(forPropertyValue(ChangeTrackingType.OBJECT.toString()).expect(ObjectChangeTrackingPolicy.class));
        data.add(forPropertyValue(ChangeTrackingType.AUTO.toString()).expect(null));
        data.add(forPropertyValue("MURKS").expect(null));
        data.add(forPropertyValue(null).expect(null));
        return data;
    }

    @Before
    public void setUp() throws Exception {
        classDescriptorCustomizer = new ChangePolicyClassDescriptorCustomizer();

        final Slf4jSessionLog slf4jSessionLog = new Slf4jSessionLog();
        final Vector<DatabaseMapping> mappings = new Vector<>(0);

        classDescriptorMock = mock(ClassDescriptor.class);
        when(classDescriptorMock.getJavaClassName()).thenReturn("MySuperJavaClass");
        when(classDescriptorMock.getMappings()).thenReturn(mappings);

        sessionMock = mock(Session.class);
        when(sessionMock.getSessionLog()).thenReturn(slf4jSessionLog);
        when(sessionMock.getProperty(eq(PersistenceUnitProperties.ZALANDO_ECLIPSELINK_CHANGE_TRACKER_NAME)))
            .thenReturn(changeTrackingTypePropertyValue);
    }

    @Test
    public void testCustomizeChangeTrackingPolicy() throws Exception {
        classDescriptorCustomizer.customize(classDescriptorMock, sessionMock);

        if (changePolicyClass != null) {
            verify(classDescriptorMock, times(1)).setObjectChangePolicy(isA(changePolicyClass));
        } else {
            verify(classDescriptorMock, never()).setObjectChangePolicy(any(ObjectChangePolicy.class));
        }
    }

    private static ParameterBuilder forPropertyValue(final String propertyValue) {
        return new ParameterBuilder(propertyValue);
    }

    private static class ParameterBuilder {

        private final String propertyValue;

        public ParameterBuilder(final String propertyValue) {
            this.propertyValue = propertyValue;
        }

        public Object[] expect(final Class<? extends ObjectChangePolicy> clazz) {
            return new Object[] {propertyValue, clazz};
        }
    }
}
