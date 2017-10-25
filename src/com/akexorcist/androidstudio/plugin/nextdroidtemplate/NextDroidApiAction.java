package com.akexorcist.androidstudio.plugin.nextdroidtemplate;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NextDroidApiAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.print(ActionManager.getInstance().getId(this) + " was clicked");
    }
}
