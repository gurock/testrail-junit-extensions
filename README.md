# testrail-junit-extensions

[![build workflow](https://github.com/gurock/testrail-junit-extensions/actions/workflows/CI.yml/badge.svg)](https://github.com/gurock/testrail-junit-extensions/actions/workflows/CI.yml)
[![Known Vulnerabilities](https://snyk.io/test/github/gurock/testrail-junit-extensions/badge.svg)](https://snyk.io/test/github/gurock/testrail-junit-extensions)

This repo contains several improvements for [JUnit](https://junit.org/junit5/) that allow you to take better advantage of JUnit 5 (jupiter engine) whenever using it together with [TestRail](https://www.testrail.com).
This code is provided as-is; you're free to use it and modify it at your will (see license ahead).

This is a preliminary release, so it is subject to change at any time.

## Overview

Results from automated scripts implemented as `@Test` methods can be tracked in test management tools to provide insights about quality aspects targeted by those scripts and their impacts.
Therefore, it's important to attach some relevant information during the execution of the tests, so it can be shared and analyzed later on in the test management tool (e.g. [TestRail](https://www.testrail.com)).

This project is highly based on previous work by the JUnit team. The idea is to be able to produce a custom JUnit XML report containing additional information that TestRail can take advantage of.
This way, testers can automate the test script and at the same time provide information such as the covered requirement, right from the test automation code. Additional information may be provided, either through new annotations or by injecting a custom reporter as argument to the test method, using a specific extension.

### Features

- Track started and finished date timestamps for each test
- Link a test method to an existing TestRail test case
- Specify additional properties dynamically
- Add attachments using the [TestRail CLI](https://github.com/gurock/trcli)

### Main classes

The project consists of:

- **EnhancedLegacyXmlReportGeneratingListener**: a custom TestExecutionListener implementation that is responsible for generating a custom JUnit XML with additional properties TestRail can take advantage of
- **@TestRail**: optional annotation to provide additional information whenever writing the automated test methods
- **TestRailTestReporterParameterResolver**: a new, optional JUnit 5 extension that provides the means to report additional information to TestRail, inside the test method flow

## Installing

These extensions are available as an artifact available on (Maven) Central Repository, which is configured by default in your Maven instalation.

Add the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>com.testrail</groupId>
  <artifactId>testrail-junit-extensions</artifactId>
  <version>0.2.0</version>
  <scope>test</scope>
</dependency>
```

### Configuration

If you want, you may configure certain aspects of this extension. The defaults should be fine, otherwise please create a properties file as `src/test/resources/testrail-junit-extensions.properties`, and define some settings.

- `report_filename`: the name of the report, without ending .xml suffix. Default is "TEST-junit-jupiter.xml"
- `report_directory`: the directory where to generate the report, in relative or absolute format. Default is "target"
- `add_timestamp_to_report_filename`: the name of the report, without ending .xml suffix. Default is "false".
- `properties_using_cdata`: list of properties, delimited by comma, whose content should be encoded as cdata inner content instead of `value` attribute on the `property` element; by default "testrail_case_field" is encoded as cdata. This is useful whenever you want to add properties that have newlines and other characters as part of their content. (encoding as cdata is only supported by TR CLI v1.9.3+)
 
Example:

```bash
report_filename=custom-report-junit
report_directory=reports
add_timestamp_to_report_filename=true
properties_using_cdata=testrail_case_field
```

## How to use

In order to generate the enhanced, customized JUnit XML report we need to register the **EnhancedLegacyXmlReportGeneratingListener** listener. This can be done in [several ways](https://junit.org/junit5/docs/current/user-guide/#launcher-api-listeners-custom):

- discovered automatically at runtime based on the contents of a file (e.g `src/test/META-INF/services/org.junit.platform.launcher.TestExecutionListener`) 

```
EnhancedLegacyXmlReportGeneratingListener
```

- programmaticaly

```java
LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                                                .request().selectors(...).filters(...).build();
Launcher launcher = LauncherFactory.create();
launcher.discover(request)
EnhancedLegacyXmlReportGeneratingListener listener = new EnhancedLegacyXmlReportGeneratingListener( Paths.get("/tmp/reports/"),new PrintWriter(System.out));
launcher.registerTestExecutionListeners(listener);
launcher.execute(request);
```

Registering the listener is mandatory.
In order to take advantage of the capabilities of this new listener, new annotations can be used. There's also a custom ParameterResolver **TestRailTestReporterParameterResolver**, which allows users to inject a **TestRailTestReporter** object as argument on the test methods. This will be useful when it's needed to report/attach some additional information during the test lifecycle.


### @TestRail Annotation

Test methods don't need to be annotated with `@TestRail` unless you want to take advantage of the following enhancements.

You may use the **@TestRail** annotation to:

- Add an extra property to map a test result to a specific Test Case on TestRail
- Add an extra summary property to the test case
- Add an extra description property to the test case

**Example:** Add case "id" property



Test code:
```java
public class SumTests {

  @Test
  @TestRail(id = "123")
  public void CanAddNumbers() {}
}
```
Report testcase element:
```xml
<testcase name="CanAddNumbers" classname="tests.SumTests">
  <properties>
    <property name="test_id" value="123"/>
  </properties>
</testcase>
```

### New Extension

A new JUnit 5 compatible Extension **TestRailTestReporterParameterResolver** can be used, so we can inject a **TestRailTestReporter** object as argument in the test methods.
This allows you to add any custom property to the report, enhancing the way results are uploaded to TestRail.

**Example:** Add attachments to test result (supported by the [TestRail CLI](https://github.com/gurock/trcli))

Test code:
```java
@ExtendWith(TestRailTestReporterParameterResolver.class)
public class SumTests {
    
    @Test
    public void canAddNumbers(TestRailTestReporter customReporter) {
        customReporter.setProperty("testrail_attachment_1", "path/to/attachment1");
        customReporter.setProperty("testrail_attachment_2", "path/to/attachment2");
    }
}
```
Report testcase element:
```xml
<testcase name="CanAddNumbers" classname="tests.SumTests">
  <properties>
    <property name="testrail_attachment_1" value="path/to/attachment1"/>
    <property name="testrail_attachment_2" value="path/to/attachment2"/>
  </properties>
</testcase>
```

## Other features and limitations

### Parameterized and repeated tests

For the time being, and similar to what happened with legacy JUnit XML reports produces with JUnit 4, parameterized tests (i.e. annotated with `@ParameterizedTest`) will be mapped to similar `<testcase>` elements in the JUnit XML report.
The same happens with repeated tests (i.e annotated with `@RepeatedTest`).

## Background

Junit 5 has a more flexible architecture. Even though JUnit XML format as not evolved meanwhile, it's possible to use Extensions and Test Execution Listeners to implement our own, tailored custom reporter.

JUnit 5 provides a [legacy XML reporter for the jupiter engine](https://junit.org/junit5/docs/current/api/org.junit.platform.reporting/org/junit/platform/reporting/legacy/xml/LegacyXmlReportGeneratingListener.html) as a listener, which was used as basis for this implementation.


## TO DOs

- Cleanup code
- Add more tests
- Enforce summary and description (not yet support by any of the mechanisms TestRail supports at this time to
  import test cases)

## FAQ

1. Can this be used with JUnit 4?
No. If you're using JUnit 4 you can still generate a "standard" JUnit XML report but you'll miss the capabilities provided by this project. It's recommended to use JUnit 5 as JUnit 4 is an older project and much more limited.

2. Is this format compatible with Jenkins and other tools?
Probably. As there is no official JUnit XML schema, it's hard to say that in advance. However, the new information being embed on the custom JUnit XML report is done in such a way that shouldn't break other tools.

3. Then enhanced JUnit XML report gets overwritten whenever I have different test classes...
Make sure you're using a recent version of `maven-surefire-plugin`, like 3.2.5; that will fix it!  If you're using a old version (e.g., 2.22.2), surefire will generate one JUnit testplan per class, and the reporter will be invoked multiple times, overwriting the previous report.

4. I don't see the enhanced JUnit XML report or I see a report but without the Xray specific information.
You're probably using the legacy JUnit report and not the one generated by this plugin. Make sure you're using the `EnhancedLegacyXmlReportGeneratingListener`; see how to enable it, above in the "How to use" section.

## Contact

Any questions related with this code, please raise issues in this GitHub project. Feel free to contribute and submit PR's.

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 legacy XML reporter](https://junit.org/junit5/docs/current/api/org.junit.platform.reporting/org/junit/platform/reporting/legacy/xml/LegacyXmlReportGeneratingListener.html)
- [TestRail CLI documentation](https://support.gurock.com/hc/en-us/articles/7146548750868)


## LICENSE

[Eclipse Public License - v 2.0](LICENSE)
