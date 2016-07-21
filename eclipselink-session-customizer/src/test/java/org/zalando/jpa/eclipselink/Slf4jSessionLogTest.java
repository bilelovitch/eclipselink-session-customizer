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
package org.zalando.jpa.eclipselink;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.eclipse.persistence.logging.SessionLogEntry;

import org.hamcrest.CoreMatchers;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PrepareForTest;

import org.powermock.modules.junit4.PowerMockRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PrepareForTest(LoggerFactory.class)
@RunWith(PowerMockRunner.class)
public class Slf4jSessionLogTest {

    @Test
    public void testThatDetailedSqlExceptionLoggingWorks() {

        // Some setup. We have to test the logging - therefore we use
        // PowerMockito to hack our way into the SLF4J LoggerFactory
        Logger logger = Mockito.mock(Logger.class);

        PowerMockito.mockStatic(LoggerFactory.class);
        Mockito.when(LoggerFactory.getLogger(Mockito.anyString())).thenReturn(logger);

        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        Slf4jSessionLog slf4jSessionLog = new Slf4jSessionLog();
        slf4jSessionLog.setShouldLogExceptionStackTrace(true);
        slf4jSessionLog.setLevel(0);

        // given
        SQLException cause = new SQLException("SQLException cause");
        SQLException nextSqlException = new SQLException("SQLException nextSqlException");
        cause.setNextException(nextSqlException);

        Exception outer = new Exception("Exception outer", cause);
        SessionLogEntry sessionLogEntry = new SessionLogEntry(null, outer);

        // when
        slf4jSessionLog.log(sessionLogEntry);

        // then
        verify(logger, Mockito.times(3)).error(stringCaptor.capture(), exceptionCaptor.capture());

        assertThat((Exception) exceptionCaptor.getAllValues().get(0), equalTo(outer));
        assertThat(stringCaptor.getAllValues().get(0), startsWith("[EL]"));
        assertThat((SQLException) exceptionCaptor.getAllValues().get(1), equalTo(cause));
        assertThat(stringCaptor.getAllValues().get(1), equalTo("[SQLException nextException]"));
        assertThat((SQLException) exceptionCaptor.getAllValues().get(2), CoreMatchers.equalTo(nextSqlException));
        assertThat(stringCaptor.getAllValues().get(2), equalTo("[SQLException nextException]"));

    }

}
