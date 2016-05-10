/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

package net.mach6.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import net.mach6.Dottable;
import net.mach6.JSONable;
import net.mach6.TestClassInfo;
import net.mach6.TestSuiteInfo;
import net.mach6.TestInfo;
import net.mach6.TestMethodInfo;
import net.mach6.YAMLable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.IResultListener2;
import org.testng.xml.XmlSuite;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DependencyReportingListener implements IResultListener2, IReporter {
    private static final Logger LOGGER = Logger.getLogger(DependencyReportingListener.class.getName());
    private static final String OUTPUT_DIR = "/DependencyReporter";
    private static final String DASH_OPTION = "dependencyReporter";
    private static final String REPORT_FILENAME_JSON = "/report.json";
    private static final String REPORT_FILENAME_YAML = "/report.yaml";
    private static final String REPORT_FILENAME_DOT = "/report.dot";
    private static List<String> dotFiles = new ArrayList<>();

    /**
     * Command line options for influencing the behavior of this listener/reporter <br>
     * Format - enumerated property name:value <br>
     * Default - first value in list <br>
     * <br>
     * For example:
     * 
     * <pre>
     * <code>prescan:false</code>, with "false" being the default value.
     * </pre>
     */
    public enum Option {
        ENABLED(Arrays.asList("true", "false")),
        PRESCAN(Arrays.asList("false", "true")),
        MODE(Arrays.asList("all", "suites", "tests", "classes", "methods", "groups", "configuration")),
        OUTPUT(Arrays.asList("all", "dot", "png", "json","yaml"));

        private List<String> values;

        Option(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }

        public boolean isSet(String value) {
            String options = System.getProperty(DASH_OPTION, "");
            String parsed = parseOptions(options);
            return parsed.equalsIgnoreCase(value);
        }

        public boolean isSet(String... orValues) {
            for (String value : orValues) {
                if (isSet(value)) {
                    return true;
                }
            }
            return false;
        }

        private String parseOptions(String options) {
            // set the default
            String opt = this.values.get(0);

            // parse the supplied, overriding the default value if specified and a valid value
            String[] keyValuePairs = options.split(",");
            for (String pair : keyValuePairs) {
                String[] terms = pair.split(":");
                String key = terms[0].toUpperCase();
                if (!this.name().equals(key)) {
                    continue;
                }
                String value = terms[1].toLowerCase();
                if (this.getValues().contains(value)) {
                    opt = value;
                }
            }
            return opt;
        }
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        if (Option.ENABLED.isSet("false")) {
            return;
        }

        final String TOP_DIR = outputDirectory + OUTPUT_DIR;
        // Clean out any old results, if they exist
        FileUtils.deleteQuietly(new File(TOP_DIR));

        // Build the TestSuiteInfo Set
        Set<TestSuiteInfo> suiteInfoSet = new ConcurrentSkipListSet<>();
        for (ISuite suite : suites) {
            TestSuiteInfo suiteInfo = new TestSuiteInfo(suite);
            suiteInfoSet.add(suiteInfo);
        }

        generateOutput(suiteInfoSet, TOP_DIR);
        logCompletion(suiteInfoSet);
    }

    private void generateOutput(Set<TestSuiteInfo> suiteInfoSet, String directory) {
        toJson(suiteInfoSet, directory + REPORT_FILENAME_JSON);
        toYaml(suiteInfoSet, directory + REPORT_FILENAME_YAML);
        toDot(suiteInfoSet, directory + REPORT_FILENAME_DOT);
        generateOutputForTestSuiteInfo(suiteInfoSet, directory);
        generatePngFromDotFiles();
    }

    private void toDot(Set<TestSuiteInfo> suiteInfoSet, String fileName) {
        if (!Option.OUTPUT.isSet("dot", "png", "all")) {
            return;
        }

        StringBuilder builder = new StringBuilder("digraph g {\n");
        for (Dottable dottable : suiteInfoSet) {
            builder.append(dottable.toDot(true));
        }
        builder.append("}\n");
        writeDotFile(builder.toString(), fileName);
    }

    private void toJson(Object jsonable, String filename) {
        if (!Option.OUTPUT.isSet("json", "all")) {
            return;
        }

        LOGGER.fine("Creating " + filename);
        String json = "";
        if (jsonable instanceof JSONable) {
            json = ((JSONable) jsonable).toJSON();
        }
        else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            json = gson.toJson(jsonable);
        }
        try {
            FileUtils.writeStringToFile(new File(filename), json, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create " + filename + " output file.", e);
        }
    }

    private void toYaml(Object yamlable, String filename) {
        if (!Option.OUTPUT.isSet("yaml", "all")) {
            return;
        }

        LOGGER.fine("Creating " + filename);
        String yaml = "";
        if (yamlable instanceof YAMLable) {
            yaml = ((YAMLable) yamlable).toYAML();
        }
        else {
            yaml = new Yaml().dump(yamlable);
        }
        try {
            FileUtils.writeStringToFile(new File(filename), yaml, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create " + filename + " output file.", e);
        }
    }

    private void toDot(Dottable dottable, String fileName) {
        // NOTE png files require dot files as an intermediate format
        if (!Option.OUTPUT.isSet("dot", "png", "all")) {
            return;
        }

        String dot = dottable.toDot(false);
        writeDotFile(dot, fileName);
    }

    private void writeDotFile(String dot, String fileName) {
        // NOTE png files require dot files as an intermediate format
        if (!Option.OUTPUT.isSet("dot", "png", "all")) {
            return;
        }

        if (StringUtils.isEmpty(dot) || dot.equals("digraph g {\n}\n")) {
            return;
        }

        try {
            LOGGER.fine("Creating " + fileName);
            FileUtils.writeStringToFile(new File(fileName), dot, "UTF-8");
            dotFiles.add(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create " + fileName + " output file.", e);
        }
    }

    private void generateOutputForTestSuiteInfo(Set<TestSuiteInfo> suites, String outputDirectory) {
        for (TestSuiteInfo suite : suites) {
            if (Option.MODE.isSet("all", "suites")) {
                LOGGER.fine("Generating reports for " + suite.getName());
                String fileName = suite.getName().replace(" ", "");
                toDot(suite, outputDirectory + "/suites/" + fileName + ".dot");
                toJson(suite, outputDirectory + "/suites/" + fileName + ".json");
                toYaml(suite, outputDirectory + "/suites/" + fileName + ".yaml");
            }

            doOutputForTestInfo(outputDirectory, suite);
        }
    }

    private void doOutputForTestInfo(String outputDirectory, TestSuiteInfo suiteInfo) {
        for (TestInfo testInfo : suiteInfo.getTests()) {
            if (Option.MODE.isSet("all", "suites", "tests")) {
                LOGGER.fine("Generating reports for " + testInfo.getName());
                String fileName = testInfo.getName().replace(" ", "");
                toDot(testInfo, outputDirectory + "/tests/" + fileName + ".dot");
                toJson(testInfo, outputDirectory + "/tests/" + fileName + ".json");
                toYaml(testInfo, outputDirectory + "/tests/" + fileName + ".yaml");
            }

            doOutputForTestClassInfo(outputDirectory, testInfo);
        }
    }

    private void doOutputForTestClassInfo(String outputDirectory, TestInfo testInfo) {
        for (TestClassInfo classInfo : testInfo.getTestClasses()) {
            if (Option.MODE.isSet("all", "suites", "tests", "classes")) {
                LOGGER.fine("Generating reports for " + classInfo.getName());
                String fileName = classInfo.getName();
                toDot(classInfo, outputDirectory + "/classes/" + fileName + ".dot");
                toJson(classInfo, outputDirectory + "/classes/" + fileName + ".json");
                toYaml(classInfo, outputDirectory + "/classes/" + fileName + ".yaml");
            }

            doOutputForMethodInfo(outputDirectory, classInfo);
        }
    }

    private void doOutputForMethodInfo(String outputDirectory, TestClassInfo classInfo) {
        if (!Option.MODE.isSet("all", "suites", "tests", "classes", "methods")) {
            return;
        }

        for (TestMethodInfo method : classInfo.getTestMethods()) {
            LOGGER.fine("Generating reports for " + method.getMethodName());
            String fileName = classInfo.getName() + "." + method.getMethodName();
            toDot(method, outputDirectory + "/methods/" + fileName + ".dot");
            toJson(method, outputDirectory + "/methods/" + fileName + ".json");
            toYaml(method, outputDirectory + "/methods/" + fileName + ".yaml");
        }
    }

    private void generatePngFromDotFiles() {
        if (!Option.OUTPUT.isSet("png", "all")) {
            return;
        }

        for (String dotFile : dotFiles) {
            dotFile = FilenameUtils.getFullPath(dotFile) + FilenameUtils.getName(dotFile);
            try {
                final String cmd = "/usr/local/bin/dot "
                        + dotFile
                        + " -Grankdir=LR -Tpng -o "
                        + StringUtils.removeEnd(dotFile, ".dot").concat(".png");

                Process p = Runtime.getRuntime().exec(cmd, null);
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                LOGGER.severe("Error generating png file due to " + e.getMessage());
                throw new RuntimeException("Error generating png file", e);
            } finally {
                // Delete the dot file if it is not a requested output format
                if (!Option.OUTPUT.isSet("dot", "all")) {
                    LOGGER.fine("deleting -> " + dotFile);
                    FileUtils.deleteQuietly(new File(dotFile));
                }
            }
        }
    }

    private void logCompletion(Set<TestSuiteInfo> suites) {
        for (TestSuiteInfo suite : suites) {
            LOGGER.info("Dependency report generation complete for " + suite.getName());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (Option.PRESCAN.isSet("false") || Option.ENABLED.isSet("false")) {
            return;
        }
        throw new SkipException("Skipped for suite analysis");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (Option.PRESCAN.isSet("false") || Option.ENABLED.isSet("false")) {
            return;
        }
        // result.setStatus(ITestResult.SUCCESS);
        // result.setThrowable(null);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        if (Option.PRESCAN.isSet("false") || Option.ENABLED.isSet("false")) {
            return;
        }
        // TODO :: figure out why status SKIP breaks some suites
        itr.setStatus(ITestResult.SUCCESS);
    }

    @Override
    public void beforeConfiguration(ITestResult tr) {
        if (Option.PRESCAN.isSet("false") || Option.ENABLED.isSet("false")) {
            return;
        }
        throw new SkipException("Skipped for suite analysis");
    }

}
