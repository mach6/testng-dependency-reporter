package net.mach6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.internal.MethodHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TestMethodInfo implements Comparable<TestMethodInfo>, Dottable, JSONable {
    @Expose
    private String methodName;
    @Expose
    private List<String> groups;
    @Expose
    private List<String> dependentGroups;
    @Expose
    private List<String> beforeGroups;
    @Expose
    private List<String> afterGroups;
    @Expose
    private List<String> dependentMethods;
    @Expose
    private Result result;

    private ITestNGMethod method;

    public TestMethodInfo(ITestNGMethod method, ISuite suite) {
        // TODO guard against null values being passed

        this.method = method;
        this.methodName = method.getTestClass().getName() + "." + method.getMethodName();

        List<ITestNGMethod> methodsDependedUpon = MethodHelper.getMethodsDependedUpon(method, suite.getAllMethods()
                .toArray(new ITestNGMethod[suite.getAllMethods().size()]));

        List<String> methodsDependedUponNames = new ArrayList<>();
        for (ITestNGMethod m : methodsDependedUpon) {
            methodsDependedUponNames.add(m.getTestClass().getName() + "." + m.getMethodName());
        }

        this.dependentMethods = methodsDependedUponNames;
        this.groups = Arrays.asList(method.getGroups());
        this.dependentGroups = Arrays.asList(method.getGroupsDependedUpon());
        this.beforeGroups = Arrays.asList(method.getBeforeGroups());
        this.afterGroups = Arrays.asList(method.getAfterGroups());
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public int compareTo(TestMethodInfo o) {
        return o.getMethodName().compareTo(getMethodName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestMethodInfo other = (TestMethodInfo) obj;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

    public List<String> getAfterGroups() {
        return afterGroups;
    }

    public List<String> getBeforeGroups() {
        return beforeGroups;
    }

    public List<String> getDependentGroups() {
        return dependentGroups;
    }

    public List<String> getDependentMethods() {
        return dependentMethods;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getMethodName() {
        return methodName;
    }

    public ITestNGMethod getTestNGMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public String toDot(boolean asSubgraph) {
        if (asSubgraph &&
                getDependentMethods().isEmpty() &&
                getDependentGroups().isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder((asSubgraph) ?
                "subgraph method" + String.valueOf(hashCode() < 0 ? System.currentTimeMillis() : hashCode()) :
                "digraph g");
        result.append(" {\n");
        for (String method : getDependentMethods()) {
            result.append("\"" + getMethodName() + "\" -> \"" + method + "\";\n");
        }
        for (String group : getDependentGroups()) {
            result.append("\"" + group + "\"[shape=cds,label=\"Group: " + group + "\"];\n");
            result.append("\"" + getMethodName() + "\" -> \"" + group + "\";\n");
        }
        result.append("}\n");
        return result.toString();
    }

    @Override
    public String toJSON() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
