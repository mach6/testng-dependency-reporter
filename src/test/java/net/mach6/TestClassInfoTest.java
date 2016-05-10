package net.mach6;

import static org.testng.Assert.*;

import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class TestClassInfoTest {

    @Test
    public void constructor(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        assertNotNull(classInfo);
    }

    @Test(dependsOnMethods = "constructor")
    public void equals(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        assertTrue(classInfo.equals(classInfo));
    }

    @Test(dependsOnMethods = "constructor")
    public void compareTo(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        assertEquals(classInfo.compareTo(classInfo), 0);
    }

    @Test(dependsOnMethods = "constructor")
    public void getName(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        assertEquals(classInfo.getName(), this.getClass().getCanonicalName());
    }

    @Test(dependsOnMethods = "constructor")
    public void getTestClass(ITestContext ctx) {
        ITestClass testClass = Reporter.getCurrentTestResult().getMethod().getTestClass();
        TestClassInfo classInfo = new TestClassInfo(testClass, null);
        assertSame(classInfo.getTestClass(), testClass);
    }

    @Test(dependsOnMethods = "constructor")
    public void getTestMethods(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        assertTrue(classInfo.getTestMethods().isEmpty());
    }

    @Test(dependsOnMethods = "constructor")
    public void toDot(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        String subGraph = classInfo.toDot(true);
        String graph = classInfo.toDot(false);

        assertTrue(subGraph.equals(""));
        assertTrue(graph.contains("digraph g"));
    }

    @Test(dependsOnMethods = { "constructor", "compareTo" })
    public void toJSON(ITestContext ctx) {
        TestClassInfo classInfo = new TestClassInfo(Reporter.getCurrentTestResult().getMethod().getTestClass(), null);
        String json = classInfo.toJSON();
        assertTrue(!json.equals(""));
        TestClassInfo compareTo = new Gson().fromJson(json, TestClassInfo.class);
        assertEquals(classInfo.compareTo(compareTo), 0);
    }
}
