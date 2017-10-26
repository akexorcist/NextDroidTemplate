package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NextDroidRepositoryAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.print(ActionManager.getInstance().getId(this) + " was clicked");
    }
}
