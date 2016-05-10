/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

package net.mach6;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.testng.ITestClass;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TestClassInfo implements Comparable<TestClassInfo>, Dottable, JSONable, YAMLable,
        Failable<TestMethodInfo>,
        Passable<TestMethodInfo>, Skipable<TestMethodInfo> {

    @Expose
    private String name;
    @Expose
    private Set<TestMethodInfo> testMethods;
    @Expose
    private Result classResult;

    private ITestClass testClass;

    public TestClassInfo(ITestClass testClass, Set<TestMethodInfo> testMethods) {
        // TODO guard against null values being passed

        this.testClass = testClass;
        this.name = testClass.getName();
        this.testMethods = testMethods;

        if (testMethods == null) {
            this.testMethods = new ConcurrentSkipListSet<>();
        }
        setTestResult();
    }

    private void setTestResult() {
        classResult = Result.PASSED;
        if (hasSkips()) {
            classResult = Result.SKIPPED;
        }
        if (hasFailures()) {
            classResult = Result.FAILED;
        }
    }

    @Override
    public int compareTo(TestClassInfo o) {
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
        TestClassInfo other = (TestClassInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public String getName() {
        return name;
    }

    public ITestClass getTestClass() {
        return testClass;
    }

    public Set<TestMethodInfo> getTestMethods() {
        return testMethods;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toDot(boolean asSubgraph) {
        if (asSubgraph &&
                getTestMethods().isEmpty()) {
            return "";
        }

        Set<String> visited = new HashSet<>();
        String formattedName = String.valueOf(hashCode() < 0 ? System.currentTimeMillis() : hashCode());
        StringBuilder result = new StringBuilder((asSubgraph) ? "subgraph class" + formattedName : "digraph g");
        result.append(" {\n");

        result.append("\"" + getName() + "\"[shape=box,label=\"Class: " + getName() + "\"];\n");
        for (TestMethodInfo tmi : getTestMethods()) {
            String methodName = tmi.getMethodName();
            if (!visited.contains(methodName)) {
                result.append("\"" + methodName + "\"[" + Result.getDotStyle(tmi.getResult()) + "];\n");
                result.append("\"" + getName() + "\" -> \"" + methodName + "\";\n");
                result.append(tmi.toDot(true));
                visited.add(methodName);
            }
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
    public String toYAML() {
        return new Yaml().dump(this);
    }

    @Override
    public boolean hasPassed() {
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.PASSED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestMethodInfo> getPassed() {
        Set<TestMethodInfo> passed = new HashSet<>();
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.PASSED) {
                passed.add(tm);
            }
        }
        return passed;
    }

    @Override
    public boolean hasSkips() {
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.SKIPPED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestMethodInfo> getSkips() {
        Set<TestMethodInfo> skipped = new HashSet<>();
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.SKIPPED) {
                skipped.add(tm);
            }
        }
        return skipped;
    }

    @Override
    public boolean hasFailures() {
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.FAILED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestMethodInfo> getFailures() {
        Set<TestMethodInfo> failed = new HashSet<>();
        for (TestMethodInfo tm : getTestMethods()) {
            if (tm.getResult() == Result.FAILED) {
                failed.add(tm);
            }
        }
        return failed;
    }

    public Result getResult() {
        return classResult;
    }
}
