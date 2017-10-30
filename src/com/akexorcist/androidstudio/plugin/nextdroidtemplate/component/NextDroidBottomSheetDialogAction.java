package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.constant.TemplateProperties;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileResult;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateClassAndLayoutDialog;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;

import java.util.Properties;

public class NextDroidBottomSheetDialogAction extends BaseNextDroidClassAction {
    private static final String TEMPLATE_PATH = "/templates/dialog/bottom_sheet/";
    private static final String TEMPLATE_NAME_BOTTOM_SHEET_DIALOG = "KotlinCustomMvvmBottomSheetDialog";
    private static final String TEMPLATE_NAME_VIEW_MODEL = "KotlinCustomDialogViewModel";
    private static final String TEMPLATE_NAME_LAYOUT = "layout_bottom_sheet_dialog_custom";
    private static final String PREFIX_LAYOUT_NAME = "dialog_";

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
        return new String[]{"%BottomSheetDialog.kt", "%sDialogViewModel.kt"};
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
        dialog.setTitle("Create Bottom Sheet Dialog Class");
        dialog.addOkClickListener(this::createNextDroidFragmentClass);
        dialog.setVisible(true);
    }

    private void createNextDroidFragmentClass(GenerateFileResult result) {
        String javaPath = result.getJavaDirectoryPath() + "/" + result.getPackageName().replaceAll("\\.", "/");
        String layoutPath = result.getLayoutDirectoryPath();
        addBottomSheetDialogTemplate(result.getSelectedModule(), result.getClassName(), result.getLayoutName(), javaPath, result.isIncludeLayout());
        addViewModelTemplate(result.getSelectedModule(), result.getClassName(), javaPath);
        if (result.isIncludeLayout()) {
            addLayoutTemplate(result.getSelectedModule(), result.getLayoutName(), layoutPath);
        }
    }

    private void addBottomSheetDialogTemplate(Module module, String className, String layoutName, String targetPath, boolean includeLayout) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        properties.setProperty(TemplateProperties.LAYOUT_NAME, includeLayout ? "R.layout." + layoutName : "0");
        properties.setProperty(TemplateProperties.APP_PACKAGE_NAME, NextDroidTemplateUtil.getAppPackageNameFromAndroidManifest(module));
        addFileTemplate(module, className + "BottomSheetDialog", TEMPLATE_NAME_BOTTOM_SHEET_DIALOG, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addViewModelTemplate(Module module, String className, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        addFileTemplate(module, className + "DialogViewModel", TEMPLATE_NAME_VIEW_MODEL, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }

    private void addLayoutTemplate(Module module, String layoutName, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        addFileTemplate(module, layoutName, TEMPLATE_NAME_LAYOUT, TEMPLATE_EXTENSION_XML, targetPath, properties);
    }
}
