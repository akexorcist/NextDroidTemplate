package com.akexorcist.androidstudio.plugin.nextdroidtemplate.component;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.StringTokenizer;

public class NextDroidActivityAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.print(ActionManager.getInstance().getId(this) + " was clicked");
    }

    public static PsiDirectory createDirectory(PsiDirectory parent, String name) throws IncorrectOperationException {
        PsiDirectory result = null;

        for (PsiDirectory dir : parent.getSubdirectories()) {
            if (dir.getName().equalsIgnoreCase(name)) {
                result = dir;
                break;
            }
        }

        if (null == result) {
            result = parent.createSubdirectory(name);
        }

        return result;
    } // createDirectory()

    public static PsiDirectory createPackage(PsiDirectory sourceDir, String qualifiedPackage)
            throws IncorrectOperationException {
        PsiDirectory parent = sourceDir;
        StringTokenizer token = new StringTokenizer(qualifiedPackage, ".");
        while (token.hasMoreTokens()) {
            String dirName = token.nextToken();
            parent = createDirectory(parent, dirName);
        }
        return parent;
    } // createPackage()


    @SuppressWarnings("unused")


    public static class View {
        private OnClickListener onClickListener;

        public void a() {
        }

        private void userClickThisButton() {
            if (onClickListener != null) {
                onClickListener.onClick(this);
            }
        }

        public void setOnClickListener(OnClickListener listener) {
            onClickListener = listener;
        }

        public interface OnClickListener {
            void onClick(View view);
        }
    }

    @SuppressWarnings("unused")
    public static class Button extends View {

        public interface OnClickListener {
            void onClick(View view);
        }

    }
}
