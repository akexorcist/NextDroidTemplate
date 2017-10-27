package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.vfs.VirtualFile;

public class NextDroidGroupAction extends DefaultActionGroup {

    @Override
    public void update(AnActionEvent event) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        if (file != null &&
                file.getCanonicalPath() != null &&
                (file.getCanonicalPath().contains("/java/") ||
                        file.getCanonicalPath().endsWith("/java"))) {
            event.getPresentation().setVisible(true);
        } else {
            event.getPresentation().setVisible(false);
        }
    }
}
