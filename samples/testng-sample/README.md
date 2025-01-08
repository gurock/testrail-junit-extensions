# Using testrail-junit-extensions with TestNG

When you run your tests with only TestNG - using `mvn test` - your output should look something like:

```
[INFO] --- maven-surefire-plugin:3.5.2:test (default-test) @ testng-sample ---
[INFO] Using auto detected provider org.apache.maven.surefire.testng.TestNGProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running TestSuite
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.131 s -- in TestSuite
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Note that TestNG produces JUnit-style reports in `target/surefire-reports/junitreports`!

In order to take advantage of the
[testrail-junit-extensions](https://github.com/gurock/testrail-junit-extensions), you must use a
[JUnit5 engine](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html#smart-resolution-of-jupiter-engine-and-vintage-engine-for-junit4)
to run the tests.

Adding the correct dependency to the Surefire plugin, your output should now look something like:

```
[INFO] --- maven-surefire-plugin:3.5.2:test (default-test) @ testng-sample ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running test.example.PropertyNameTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in test.example.PropertyNameTest
[INFO] Running test.example.MethodNameTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in test.example.MethodNameTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Notice the different provider (and different output). Also, the JUnit-style reports are now in
`target/surefire-reports`, where JUnit normally places these.

We can now hook up the `testrail-junit-extensions` dependency, **plus** specify the correct listener
in `src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener`.

After this dependency nothing much changes in the output. However, you will get a new file
`target/TEST-testng.xml` that is the aggregate of all the JUnit-style reports **including** all the
`@TestRail` annotations from your tests.

... profit! ;)
