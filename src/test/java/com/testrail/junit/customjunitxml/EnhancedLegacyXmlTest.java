/*
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.joox.JOOX.$;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.joox.Match;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EnhancedLegacyXmlTest {

    private static final Class TEST_EXAMPLES_CLASS = ExamplesTestRailEnabledTestExamples.class;
    private static final Runnable FAILING_BLOCK = () -> fail("should fail");
    private static final Runnable SUCCEEDING_TEST = () -> {
    };

	@TempDir
	Path tempDirectory;
    private static final String REPORT_NAME = "TEST-junit-jupiter.xml";


    @Test
    public void shouldSupportCustomReportNames() throws Exception {
        String testMethodName = "legacyTest";

        String customProperties = "report_filename=custom-report-junit\n# report_directory=reports\n# add_timestamp_to_report_filename=true\n";
        Path customPropertiesFile = Files.createTempFile("testrail-junit-extensions", ".properties");
        Files.write(customPropertiesFile, customProperties.getBytes());
        executeTestMethodWithCustomProperties(TEST_EXAMPLES_CLASS, testMethodName, customPropertiesFile, "", Clock.systemDefaultZone());

        String reportName = "custom-report-junit.xml";
        Match testsuite = readValidXmlFile(tempDirectory.resolve(reportName));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_id")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_key")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements")).isEmpty();
    }

    @Test
    public void shouldSupportCustomReportNamesWithTimestamp() throws Exception {
        String testMethodName = "legacyTest";
        String customProperties = "report_filename=custom-report-junit\n# report_directory=reports\nadd_timestamp_to_report_filename=true\n";
        Path customPropertiesFile = Files.createTempFile("testrail-junit-extensions", ".properties");
        Files.write(customPropertiesFile, customProperties.getBytes());

        String fakeTimestamp = "2021-03-24T12:01:02.456";
        LocalDateTime now = LocalDateTime.parse(fakeTimestamp);
        ZoneId zone = ZoneId.of("UTC");
        Clock clock = Clock.fixed(ZonedDateTime.of(now, zone).toInstant(), zone);

        executeTestMethodWithCustomProperties(TEST_EXAMPLES_CLASS, testMethodName, customPropertiesFile, "", clock);

        String reportName = "custom-report-junit-2021_03_24-12_01_02_456.xml";
        Match testsuite = readValidXmlFile(tempDirectory.resolve(reportName));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "case_id")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_key")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements")).isEmpty();
    }

    @Test
    public void shouldSupportCustomReportAbsoluteDirectory() throws Exception {
        String testMethodName = "legacyTest";

        String customReportDir = tempDirectory.resolve("custom_reports").toString();
        String customProperties = "#report_filename=custom-report-junit\nreport_directory=" + customReportDir.replace("\\", "/") + "\n# add_timestamp_to_report_filename=true\n";
        System.out.println(customProperties);
        Path customPropertiesFile = Files.createTempFile("testrail-junit-extensions", ".properties");
        Files.write(customPropertiesFile, customProperties.getBytes());
        executeTestMethodWithCustomProperties(TEST_EXAMPLES_CLASS, testMethodName, customPropertiesFile, "", Clock.systemDefaultZone());
        
        Match testsuite = readValidXmlFile(tempDirectory.resolve("custom_reports").resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "case_id")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_key")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements")).isEmpty();
    }

    @Test
    public void shouldSupportCustomReportRelativeDirectory() throws Exception {
        String testMethodName = "legacyTest";
        String relativeCustomReportDir = "target/custom_reports";
        String customProperties = "#report_filename=custom-report-junit\nreport_directory=" + relativeCustomReportDir.replace("\\", "/") + "\n# add_timestamp_to_report_filename=true\n";
        Path customPropertiesFile = Files.createTempFile("testrail-junit-extensions", ".properties");
        Files.write(customPropertiesFile, customProperties.getBytes());
        executeTestMethodWithCustomProperties(TEST_EXAMPLES_CLASS, testMethodName, customPropertiesFile, "", Clock.systemDefaultZone());
        
        Match testsuite = readValidXmlFile(FileSystems.getDefault().getPath(".").resolve(relativeCustomReportDir).resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "case_id")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_key")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements")).isEmpty();
    }

    @Test
    public void simpleTestShouldNotHaveCustomProperties() throws Exception {
        String testMethodName = "legacyTest";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);

        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "case_id")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_key")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags")).isEmpty();
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements")).isEmpty();
    }

    @Test
    public void testsShouldHaveStartAndFinishTimestamps() throws Exception {
        String testMethodName = "legacyTest";

        String fakeTimestamp = "2021-03-24T12:01:02.456";
        LocalDateTime now = LocalDateTime.parse(fakeTimestamp);
		ZoneId zone = ZoneId.of("UTC");
        Clock clock = Clock.fixed(ZonedDateTime.of(now, zone).toInstant(), zone);

        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName, clock);
        
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        //assertThat(testcase.attr("started-at")).isNotEmpty();
        assertThat(testcase.attr("started-at")).isEqualTo(fakeTimestamp);
        //assertThat(testcase.attr("finished-at")).isNotEmpty();
        assertThat(testcase.attr("finished-at")).isEqualTo(fakeTimestamp);
    }

    @Test
    public void shouldMapTestIdToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedTestWithCustomId";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_id").attr("value")).isEqualTo("myCustomId");
    }

    @Test
    public void shouldMapTestSummaryToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedTestWithSummary";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        //System.out.println(tempDirectory.resolve(REPORT_NAME));
        //Thread.sleep(10000);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary").attr("value")).isEqualTo("custom summary");
    }

    @Test
    public void shouldMapTestDescriptionToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedTestWithDescription";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        //System.out.println(tempDirectory.resolve(REPORT_NAME));
        //Thread.sleep(10000);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_description").cdata()).isEqualTo("custom description");
    }

    @Test
    public void shouldMapOneRequirementToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedWithOneRequirement";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements").attr("value")).isEqualTo("CALC-123");
    }

    @Test
    public void shouldMapMultipleRequirementsToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedWithMultipleRequirements";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "requirements").attr("value")).isEqualTo("CALC-123,CALC-124");
    }

    @Test
    public void shouldMapOneTagToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedWithOneTag";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags").attr("value")).isEqualTo("tag1");
    }

    @Test
    public void shouldMapMultipleTagsToTestcaseProperty() throws Exception {
        String testMethodName = "annotatedWithMultipleTags";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "tags").attr("value")).isEqualTo("tag1,tag2");
    }

    private void executeTestMethod(Class<?> testClass, String methodName, Clock clock) {
        executeTestMethodWithParams(testClass, methodName, "", clock);      
    }

    private void executeTestMethod(Class<?> testClass, String methodName) {
        executeTestMethodWithParams(testClass, methodName, "");      
    }

    private void executeTestMethodWithCustomProperties(Class<?> testClass, String methodName, Path propertiesPath,  String methodParameterTypes, Clock clock) {
        LauncherDiscoveryRequest discoveryRequest = request()//
                .selectors(selectMethod(testClass, methodName, ""))
                .build();
        Launcher launcher = LauncherFactory.create();
        EnhancedLegacyXmlReportGeneratingListener listener = new EnhancedLegacyXmlReportGeneratingListener(tempDirectory, propertiesPath, new PrintWriter(System.out), clock);
        launcher.execute(discoveryRequest, listener);
    }

    private void executeTestMethodWithParams(Class<?> testClass, String methodName, String methodParameterTypes) {
        LauncherDiscoveryRequest discoveryRequest = request()//
                .selectors(selectMethod(testClass, methodName, methodParameterTypes))
                .build();
        Launcher launcher = LauncherFactory.create();
        EnhancedLegacyXmlReportGeneratingListener listener = new EnhancedLegacyXmlReportGeneratingListener(tempDirectory, new PrintWriter(System.out));
        //launcher.registerTestExecutionListeners(listener);
        launcher.execute(discoveryRequest, listener);        
    }

    private void executeTestMethodWithParams(Class<?> testClass, String methodName, String methodParameterTypes, Clock clock) {
        LauncherDiscoveryRequest discoveryRequest = request()//
                .selectors(selectMethod(testClass, methodName, methodParameterTypes))
                .build();
        Launcher launcher = LauncherFactory.create();
        EnhancedLegacyXmlReportGeneratingListener listener = new EnhancedLegacyXmlReportGeneratingListener(tempDirectory, new PrintWriter(System.out), clock);
        //launcher.registerTestExecutionListeners(listener);
        launcher.execute(discoveryRequest, listener);        
    }

    @Test
    public void shouldMapDisplayNameToTestSummaryProperty() throws Exception {
        String testMethodName = "annotatedWithDisplayName";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary").attr("value")).isEqualTo("custom name");
    }

    @Test
    public void shouldMapDisplayNameInParameterizedTestToTestSummaryProperty() throws Exception {
        String testMethodName = "parameterizedTestAnnotatedWithDisplayName";
        executeTestMethodWithParams(TEST_EXAMPLES_CLASS, testMethodName, "int");
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary").attr("value")).isEqualTo("custom name");
    }

    @Test
    public void shouldNotMapMethodNameInParameterizedTestToTestSummaryProperty() throws Exception {
        String testMethodName = "parameterizedTestWithoutDisplayName";
        executeTestMethodWithParams(TEST_EXAMPLES_CLASS, testMethodName, "int");
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty(); // isEqualTo("parameterizedTestWithoutDisplayName");
    }

    @Test
    public void shouldMapDisplayNameInRepeatedTestToTestSummaryProperty() throws Exception {
        String testMethodName = "repeatedTestAnnotatedWithDisplayName";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary").attr("value")).isEqualTo("custom name");
    }

    @Test
    public void shouldNotMapMethodNameInRepeatedTestToTestSummaryProperty() throws Exception {
        String testMethodName = "repeatedTestAnnotatedWithoutDisplayName";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary")).isEmpty(); // isEqualTo("parameterizedTestWithoutDisplayName");
    }


    @Test
    public void shouldMapDisplayNameInDynamicTestToTestSummaryProperty() throws Exception {
        String testMethodName = "dynamicTestsFromCollection";
        executeTestMethod(TEST_EXAMPLES_CLASS, testMethodName);
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "test_summary").attr("value")).isEqualTo("1st dynamic test");
    }

    @Test
    public void shouldStoreTestRunProperties() throws Exception {
        String testMethodName = "testWithTestRunProperty";
        executeTestMethodWithParams(TEST_EXAMPLES_CLASS, testMethodName, "com.testrail.junit.customjunitxml.TestRailTestReporter");
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "my_property").attr("value")).isEqualTo("hello");
    }

    @Test
    public void shouldStoreTestRunPropertiesMultiple() throws Exception {
        String testMethodName = "testWithMultipleTestRunProperties";
        executeTestMethodWithParams(TEST_EXAMPLES_CLASS, testMethodName, "com.testrail.junit.customjunitxml.TestRailTestReporter");
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "my_property1").attr("value")).isEqualTo("hello");
        assertThat(testcase.child("properties").children("property").matchAttr("name", "my_property2").attr("value")).isEqualTo("world");
    }

    @Test
    public void shouldStoreMultilineTestRunProperties() throws Exception {
        String testMethodName = "testWithTestRunPropertyMultiline";
        executeTestMethodWithParams(TEST_EXAMPLES_CLASS, testMethodName, "com.testrail.junit.customjunitxml.TestRailTestReporter");
        dumpJunitXMLReport(tempDirectory.resolve(REPORT_NAME));
        Match testsuite = readValidXmlFile(tempDirectory.resolve(REPORT_NAME));
        Match testcase = testsuite.child("testcase");
        assertThat(testcase.attr("name", String.class)).isEqualTo(testMethodName);
        assertThat(testcase.child("properties").children("property").matchAttr("name", "testrail_case_field").attr("value")).isEqualTo("custom_steps:1. First step&#10;2. Second step&#10;3. Third step");
    }

	private Match readValidXmlFile(Path xmlFile) throws Exception {
		assertTrue(Files.exists(xmlFile), () -> "File does not exist: " + xmlFile);
		try (BufferedReader reader = Files.newBufferedReader(xmlFile)) {
			Match xml = $(reader);
            assertValidAccordingToJenkinsSchema(xml.document());
			return xml;
		}
	}

	static void assertValidAccordingToJenkinsSchema(Document document) throws Exception {
		try {
			// Schema is thread-safe, Validator is not
			Validator validator = CachedSchema.JENKINS.newValidator();
			validator.validate(new DOMSource(document));
		}
		catch (Exception e) {
			fail("Invalid XML document: " + document, e);
		}
	}

    private void dumpJunitXMLReport(Path reportPath) {
        System.out.println("Junit XML report: " + reportPath);
        try {
            Files.lines(reportPath).forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private enum CachedSchema {

		JENKINS("/enhanced-jenkins-junit.xsd");

		private final Schema schema;

		CachedSchema(String resourcePath) {
			URL schemaFile = EnhancedLegacyXmlReportGeneratingListener.class.getResource(resourcePath);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				this.schema = schemaFactory.newSchema(schemaFile);
			}
			catch (SAXException e) {
				throw new RuntimeException("Failed to create schema using " + schemaFile, e);
			}
		}

		Validator newValidator() {
			return schema.newValidator();
		}
	}

}
