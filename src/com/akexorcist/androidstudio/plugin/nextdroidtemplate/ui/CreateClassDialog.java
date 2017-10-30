package com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileRequest;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.data.GenerateFileResult;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.event.TextChangeListener;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateClassDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldClassName;
    private JTextField textFieldPackagePath;
    private JLabel textViewClassNameInvalid;

    private GenerateFileRequest request;

    // Validate
    private String[] validationClassNames;

    private CreateClassAndLayoutDialog.OnOkClickListener onOkClickListener;
    private CreateClassAndLayoutDialog.OnCancelClickListener onCancelClickListener;

    public CreateClassDialog() {
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
        setPreferredSize(new Dimension(400, 180));
        pack();
        setLocationRelativeTo(null);
    }

    private void setupView() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        textFieldClassName.getDocument().addDocumentListener((TextChangeListener) event -> {
            checkFileNameValid();
        });
    }

    public void setGenerateFileRequest(GenerateFileRequest request) {
        this.request = request;
        textFieldPackagePath.setText(request.getSelectedPackage());
    }

    public void setValidationClassAndLayoutName(String[] validationClassNames) {
        this.validationClassNames = validationClassNames;
    }

    public void addOkClickListener(CreateClassAndLayoutDialog.OnOkClickListener listener) {
        this.onOkClickListener = listener;
    }

    public void addCancelClickListener(CreateClassAndLayoutDialog.OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    private void onOK() {
        String selectedPackage = textFieldPackagePath.getText();
        String className = textFieldClassName.getText();
        if (validateFilePath(selectedPackage, className)) {
            if (onOkClickListener != null) {
                GenerateFileResult result = new GenerateFileResult()
                        .setPackageName(selectedPackage)
                        .setClassName(className)
                        .setLayoutName(null)
                        .setSelectedModule(request.getSelectedModule())
                        .setJavaDirectoryPath(request.getJavaDirectoryPath())
                        .setLayoutDirectoryPath(request.getLayoutDirectoryPath())
                        .setIncludeLayout(false);
                onOkClickListener.onButtonOkClick(result);
            }
            dispose();
        }
    }

    private boolean validateFilePath(String selectedPackage, String className) {
        selectedPackage = selectedPackage.replaceAll("\\.", "/");
        for (String validationClassName : validationClassNames) {
            String classPath = request.getJavaDirectoryPath() + "/" + selectedPackage.replaceAll("\\.", "/") + "/" + String.format(validationClassName, className);
            if (isFileDuplicated(classPath)) {
                showClassFileAlreadyExistMessage(classPath);
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

    private void showClassFileAlreadyExistMessage(String path) {
        Messages.showErrorDialog("Cannot create class\n'" + path + "'\nFile already exists.", "Cannot Create Class");
    }

    private void checkFileNameValid() {
        if (isClassNameValid()) {
            textViewClassNameInvalid.setVisible(false);
            buttonOK.setEnabled(true);
        } else {
            textViewClassNameInvalid.setVisible(true);
            buttonOK.setEnabled(false);
        }
    }

    private boolean isClassNameValid() {
        return NextDroidTemplateUtil.isClassNameValid(textFieldClassName.getText());
    }

    private boolean isFileDuplicated(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path) != null;
    }

    public interface OnOkClickListener {
        void onButtonOkClick(GenerateFileResult result);
    }

    public interface OnCancelClickListener {
        void onButtonCancelClick();
    }

    public static void main(String[] args) {
        CreateClassDialog dialog = new CreateClassDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
