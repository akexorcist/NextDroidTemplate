package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui.CreateApiDialog;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

public class NextDroidApiAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());
        Project project = anActionEvent.getProject();
        String projectDirectory = project != null ? project.getBasePath() : "";
        String directoryPath = NextDroidTemplateUtil.getDirectoryPath(file);
        String selectedPackage = NextDroidTemplateUtil.getPackagePath(directoryPath, projectDirectory);
        showDialog(file, anActionEvent.getProject(), selectedPackage);
    }

    private void showDialog(VirtualFile file, Project project, String selectedPackage) {
        CreateApiDialog dialog = new CreateApiDialog();
        dialog.setCurrentProject(project);
        dialog.setVirtualFile(file);
        dialog.setSelectedPackage(selectedPackage);
        dialog.setTitle("Create API class");
        dialog.addOkClickListener(this::createNextDroidApiClass);
        dialog.setVisible(true);
    }

    private void createNextDroidApiClass(VirtualFile file, Project project, String selectedPackage, String className) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);

        if (module != null) {
            String targetPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/java/" + selectedPackage.replaceAll("\\.", "/");
            System.out.println("File : " + className);
            System.out.println("Target : " + targetPath);
            addFileTemplate(project, className, targetPath);
        }

    }

    private void addFileTemplate(Project project, String targetName, String targetPath) {
        String templateName = "NextActivity";
        String extension = "kt";
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(LocalFileSystem.getInstance().findFileByPath(targetPath));
        NextDroidTemplateUtil.addFileTemplate(fileTemplateManager, directory, getClass().getClassLoader(), targetName, templateName, extension);
    }
}
