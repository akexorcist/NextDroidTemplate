package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.constant.TemplateProperties;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileResult;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateClassDialog;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;

import java.util.Properties;

public class NextDroidPagerAdapterAction extends BaseNextDroidClassAction {
    private static final String TEMPLATE_PATH = "/templates/adapter/pager_adapter/";
    private static final String TEMPLATE_NAME_PAGER_ADAPTER = "KotlinMvvmCustomPagerAdapter";

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
        return new String[]{"%sPagerAdapter.kt"};
    }

    @Override
    public String[] getValidationLayoutName() {
        return new String[]{};
    }

    @Override
    public void showDialog(GenerateFileRequest request) {
        CreateClassDialog dialog = new CreateClassDialog();
        dialog.setGenerateFileRequest(request);
        dialog.setValidationClassAndLayoutName(getValidationClassName());
        dialog.setTitle("Create Pager Adapter Class");
        dialog.addOkClickListener(this::createNextDroidPagerAdapterClass);
        dialog.setVisible(true);
    }

    private void createNextDroidPagerAdapterClass(GenerateFileResult result) {
        String javaPath = result.getJavaDirectoryPath() + "/" + result.getPackageName().replaceAll("\\.", "/");
        addPagerAdapterTemplate(result.getSelectedModule(), result.getClassName(), javaPath);
    }

    private void addPagerAdapterTemplate(Module module, String className, String targetPath) {
        Properties properties = FileTemplateManager.getInstance(module.getProject()).getDefaultProperties();
        properties.setProperty(TemplateProperties.CLASS_NAME, className);
        addFileTemplate(module, className + "PagerAdapter", TEMPLATE_NAME_PAGER_ADAPTER, TEMPLATE_EXTENSION_KOTLIN, targetPath, properties);
    }
}
