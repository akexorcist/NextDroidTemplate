package com.akexorcist.androidstudio.plugin.nextdroidtemplate.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.SourceVersion;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NextDroidTemplateUtil {

    public static void addFileTemplate(FileTemplateManager fileTemplateManager, PsiDirectory targetDirectory, ClassLoader classLoader, String fileName, String templateName, String extension, String templatePath, Properties properties) {
        FileTemplate template = fileTemplateManager.getTemplate(templateName);
        if (template != null) {
            fileTemplateManager.removeTemplate(template);
        }
        try {
            template = fileTemplateManager.addTemplate(templateName, extension);
            template.setText(FileUtil.loadTextAndClose(new InputStreamReader(classLoader.getResourceAsStream(templatePath + templateName + "." + extension + ".ft"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileTemplateUtil.createFromTemplate(template, fileName, properties, targetDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isClassNameValid(String className) {
        return SourceVersion.isIdentifier(className) && !SourceVersion.isKeyword(className);
    }

    public static boolean isLayoutNameValid(String layoutName) {
        return layoutName != null &&
                StringUtils.isNotEmpty(layoutName) &&
                !StringUtils.isNumeric(layoutName.charAt(0) + "") &&
                !StringUtils.equals(layoutName.charAt(0) + "", "_") &&
                layoutName.matches("^[a-z0-9_]+$");
    }

    @Deprecated
    public static String getDirectoryPath(VirtualFile file) {
        String path = file.getCanonicalPath();
        if (path != null && !file.isDirectory()) {
            return path.replace("/" + file.getName(), "");
        } else {
            return path;
        }
    }

    public static VirtualFile getDirectory(VirtualFile file) {
        if (file != null && !file.isDirectory()) {
            return file.getParent();
        } else {
            return file;
        }
    }

    public static String getPackagePath(String selectedDirectory, String projectDirectory) {
        if (selectedDirectory.contains("/java")) {
            return selectedDirectory.replace(projectDirectory + "/", "")
                    .replaceAll("/", ".");
        } else {
            return "";
        }
    }

    public static String getJavaDirectoryPath(String selectedDirectory) {
        if (selectedDirectory.contains("/java/")) {
            return selectedDirectory.replaceAll("/java/.+", "/java");
        } else {
            return selectedDirectory;
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

    public static Document getAndroidManifest(Module module) {
        VirtualFile moduleFile = NextDroidTemplateUtil.getModuleVirtualFile(module);
        VirtualFile androidManifestFile = moduleFile.findChild("src").findChild("main").findChild("AndroidManifest.xml");
        Document document = FileDocumentManager.getInstance().getCachedDocument(androidManifestFile);
        if (document != null && document.isWritable()) {
            return document;
        } else {
            return null;
        }
    }

    public static String getAppPackageNameFromAndroidManifest(Module module) {
        Document document = NextDroidTemplateUtil.getAndroidManifest(module);
        if (document != null) {
            String androidManifest = document.getCharsSequence().toString();
            String regex = "package=\"(.+)\">";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(androidManifest);
            if (matcher.find()) {
                try {
                    return matcher.group(1);
                } catch (Exception ignored) {
                }
            }
        }
        return "";
    }

    public static VirtualFile getModuleVirtualFile(Module module) {
        return ModuleRootManager.getInstance(module).getContentRoots()[0];
    }

    public static void createDirectoryIfNotExist(Module module, String directoryPath, DirectoryCreateListener listener) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            VirtualFile directory = NextDroidTemplateUtil.getModuleVirtualFile(module);
            String subDirectoryPath = directoryPath.replace(directory.getCanonicalPath(), "");
            String[] folders = subDirectoryPath.split("/");
            for (String childFolder : folders) {
                VirtualFile childDirectory = directory.findChild(childFolder);
                if (childDirectory != null && childDirectory.isDirectory()) {
                    directory = childDirectory;
                } else if (!childFolder.isEmpty()) {
                    try {
                        directory = directory.createChildDirectory(module.getProject(), childFolder);
                    } catch (IOException ignored) {
                    }
                }
            }
            listener.onComplete(directory);
        });
    }

    public interface DirectoryCreateListener {
        void onComplete(VirtualFile file);
    }
}
