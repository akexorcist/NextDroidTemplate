package com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileResult;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.event.TextChangeListener;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.google.common.base.CaseFormat;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;

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
    private JTextField textFieldClassName;
    private JTextField textFieldPackagePath;
    private JTextField textFieldLayoutName;
    private JLabel labelLayoutName;
    private JLabel textViewClassNameInvalid;
    private JLabel textViewLayoutNameInvalid;
    private JCheckBox checkBoxIncludeLayout;

    //    private VirtualFile currentFile;
    //    private Project currentProject;
    private GenerateFileRequest request;
    private String prefixLayoutName;

    // Validate
    private String[] validationClassNames;
    private String[] validationLayoutNames;

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

    public void setGenerateFileRequest(GenerateFileRequest request) {
        this.request = request;
        textFieldPackagePath.setText(request.getSelectedPackage());
    }

    public void setPrefixLayoutName(String prefixLayoutName) {
        this.prefixLayoutName = prefixLayoutName;
    }


    public void setValidationClassAndLayoutName(String[] validationClassNames, String[] validationLayoutName) {
        this.validationClassNames = validationClassNames;
        this.validationLayoutNames = validationLayoutName;
    }

    public void addOkClickListener(OnOkClickListener listener) {
        this.onOkClickListener = listener;
    }

    public void addCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    private void onOK() {
        String selectedPackage = textFieldPackagePath.getText();
        String className = textFieldClassName.getText();
        String layoutName = textFieldLayoutName.getText();
        if (validateFilePath(selectedPackage, className, layoutName)) {
            if (onOkClickListener != null) {
                GenerateFileResult result = new GenerateFileResult()
                        .setPackageName(selectedPackage)
                        .setClassName(className)
                        .setLayoutName(layoutName)
                        .setSelectedModule(request.getSelectedModule())
                        .setJavaDirectoryPath(request.getJavaDirectoryPath())
                        .setLayoutDirectoryPath(request.getLayoutDirectoryPath())
                        .setIncludeLayout(checkBoxIncludeLayout.isSelected());
                onOkClickListener.onButtonOkClick(result);
            }
            dispose();
        }
    }

    private boolean validateFilePath(String selectedPackage, String className, String layoutName) {
        selectedPackage = selectedPackage.replaceAll("\\.", "/");
        for (String validationClassName : validationClassNames) {
            String classPath = request.getJavaDirectoryPath() + "/" + selectedPackage.replaceAll("\\.", "/") + "/" + String.format(validationClassName, className);
            if (isFileDuplicated(classPath)) {
                showClassFileAlreadyExistMessage(classPath);
                return false;
            }
        }
        for (String validationLayoutName : validationLayoutNames) {
            String layoutPath = request.getLayoutDirectoryPath() + "/" + String.format(validationLayoutName, layoutName);
            if (isFileDuplicated(layoutPath)) {
                showLayoutFileAlreadyExistMessage(layoutPath);
                return false;
            }
        }
        return true;
    }

    private void onCancel() {
        if (onCancelClickListener != null) {
            onCancelClickListener.onButtonCancelClick();
        }
        dispose();
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
        void onButtonOkClick(GenerateFileResult result);
    }

    public interface OnCancelClickListener {
        void onButtonCancelClick();
    }
}
