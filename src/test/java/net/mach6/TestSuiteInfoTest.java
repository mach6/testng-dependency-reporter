package net.mach6;

import static org.testng.Assert.*;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class TestSuiteInfoTest {

    @Test
    public void constructor(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertNotNull(suiteInfo);
    }

    @Test(dependsOnMethods = "constructor")
    public void equals(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertTrue(suiteInfo.equals(suiteInfo));
    }

    @Test(dependsOnMethods = "constructor")
    public void compareTo(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertEquals(suiteInfo.compareTo(suiteInfo), 0);
    }

    @Test(dependsOnMethods = "constructor")
    public void getChildSuites(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertTrue(suiteInfo.getChildSuites().size() == 0);
    }

    @Test(dependsOnMethods = "constructor")
    public void getName(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertEquals(suiteInfo.getName(), ctx.getSuite().getName());
    }

    @Test(dependsOnMethods = "constructor")
    public void getISuite(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertSame(suiteInfo.getISuite(), ctx.getSuite());
    }

    @Test(dependsOnMethods = "constructor")
    public void getParallel(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertEquals(suiteInfo.getParallel(), ctx.getSuite().getParallel().toString());
    }

    @Test(dependsOnMethods = "constructor")
    public void getTests(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertEquals(suiteInfo.getTests(), null);
    }

    @Test(dependsOnMethods = "constructor")
    public void isPreserveOrder(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        assertEquals(suiteInfo.isPreserveOrder(),
                Boolean.parseBoolean(ctx.getCurrentXmlTest().getSuite().getPreserveOrder()));
    }

    @Test(dependsOnMethods = "constructor")
    public void toDot(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        String subGraph = suiteInfo.toDot(true);
        String graph = suiteInfo.toDot(false);

        assertTrue(subGraph.equals(""));
        assertTrue(graph.contains("digraph g"));
    }

    @Test(dependsOnMethods = { "constructor", "compareTo" })
    public void toJSON(ITestContext ctx) {
        TestSuiteInfo suiteInfo = new TestSuiteInfo(ctx.getSuite(), null);
        String json = suiteInfo.toJSON();
        assertTrue(!json.equals(""));
        TestSuiteInfo compareTo = new Gson().fromJson(json, TestSuiteInfo.class);
        assertEquals(suiteInfo.compareTo(compareTo), 0);
    }
}
