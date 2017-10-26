package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.constant.TemplateProperties;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateApiDialog;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.WriteAction;
import com.google.common.base.CaseFormat;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NextDroidApiAction extends AnAction {
    private static final String TEMPLATE_PATH = "/templates/activity/";
    private static final String TEMPLATE_NAME_ACTIVITY = "KotlinMvvmCustomActivity";
    private static final String TEMPLATE_NAME_VIEW_MODEL = "KotlinMvvmCustomViewModel";
    private static final String TEMPLATE_NAME_LAYOUT = "layout_activity_custom";

    private static final String TEMPLATE_EXTENSION_KOTLIN = "kt";
    private static final String TEMPLATE_EXTENSION_XML = "xml";

    private VirtualFile file;
    private Project project;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        file = DataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());
        project = anActionEvent.getProject();
        String projectDirectory = project != null ? project.getBasePath() : "";
        String directoryPath = NextDroidTemplateUtil.getDirectoryPath(file);
        String selectedPackage = NextDroidTemplateUtil.getPackagePath(directoryPath, projectDirectory);
        showDialog(anActionEvent.getProject(), selectedPackage);
    }

    private void showDialog(Project project, String selectedPackage) {
        CreateApiDialog dialog = new CreateApiDialog();
        dialog.setCurrentProject(project);
        dialog.setSelectedPackage(selectedPackage);
        dialog.setTitle("Create API class");
        dialog.addOkClickListener(this::createNextDroidApiClass);
        dialog.setVisible(true);
    }

    private void createNextDroidApiClass(String selectedPackage, String className) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null) {
            String javaPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/java/" + selectedPackage.replaceAll("\\.", "/");
            String layoutPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/res/layout/";
            addActivityTemplate(file, project, className, javaPath);
            addViewModelTemplate(project, className, javaPath);
            addLayoutTemplate(project, className, layoutPath);
            addAndroidManifest(file, className, selectedPackage);
        } else {
            System.out.println("Module not found");
        }
    }

    private void addActivityTemplate(VirtualFile file, Project project, String className, String targetPath) {
        String classNameLowerUnderScore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        properties.setProperty(TemplateProperties.LAYOUT_NAME, "layout_activity_" + classNameLowerUnderScore);
        properties.setProperty(TemplateProperties.APP_PACKAGE_NAME, getAppPackageNameFromAndroidManifest(file));
        addFileTemplate(project, className + "Activity", TEMPLATE_NAME_ACTIVITY, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addViewModelTemplate(Project project, String className, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        addFileTemplate(project, className + "ViewModel", TEMPLATE_NAME_VIEW_MODEL, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addLayoutTemplate(Project project, String className, String targetPath) {
        String classNameLowerUnderScore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        addFileTemplate(project, "layout_activity_" + classNameLowerUnderScore, TEMPLATE_NAME_LAYOUT, TEMPLATE_EXTENSION_XML, targetPath, properties);
    }

    /*
     * https://github.com/JorgeDC/AndroidManifestFitter
     */
    private void addAndroidManifest(VirtualFile file, String className, String packageName) {
        Document document = getAndroidManifest(file);
        if (document != null) {
            String androidManifest = document.getCharsSequence().toString();
            androidManifest = androidManifest.replace("</application>", "    <activity android:name=\"" + packageName + "." + className + "Activity\" />\n    </application>");
            Runnable writeAction = new WriteAction(androidManifest, document);
            ApplicationManager.getApplication().runWriteAction(writeAction);
        }
    }

    private String getAppPackageNameFromAndroidManifest(VirtualFile file) {
        Document document = getAndroidManifest(file);
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

    private void addFileTemplate(Project project, String fileName, String templateName, String extension, String targetPath, Properties properties) {
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(LocalFileSystem.getInstance().findFileByPath(targetPath));
        NextDroidTemplateUtil.addFileTemplate(fileTemplateManager, directory, getClass().getClassLoader(), fileName, templateName, extension, TEMPLATE_PATH, properties);
    }

    private Document getAndroidManifest(VirtualFile file) {
        VirtualFile parent = file.getParent();
        if (parent != null) {
            int i = 100;
            while (i > 0 && parent != null && (!parent.getName().equals("main") && (!parent.getName().equals("java")) && (!parent.getName().equals("src")))) {
                parent = parent.getParent();
                i--;
            }
        }
        VirtualFile virtualFile[] = parent.getParent().getChildren();
        for (int i = 0; i < virtualFile.length; i++) {
            VirtualFile childFile = virtualFile[i];
            Document document = FileDocumentManager.getInstance().getCachedDocument(childFile);
            if (document != null && document.isWritable() && childFile.getPresentableName().toLowerCase().equals("androidmanifest.xml")) {
                return document;
            }
        }
        return null;
    }
}
