package test.example;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class MethodNameTest {

    @Test
    public void C111_testSayHello() {
	HelloWorld helloWorld = new HelloWorld();
	assertEquals("Hello, World!", helloWorld.sayHello());
    }

    @Test
    public void C112_testAdd() {
	HelloWorld helloWorld = new HelloWorld();
	assertEquals(5, helloWorld.add(2, 3));
	assertEquals(-1, helloWorld.add(2, -3));
    }
}
