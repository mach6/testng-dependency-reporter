package net.mach6;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.xml.XmlSuite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TestSuiteInfo implements Comparable<TestSuiteInfo>, Dottable, JSONable, Failable<TestInfo>,
        Skipable<TestInfo>, Passable<TestInfo> {

    @Expose
    private String parallel;
    @Expose
    private String name;
    @Expose
    private boolean preserveOrder;
    @Expose
    private Set<TestInfo> tests;
    @Expose
    private Set<String> childSuites;
    @Expose
    private Result suiteResult;

    private ISuite suite;

    public TestSuiteInfo(ISuite suite) {
        this(suite, null);
    }

    public TestSuiteInfo(ISuite suite, Set<TestInfo> tests) {
        // TODO guard against null values being passed

        this.suite = suite;
        this.parallel = suite.getParallel();
        this.preserveOrder = Boolean.parseBoolean(suite.getXmlSuite().getPreserveOrder());
        this.name = suite.getName();
        this.tests = tests;
        buildChildSuites();

        if (tests == null) {
            buildTestInfo();
        }
        setTestResult();
    }

    private void setTestResult() {
        suiteResult = Result.SKIPPED;
        if (hasPassed()) {
            suiteResult = Result.PASSED;
        }
        if (hasFailures()) {
            suiteResult = Result.FAILED;
        }
    }

    public Result getResult() {
        return suiteResult;
    }

    private void buildChildSuites() {
        List<XmlSuite> children = suite.getXmlSuite().getChildSuites();
        Set<String> suites = new ConcurrentSkipListSet<>();
        for (XmlSuite xs : children) {
            suites.add(xs.getName());
        }
        this.childSuites = suites;
    }

    private void buildTestInfo() {
        tests = new ConcurrentSkipListSet<>();
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            tests.add(new TestInfo(suiteResult.getTestContext()));
        }
    }

    @Override
    public int compareTo(TestSuiteInfo o) {
        return o.getName().compareTo(getName());
    }

    private String dotWalkSuiteInfo(TestSuiteInfo suite) {
        if (suite.getChildSuites() == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (String s : suite.getChildSuites()) {
            result.append("\"" + s + "\"[shape=box,peripheries=2,label=\"Suite: " + s + "\"];\n");
            result.append("\"" + suite.getName() + "\" -> \"" + s + "\";\n");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestSuiteInfo other = (TestSuiteInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public Set<String> getChildSuites() {
        return childSuites;
    }

    public ISuite getISuite() {
        return suite;
    }

    public String getName() {
        return name;
    }

    public String getParallel() {
        return parallel;
    }

    public Set<TestInfo> getTests() {
        return tests;
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
        if (asSubgraph &&
                getTests().isEmpty() &&
                getChildSuites().isEmpty()) {
            return "";
        }

        String formattedName = String.valueOf(hashCode() < 0 ? System.currentTimeMillis() : hashCode());
        StringBuilder result = new StringBuilder((asSubgraph) ? "subgraph suite" + formattedName : "digraph g");
        result.append(" {\n");
        result.append("\"" + getName() + "\"[" + Result.getDotStyle(getResult()) +
                    ",shape=box,peripheries=2,label=\"Suite: " + getName() + "\"];\n");
        result.append(dotWalkSuiteInfo(this));
        for (TestInfo ti : getTests()) {
            result.append("\"" + ti.getName() + "\"[" + Result.getDotStyle(ti.getResult()) +
                    ",shape=house,label=\"Test: " + ti.getName() + "\"];\n");
            result.append("\"" + getName() + "\" -> \"" + ti.getName() + "\";\n");
            result.append(ti.toDot(true));
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
        for (TestInfo ti : getTests()) {
            if (ti.hasPassed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestInfo> getPassed() {
        Set<TestInfo> passed = new HashSet<>();
        for (TestInfo ti : getTests()) {
            if (ti.getResult() == Result.PASSED) {
                passed.add(ti);
            }
        }
        return passed;
    }

    @Override
    public boolean hasSkips() {
        for (TestInfo ti : getTests()) {
            if (ti.hasSkips()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestInfo> getSkips() {
        Set<TestInfo> skipped = new HashSet<>();
        for (TestInfo ti : getTests()) {
            if (ti.getResult() == Result.SKIPPED) {
                skipped.add(ti);
            }
        }
        return skipped;
    }

    @Override
    public boolean hasFailures() {
        for (TestInfo ti : getTests()) {
            if (ti.hasFailures()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<TestInfo> getFailures() {
        Set<TestInfo> failed = new HashSet<>();
        for (TestInfo ti : getTests()) {
            if (ti.getResult() == Result.FAILED) {
                failed.add(ti);
            }
        }
        return failed;
    }

}
