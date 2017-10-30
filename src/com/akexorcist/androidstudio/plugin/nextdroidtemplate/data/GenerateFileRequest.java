package com.akexorcist.androidstudio.plugin.nextdroidtemplate.data;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

public class GenerateFileRequest {
    private Module selectedModule;
    private VirtualFile selectedDirectory;
    private String selectedPackage;
    private String javaDirectoryPath;
    private String layoutDirectoryPath;

    public GenerateFileRequest() {
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public GenerateFileRequest setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
        return this;
    }

    public VirtualFile getSelectedDirectory() {
        return selectedDirectory;
    }

    public GenerateFileRequest setSelectedDirectory(VirtualFile selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
        return this;
    }

    public String getSelectedPackage() {
        return selectedPackage;
    }

    public GenerateFileRequest setSelectedPackage(String selectedPackage) {
        this.selectedPackage = selectedPackage;
        return this;
    }

    public String getJavaDirectoryPath() {
        return javaDirectoryPath;
    }

    public GenerateFileRequest setJavaDirectoryPath(String javaDirectoryPath) {
        this.javaDirectoryPath = javaDirectoryPath;
        return this;
    }

    public String getLayoutDirectoryPath() {
        return layoutDirectoryPath;
    }

    public GenerateFileRequest setLayoutDirectoryPath(String layoutDirectoryPath) {
        this.layoutDirectoryPath = layoutDirectoryPath;
        return this;
    }
}
