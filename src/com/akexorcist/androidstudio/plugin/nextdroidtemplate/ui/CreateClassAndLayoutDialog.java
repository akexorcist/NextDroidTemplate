package com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.event.TextChangeListener;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.google.common.base.CaseFormat;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateClassAndLayoutDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonPackagePath;
    private JTextField textFieldClassName;
    private JTextField textFieldPackagePath;
    private JTextField textFieldLayoutName;
    private JLabel labelLayoutName;
    private JLabel textViewClassNameInvalid;
    private JLabel textViewLayoutNameInvalid;
    private JCheckBox checkBoxIncludeLayout;

    private VirtualFile currentFile;
    private Project currentProject;
    private String selectedPackage;
    private String prefixLayoutName;

    // Validate
    private String[] validationClassNames;
    private String validationLayoutName;

    private OnOkClickListener onOkClickListener;
    private OnCancelClickListener onCancelClickListener;

    public CreateClassAndLayoutDialog() {
        setupDialogWindow();
        checkFileNameValid();
        setupView();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void setupDialogWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setPreferredSize(new Dimension(400, 250));
        pack();
        setLocationRelativeTo(null);
    }

    private void setupView() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        buttonPackagePath.addActionListener(e -> onSelectPackageClick());
        textFieldClassName.getDocument().addDocumentListener((TextChangeListener) event -> {
            checkFileNameValid();
            updateLayoutName();
        });
        textFieldLayoutName.getDocument().addDocumentListener((TextChangeListener) event -> {
            checkFileNameValid();
        });
        checkBoxIncludeLayout.addItemListener(event -> {
            checkFileNameValid();
            checkLayoutNameVisibility(event.getStateChange() == ItemEvent.SELECTED);
        });
    }

    public void setSelectedPackage(String selectedPackage) {
        this.selectedPackage = selectedPackage;
        textFieldPackagePath.setText(selectedPackage);
    }

    public void setPrefixLayoutName(String prefixLayoutName) {
        this.prefixLayoutName = prefixLayoutName;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public void setCurrentVirtualFile(VirtualFile currentVirtualFile) {
        this.currentFile = currentVirtualFile;
    }

    public void setValidationClassAndLayoutName(String[] validationClassNames, String validationLayoutName) {
        this.validationClassNames = validationClassNames;
        this.validationLayoutName = validationLayoutName;
    }

    public void addOkClickListener(OnOkClickListener listener) {
        this.onOkClickListener = listener;
    }

    public void addCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    private void onOK() {
        selectedPackage = textFieldPackagePath.getText();
        String className = textFieldClassName.getText();
        String layoutName = textFieldLayoutName.getText();
        if (validateFilePath(selectedPackage, className, layoutName)) {
            boolean includeLayout = checkBoxIncludeLayout.isSelected();
            if (onOkClickListener != null) {
                onOkClickListener.onButtonOkClick(selectedPackage, className, layoutName, includeLayout);
            }
            dispose();
        }
    }

    private boolean validateFilePath(String selectedPackage, String className, String layoutName) {
        selectedPackage = selectedPackage.replaceAll("\\.", "/");
        Module module = ModuleUtilCore.findModuleForFile(currentFile, currentProject);
        for (String validationClassName : validationClassNames) {
            String classPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/java/" + selectedPackage.replaceAll("\\.", "/") + "/" + String.format(validationClassName, className);
            if (isFileDuplicated(classPath)) {
                showClassFileAlreadyExistMessage(classPath);
                return false;
            }
        }
        String layoutPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath() + "/src/main/res/layout/" + String.format(validationLayoutName, layoutName);
        if (isFileDuplicated(layoutPath)) {
            showLayoutFileAlreadyExistMessage(layoutPath);
            return false;
        }
        return true;
    }

    private void onCancel() {
        if (onCancelClickListener != null) {
            onCancelClickListener.onButtonCancelClick();
        }
        dispose();
    }

    private void onSelectPackageClick() {
        PackageChooserDialog chooserDialog = new PackageChooserDialog("Select package", currentProject);
        chooserDialog.selectPackage(selectedPackage);
        chooserDialog.show();
        textFieldPackagePath.setText(chooserDialog.getSelectedPackage().getQualifiedName());
    }

    private void updateLayoutName() {
        String className = textFieldClassName.getText().replaceAll("[^A-Za-z0-9]", "");
        String classNameLowerUnderScore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
        textFieldLayoutName.setText(prefixLayoutName + classNameLowerUnderScore);
    }

    private void showClassFileAlreadyExistMessage(String path) {
        Messages.showErrorDialog("Cannot create class\n'" + path + "'\nFile already exists.", "Cannot Create Class");
    }

    private void showLayoutFileAlreadyExistMessage(String path) {
        Messages.showErrorDialog("Cannot create layout\n'" + path + "'\nFile already exists.", "Cannot Create Layout");
    }

    private void checkLayoutNameVisibility(boolean isVisible) {
        textFieldLayoutName.setVisible(isVisible);
        labelLayoutName.setVisible(isVisible);
    }

    private void checkFileNameValid() {
        boolean includeLayout = checkBoxIncludeLayout.isSelected();
        if (isClassNameValid()) {
            textViewClassNameInvalid.setVisible(false);
        } else {
            textViewClassNameInvalid.setVisible(true);
        }

        if (includeLayout && !isLayoutNameValid()) {
            textViewLayoutNameInvalid.setVisible(true);
        } else {
            textViewLayoutNameInvalid.setVisible(false);
        }

        if (isClassNameValid() && (!includeLayout | isLayoutNameValid())) {
            buttonOK.setEnabled(true);
        } else {
            buttonOK.setEnabled(false);
        }
    }

    private boolean isClassNameValid() {
        return NextDroidTemplateUtil.isClassNameValid(textFieldClassName.getText());
    }

    private boolean isLayoutNameValid() {
        return NextDroidTemplateUtil.isLayoutNameValid(textFieldLayoutName.getText());
    }

    private boolean isFileDuplicated(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path) != null;
    }

    public static void main(String[] args) {
        CreateClassAndLayoutDialog dialog = new CreateClassAndLayoutDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public interface OnOkClickListener {
        void onButtonOkClick(String selectedPackage, String className, String layoutName, boolean includeLayout);
    }

    public interface OnCancelClickListener {
        void onButtonCancelClick();
    }
}
