/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

package net.mach6;

import static org.testng.Assert.*;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class TestMethodInfoTest {

    @Test
    public void constructor(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertNotNull(methodInfo);
    }

    @Test(dependsOnMethods = "constructor")
    public void compareTo(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertEquals(methodInfo.compareTo(methodInfo), 0);
    }

    @Test(dependsOnMethods = "constructor")
    public void equals(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertTrue(methodInfo.equals(methodInfo));
    }

    @Test(dependsOnMethods = "constructor")
    public void getAfterGroups(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertTrue(methodInfo.getAfterGroups().isEmpty());
    }

    @Test(dependsOnMethods = "constructor")
    public void getBeforeGroups(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertTrue(methodInfo.getBeforeGroups().isEmpty());
    }

    @Test(dependsOnMethods = "constructor", dependsOnGroups = { "testmethodinfo" })
    public void getDependentGroups(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertEquals(methodInfo.getDependentGroups().size(), 1);
        assertEquals(methodInfo.getDependentGroups().get(0), "testmethodinfo");
    }

    @Test(dependsOnMethods = "constructor")
    public void getDependentMethods(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertEquals(methodInfo.getDependentMethods().size(), 1);
        assertEquals(methodInfo.getDependentMethods().get(0), getClass().getCanonicalName() + ".constructor");
    }

    @Test(dependsOnMethods = "constructor", groups = { "testmethodinfo" })
    public void getGroups(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertEquals(methodInfo.getGroups().size(), 1);
        assertEquals(methodInfo.getGroups().get(0), "testmethodinfo");
    }

    @Test(dependsOnMethods = "constructor")
    public void getMethodName(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        assertEquals(methodInfo.getMethodName(), getClass().getCanonicalName() + ".getMethodName");
    }

    @Test(dependsOnMethods = "constructor")
    public void getTestNGMethod(ITestContext ctx) {
        ITestNGMethod method = Reporter.getCurrentTestResult().getMethod();
        TestMethodInfo methodInfo = new TestMethodInfo(method, ctx.getSuite());
        assertSame(methodInfo.getTestNGMethod(), method);
    }

    @Test(dependsOnMethods = "constructor")
    public void toDot(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        String subGraph = methodInfo.toDot(true);
        String graph = methodInfo.toDot(false);

        assertTrue(subGraph.contains("subgraph"));
        assertTrue(graph.contains("digraph g"));
    }

    @Test(dependsOnMethods = { "constructor", "compareTo" })
    public void toJSON(ITestContext ctx) {
        TestMethodInfo methodInfo = new TestMethodInfo(Reporter.getCurrentTestResult().getMethod(), ctx.getSuite());
        String json = methodInfo.toJSON();
        assertTrue(!json.equals(""));
        TestMethodInfo compareTo = new Gson().fromJson(json, TestMethodInfo.class);
        assertEquals(methodInfo.compareTo(compareTo), 0);
    }
}
