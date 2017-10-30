package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.constant.TemplateProperties;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileResult;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateClassAndLayoutDialog;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.WriteAction;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;

import java.util.Properties;

public class NextDroidActivityAction extends BaseNextDroidClassAction {
    private static final String TEMPLATE_PATH = "/templates/activity/";
    private static final String TEMPLATE_NAME_ACTIVITY = "KotlinMvvmCustomActivity";
    private static final String TEMPLATE_NAME_VIEW_MODEL = "KotlinMvvmCustomViewModel";
    private static final String TEMPLATE_NAME_LAYOUT = "layout_activity_custom";
    private static final String PREFIX_LAYOUT_NAME = "activity_";

    @Override
    public void actionPerformed(AnActionEvent event) {
        super.actionPerformed(event);
    }

    @Override
    public String getTemplatePath() {
        return TEMPLATE_PATH;
    }

    @Override
    public String[] getValidationClassName() {
        return new String[]{"%sActivity.kt", "%sViewModel.kt"};
    }

    @Override
    public String[] getValidationLayoutName() {
        return new String[]{"%s.xml"};
    }

    @Override
    public void showDialog(GenerateFileRequest request) {
        CreateClassAndLayoutDialog dialog = new CreateClassAndLayoutDialog();
        dialog.setGenerateFileRequest(request);
        dialog.setPrefixLayoutName(PREFIX_LAYOUT_NAME);
        dialog.setValidationClassAndLayoutName(getValidationClassName(), getValidationLayoutName());
        dialog.setTitle("Create Activity class");
        dialog.addOkClickListener(this::createNextDroidActivityClass);
        dialog.setVisible(true);
    }

    private void createNextDroidActivityClass(GenerateFileResult result) {
        String javaPath = result.getJavaDirectoryPath() + "/" + result.getPackageName().replaceAll("\\.", "/");
        String layoutPath = result.getLayoutDirectoryPath();
        addActivityTemplate(result.getSelectedModule(), result.getClassName(), result.getLayoutName(), javaPath, result.isIncludeLayout());
        addViewModelTemplate(result.getSelectedModule(), result.getClassName(), javaPath);
        addActivityInAndroidManifest(result.getSelectedModule(), result.getClassName(), result.getPackageName());
        if (result.isIncludeLayout()) {
            addLayoutTemplate(result.getSelectedModule(), result.getLayoutName(), layoutPath);
        }
    }

    private void addActivityTemplate(Module module, String className, String layoutName, String targetPath, boolean includeLayout) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        properties.setProperty(TemplateProperties.LAYOUT_NAME, includeLayout ? "R.layout." + layoutName : "0");
        properties.setProperty(TemplateProperties.APP_PACKAGE_NAME, NextDroidTemplateUtil.getAppPackageNameFromAndroidManifest(module));
        addFileTemplate(module, className + "Activity", TEMPLATE_NAME_ACTIVITY, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addViewModelTemplate(Module module, String className, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        addFileTemplate(module, className + "ViewModel", TEMPLATE_NAME_VIEW_MODEL, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addLayoutTemplate(Module module, String layoutName, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        addFileTemplate(module, layoutName, TEMPLATE_NAME_LAYOUT, TEMPLATE_EXTENSION_XML, targetPath, properties);
    }

    /*
     * https://github.com/JorgeDC/AndroidManifestFitter
     */
    private void addActivityInAndroidManifest(Module module, String className, String packageName) {
        Document document = NextDroidTemplateUtil.getAndroidManifest(module);
        if (document != null) {
            String androidManifest = document.getCharsSequence().toString();
            androidManifest = androidManifest.replace("</application>", "    <activity android:name=\"" + packageName + "." + className + "Activity\" />\n    </application>");
            Runnable writeAction = new WriteAction(androidManifest, document);
            ApplicationManager.getApplication().runWriteAction(writeAction);
        }
    }
}
