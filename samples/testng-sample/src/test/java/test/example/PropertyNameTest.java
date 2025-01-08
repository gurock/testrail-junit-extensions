package test.example;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.testrail.junit.customjunitxml.annotations.TestRail;

public class PropertyNameTest {

    @Test
    @TestRail(id = "C222")
    public void testSayHello() {
	HelloWorld helloWorld = new HelloWorld();
	assertEquals("Hello, World!", helloWorld.sayHello());
    }

    @Test
    @TestRail(id = "C223")
    public void testAdd() {
	HelloWorld helloWorld = new HelloWorld();
	assertEquals(5, helloWorld.add(2, 3));
	assertEquals(-1, helloWorld.add(2, -3));
    }
}
