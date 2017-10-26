package com.akexorcist.androidstudio.plugin.nextdroidtemplate.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

import javax.lang.model.SourceVersion;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class NextDroidTemplateUtil {

    public static void addFileTemplate(FileTemplateManager fileTemplateManager, PsiDirectory targetDirectory, ClassLoader classLoader, String targetName, String templateName, String extension) {
        FileTemplate template = fileTemplateManager.getTemplate(templateName);
        if (template == null) {
            try {
                template = fileTemplateManager.addTemplate(templateName, extension);
                template.setText(FileUtil.loadTextAndClose(new InputStreamReader(classLoader.getResourceAsStream("/templates/" + templateName + "." + extension + ".ft"))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Properties properties = fileTemplateManager.getDefaultProperties();
            com.intellij.ide.fileTemplates.FileTemplateUtil.createFromTemplate(template, targetName, properties, targetDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isClassNameValid(String className) {
        return SourceVersion.isIdentifier(className) && !SourceVersion.isKeyword(className);
    }

    public static String getDirectoryPath(VirtualFile file) {
        String path = file.getCanonicalPath();
        if (path != null && !file.isDirectory()) {
            return path.replace("/" + file.getName(), "");
        } else {
            return path;
        }
    }

    public static String getPackagePath(String selectedDirectory, String projectDirectory) {
        if (selectedDirectory.contains("/java/")) {
            return selectedDirectory.replace(projectDirectory, "")
                    .replaceAll(".+/java/", "")
                    .replaceAll("/", ".");
        } else {
            return "";
        }
    }

    public static ArrayList<String> getBuildConfigList(Module module) {
        ArrayList<String> buildConfigList = new ArrayList<>();
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        for (VirtualFile virtualFile : rootManager.getSourceRoots()) {
            if (!virtualFile.toString().contains(module.getName() + "/build/") &&
                    !buildConfigList.contains(virtualFile.getName()) &&
                    virtualFile.toString().endsWith("/java")) {
                buildConfigList.add(virtualFile.getParent().getName());
            }
        }
        return buildConfigList;
    }
}
