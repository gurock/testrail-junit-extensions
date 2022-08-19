/*
 * Copyright 2021-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package com.testrail.junit.customjunitxml;

public interface TestRailTestReporter {

    String TESTRAIL_PREFIX = "testrail:";
    String TESTRUN_PROPERTY = "testrail:property";

    void setProperty(String name, String value);
}
