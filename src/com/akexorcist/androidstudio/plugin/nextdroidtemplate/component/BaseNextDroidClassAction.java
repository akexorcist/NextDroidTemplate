package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.Properties;

public abstract class BaseNextDroidClassAction extends AnAction {
    public static final String TEMPLATE_EXTENSION_KOTLIN = "kt";
    public static final String TEMPLATE_EXTENSION_XML = "xml";

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        Module module = DataKeys.MODULE.getData(event.getDataContext());
        GenerateFileRequest request = createGenerateFileRequest(module, file);
        showDialog(request);
    }

    private GenerateFileRequest createGenerateFileRequest(Module module, VirtualFile file) {
        VirtualFile selectedDirectory = NextDroidTemplateUtil.getDirectory(file);
        String javaDirectoryPath = NextDroidTemplateUtil.getJavaDirectoryPath(file.getCanonicalPath());
        String selectedPackage = NextDroidTemplateUtil.getPackagePath(selectedDirectory.getCanonicalPath(), javaDirectoryPath);
        String layoutDirectoryPath = javaDirectoryPath.replaceAll("/java", "/res/layout");
        return new GenerateFileRequest()
                .setSelectedModule(module)
                .setSelectedDirectory(selectedDirectory)
                .setSelectedPackage(selectedPackage)
                .setJavaDirectoryPath(javaDirectoryPath)
                .setLayoutDirectoryPath(layoutDirectoryPath);
    }

    protected void addFileTemplate(Module module, String fileName, String templateName, String extension, String targetPath, Properties properties) {
        NextDroidTemplateUtil.createDirectoryIfNotExist(module, targetPath, new NextDroidTemplateUtil.DirectoryCreateListener() {
            @Override
            public void onComplete(VirtualFile file) {
                FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(module.getProject());
                PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(file);
                NextDroidTemplateUtil.addFileTemplate(fileTemplateManager, directory, getClass().getClassLoader(), fileName, templateName, extension, getTemplatePath(), properties);
            }
        });
    }

    public abstract void showDialog(GenerateFileRequest request);

    public abstract String getTemplatePath();

    public abstract String[] getValidationClassName();

    public abstract String[] getValidationLayoutName();

}
