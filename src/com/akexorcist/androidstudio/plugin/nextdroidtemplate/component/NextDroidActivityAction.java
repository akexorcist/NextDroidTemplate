package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.constant.TemplateProperties;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateClassAndLayoutDialog;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.WriteAction;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.Properties;

public class NextDroidActivityAction extends AnAction {
    private static final String TEMPLATE_PATH = "/templates/activity/";
    private static final String TEMPLATE_NAME_ACTIVITY = "KotlinMvvmCustomActivity";
    private static final String TEMPLATE_NAME_VIEW_MODEL = "KotlinMvvmCustomViewModel";
    private static final String TEMPLATE_NAME_LAYOUT = "layout_activity_custom";
    private static final String PREFIX_LAYOUT_NAME = "activity_";

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
        showDialog(file, project, selectedPackage);
    }

    private void showDialog(VirtualFile file, Project project, String selectedPackage) {
        CreateClassAndLayoutDialog dialog = new CreateClassAndLayoutDialog();
        dialog.setCurrentProject(project);
        dialog.setCurrentVirtualFile(file);
        dialog.setSelectedPackage(selectedPackage);
        dialog.setPrefixLayoutName(PREFIX_LAYOUT_NAME);
        dialog.setValidationClassAndLayoutName(new String[]{"%sActivity.kt", "%sViewModel.kt"}, "%s.xml");
        dialog.setTitle("Create Activity class");
        dialog.addOkClickListener(this::createNextDroidApiClass);
        dialog.setVisible(true);
    }

    private void createNextDroidApiClass(String selectedPackage, String className, String layoutName, boolean includeLayout) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null) {
            String javaPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/java/" + selectedPackage.replaceAll("\\.", "/");
            String layoutPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/res/layout/";
            addActivityTemplate(file, project, className, layoutName, javaPath, includeLayout);
            addViewModelTemplate(project, className, javaPath);
            addAndroidManifest(file, className, selectedPackage);
            if (includeLayout) {
                addLayoutTemplate(project, layoutName, layoutPath);
            }
        } else {
            System.out.println("Module not found");
        }
    }

    private void addActivityTemplate(VirtualFile file, Project project, String className, String layoutName, String targetPath, boolean includeLayout) {
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        properties.setProperty(TemplateProperties.LAYOUT_NAME, includeLayout ? "R.layout." + layoutName : "0");
        properties.setProperty(TemplateProperties.APP_PACKAGE_NAME, NextDroidTemplateUtil.getAppPackageNameFromAndroidManifest(file));
        addFileTemplate(project, className + "Activity", TEMPLATE_NAME_ACTIVITY, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addViewModelTemplate(Project project, String className, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        addFileTemplate(project, className + "ViewModel", TEMPLATE_NAME_VIEW_MODEL, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addLayoutTemplate(Project project, String layoutName, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(project).getDefaultProperties();
        addFileTemplate(project, layoutName, TEMPLATE_NAME_LAYOUT, TEMPLATE_EXTENSION_XML, targetPath, properties);
    }

    /*
     * https://github.com/JorgeDC/AndroidManifestFitter
     */
    private void addAndroidManifest(VirtualFile file, String className, String packageName) {
        Document document = NextDroidTemplateUtil.getAndroidManifest(file);
        if (document != null) {
            String androidManifest = document.getCharsSequence().toString();
            androidManifest = androidManifest.replace("</application>", "    <activity android:name=\"" + packageName + "." + className + "Activity\" />\n    </application>");
            Runnable writeAction = new WriteAction(androidManifest, document);
            ApplicationManager.getApplication().runWriteAction(writeAction);
        }
    }

    private void addFileTemplate(Project project, String fileName, String templateName, String extension, String targetPath, Properties properties) {
        String targetSourcePath = NextDroidTemplateUtil.getSourceDirectoryPath(project, targetPath);
        NextDroidTemplateUtil.createDirectoryIfNotExist(project, targetSourcePath, new NextDroidTemplateUtil.DirectoryCreateListener() {
            @Override
            public void onComplete(boolean isSuccess, VirtualFile file) {
                if (isSuccess) {
                    FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
                    PsiDirectory directory = PsiManager.getInstance(project).findDirectory(file);
                    NextDroidTemplateUtil.addFileTemplate(fileTemplateManager, directory, getClass().getClassLoader(), fileName, templateName, extension, TEMPLATE_PATH, properties);
                }
            }
        });
    }
}
