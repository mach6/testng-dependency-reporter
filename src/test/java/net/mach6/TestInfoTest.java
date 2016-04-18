package net.mach6;

import static org.testng.Assert.*;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class TestInfoTest {

    @Test
    public void constructor(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertNotNull(testInfo);
    }

    @Test(dependsOnMethods = "constructor")
    public void equals(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertTrue(testInfo.equals(testInfo));
    }

    @Test(dependsOnMethods = "constructor")
    public void compareTo(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertEquals(testInfo.compareTo(testInfo), 0);
    }

    @Test(dependsOnMethods = "constructor")
    public void getName(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertEquals(testInfo.getName(), ctx.getName());
    }

    @Test(dependsOnMethods = "constructor")
    public void getTestClasses(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertNull(testInfo.getTestClasses());
    }

    @Test(dependsOnMethods = "constructor")
    public void getXmlTest(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertSame(testInfo.getITestContext(), ctx);
    }

    @Test(dependsOnMethods = "constructor")
    public void isPreserveOrder(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        assertEquals(testInfo.isPreserveOrder(), Boolean.parseBoolean(ctx.getCurrentXmlTest().getPreserveOrder()));
    }

    @Test(dependsOnMethods = "constructor")
    public void toDot(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        String subGraph = testInfo.toDot(true);
        String graph = testInfo.toDot(false);

        assertTrue(subGraph.equals(""));
        assertTrue(graph.contains("digraph g"));
    }

    @Test(dependsOnMethods = { "constructor", "compareTo" })
    public void toJSON(ITestContext ctx) {
        TestInfo testInfo = new TestInfo(ctx, null);
        String json = testInfo.toJSON();
        assertTrue(!json.equals(""));
        TestInfo compareTo = new Gson().fromJson(json, TestInfo.class);
        assertEquals(testInfo.compareTo(compareTo), 0);
    }
}
