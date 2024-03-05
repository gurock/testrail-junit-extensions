package com.testrail.junit.customjunitxml;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.testrail.junit.customjunitxml.annotations.Requirement;
import com.testrail.junit.customjunitxml.annotations.TestRail;

import org.junit.jupiter.api.DynamicTest;

@ExtendWith(TestRailTestReporterParameterResolver.class)
public class ExamplesTestRailEnabledTestExamples {

    @Test
    public void legacyTest() {
    }

    @Test
    public void testWithTestRunProperty(TestRailTestReporter customReporter) {
        customReporter.setProperty("my_property", "hello");
    }

    @Test
    public void testWithMultipleTestRunProperties(TestRailTestReporter customReporter) {
        customReporter.setProperty("my_property1", "hello");
        customReporter.setProperty("my_property2", "world");
    }

    @Test
    @TestRail(id = "myCustomId")
    public void annotatedTestWithCustomId() {
        fail("this should have id: myCustomId");
    }
    
    @Test
    @TestRail(summary = "custom summary")
    public void annotatedTestWithSummary() {
        //
    }

    @Test
    @TestRail(description = "custom description")
    public void annotatedTestWithDescription() {
        //
    }

    @Test
    @Requirement("CALC-123")
    public void annotatedWithOneRequirement() {
        fail("this covering a requirement");
    }

    @Test
    @Requirement({"CALC-123", "CALC-124"})
    public void annotatedWithMultipleRequirements() {
        fail("this covering multiple requirements");
    }

    @Test
    @Tag("tag1")
    public void annotatedWithOneTag() {
        fail("tagged with one tag");
    }

    @Test
    @Tag("tag1")
    @Tag("tag2")
    public void annotatedWithMultipleTags() {
        fail("tagged with multiple tags");
    }

    @Test
    @DisplayName("custom name")
    public void annotatedWithDisplayName() {
        fail("with DisplayName");
    }

    @DisplayName("custom name")
    @ParameterizedTest
    @ValueSource(ints = {-1,0,5,-1001})
    public void parameterizedTestAnnotatedWithDisplayName(int number)
    {
        fail("with DisplayName");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {-1,0,5,-1001})
    public void parameterizedTestWithoutDisplayName(int number)
    {
        fail("without DisplayName");
    }

    @DisplayName("custom name")
    @RepeatedTest(3)
    void repeatedTestAnnotatedWithDisplayName() {
        fail("with DisplayName");
    }

    @RepeatedTest(3)
    void repeatedTestAnnotatedWithoutDisplayName() {
        fail("without DisplayName");
    }

    @TestFactory
    Collection<DynamicTest> dynamicTestsFromCollection() {
        return Arrays.asList(
            dynamicTest("1st dynamic test", () -> assertTrue(true))
        );
    }
}