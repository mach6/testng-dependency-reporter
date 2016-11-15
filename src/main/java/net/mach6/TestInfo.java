/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

package net.mach6;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TestInfo implements Comparable<TestInfo>, Dottable, JSONable, Failable<TestClassInfo>,
        Passable<TestClassInfo>, Skipable<TestClassInfo> {

    @Expose
    private String name;
    @Expose
    private boolean preserveOrder;
    @Expose
    private String parallel;
    @Expose
    private Set<TestClassInfo> testClasses;
    @Expose
    private List<String> testGroups;
    @Expose
    private Result testResult;

    private ITestContext testContext;

    public TestInfo(ITestContext testContext) {
        this(testContext, null);
    }

    public TestInfo(ITestContext testContext, Set<TestClassInfo> testClasses) {
        // TODO guard against null values being passed

        this.testContext = testContext;
        this.name = testContext.getName();
        this.preserveOrder = Boolean.parseBoolean(testContext.getCurrentXmlTest().getPreserveOrder());
        this.parallel = testContext.getCurrentXmlTest().getParallel().toString();
        this.testClasses = testClasses;
        this.testGroups = Arrays.asList(testContext.getIncludedGroups());

        if (testClasses == null) {
            buildTestClasses();
        }
        setTestResult();
    }

    private void setTestResult() {
        testResult = Result.PASSED;
        if (hasSkips()) {
            testResult = Result.SKIPPED;
        }
        if (hasFailures()) {
            testResult = Result.FAILED;
        }
    }

    private void buildTestClasses() {
        testClasses = new ConcurrentSkipListSet<>();

        // Get all the Methods in the test context
        Set<ITestNGMethod> allMethods = new HashSet<>();

        allMethods.addAll(testContext.getPassedTests().getAllMethods());
        allMethods.addAll(testContext.getFailedButWithinSuccessPercentageTests().getAllMethods());
        allMethods.addAll(testContext.getFailedTests().getAllMethods());
        allMethods.addAll(testContext.getSkippedTests().getAllMethods());

        Set<ITestClass> visitedClasses = new HashSet<>();
        for (ITestNGMethod testMethod : allMethods) {
            ITestClass testClass = testMethod.getTestClass();
            if (!visitedClasses.contains(testClass)) {
                testClasses.add(new TestClassInfo(testMethod.getTestClass(),
                        filterTestMethods(testMethod.getTestClass(), allMethods)));
                visitedClasses.add(testClass);
            }
        }
    }

    @Override
    public int compareTo(TestInfo o) {
        return o.getName().compareTo(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestInfo other = (TestInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    private Set<TestMethodInfo> filterTestMethods(ITestClass testClass, Set<ITestNGMethod> allMethods) {
        Set<TestMethodInfo> testMethods = new ConcurrentSkipListSet<>();
        for (ITestNGMethod testMethod : allMethods) {
            if ((testMethod.getTestClass() == testClass) && (testMethod.getEnabled())) {
                TestMethodInfo toAdd = new TestMethodInfo(testMethod, testContext.getSuite());
                toAdd.setResult(determineTestMethodResult(testMethod));
                testMethods.add(toAdd);
            }
        }
        return testMethods;
    }

    private Result determineTestMethodResult(ITestNGMethod testMethod) {
        if (testContext.getFailedTests().getAllMethods().contains(testMethod)) {
            return Result.FAILED;
        }
        if (testContext.getSkippedTests().getAllMethods().contains(testMethod)) {
            return Result.SKIPPED;
        }
        return Result.PASSED;
    }

    public ITestContext getITestContext() {
        return testContext;
    }

    public String getName() {
        return name;
    }

    public String getParallel() {
        return parallel;
    }

    public Set<TestClassInfo> getTestClasses() {
        return testClasses;
    }

    public List<String> getTestGroups() {
        return testGroups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    public boolean isPreserveOrder() {
        return preserveOrder;
    }

    @Override
    public String toDot(boolean asSubgraph) {
        // nothing to add
        if (asSubgraph &&
                getTestClasses().isEmpty() &&
                getTestGroups().isEmpty()) {
            return "";
        }

        Set<String> visited = new HashSet<>();
        String formattedName = String.valueOf(hashCode() < 0 ? System.currentTimeMillis() : hashCode());
        formattedName = formattedName.replace(".", "");
        StringBuilder result = new StringBuilder((asSubgraph) ? "subgraph test" + formattedName : "digraph g");
        result.append(" {\n");
        for (TestClassInfo tc : getTestClasses()) {
            if (!visited.contains(tc.getName())) {
                result.append("\"" + tc.getName() + "\"[" + Result.getDotStyle(tc.getResult()) + "];\n");
                result.append("\"" + getName() + "\" -> \"" + tc.getName() + "\";\n");
                result.append(tc.toDot(true));
                visited.add(tc.getName());
            }
        }
        for (String group : getTestGroups()) {
            result.append("\"" + group + "\"[shape=cds,label=\"Group: " + group + "\"];\n");
            result.append("\"" + getName() + "\" -> \"" + group + "\";\n");
        }
        result.append("}\n");

        return result.toString();
    }

    @Override
    public String toJSON() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    @Override
    public boolean hasPassed() {
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.hasPassed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestClassInfo> getPassed() {
        Set<TestClassInfo> passed = new HashSet<>();
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.getResult() == Result.PASSED) {
                passed.add(tc);
            }
        }
        return passed;
    }

    @Override
    public boolean hasSkips() {
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.hasSkips()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestClassInfo> getSkips() {
        Set<TestClassInfo> skipped = new HashSet<>();
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.getResult() == Result.SKIPPED) {
                skipped.add(tc);
            }
        }
        return skipped;
    }

    @Override
    public boolean hasFailures() {
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.hasFailures()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestClassInfo> getFailures() {
        Set<TestClassInfo> failed = new HashSet<>();
        for (TestClassInfo tc : getTestClasses()) {
            if (tc.getResult() == Result.FAILED) {
                failed.add(tc);
            }
        }
        return failed;
    }

    public Result getResult() {
        return testResult;
    }

}
