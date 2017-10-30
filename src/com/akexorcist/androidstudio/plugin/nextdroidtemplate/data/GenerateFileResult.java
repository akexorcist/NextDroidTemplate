package com.akexorcist.androidstudio.plugin.nextdroidtemplate.data;

import com.intellij.openapi.module.Module;

public class GenerateFileResult {
    private Module selectedModule;
    private String packageName;
    private String className;
    private String layoutName;
    private String javaDirectoryPath;
    private String layoutDirectoryPath;
    private boolean includeLayout;

    public GenerateFileResult() {
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public GenerateFileResult setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public GenerateFileResult setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public GenerateFileResult setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public GenerateFileResult setLayoutName(String layoutName) {
        this.layoutName = layoutName;
        return this;
    }

    public String getJavaDirectoryPath() {
        return javaDirectoryPath;
    }

    public GenerateFileResult setJavaDirectoryPath(String javaDirectoryPath) {
        this.javaDirectoryPath = javaDirectoryPath;
        return this;
    }

    public String getLayoutDirectoryPath() {
        return layoutDirectoryPath;
    }

    public GenerateFileResult setLayoutDirectoryPath(String layoutDirectoryPath) {
        this.layoutDirectoryPath = layoutDirectoryPath;
        return this;
    }

    public boolean isIncludeLayout() {
        return includeLayout;
    }

    public GenerateFileResult setIncludeLayout(boolean includeLayout) {
        this.includeLayout = includeLayout;
        return this;
    }
}
