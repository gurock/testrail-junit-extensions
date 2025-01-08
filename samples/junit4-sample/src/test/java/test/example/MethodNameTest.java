package test.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
