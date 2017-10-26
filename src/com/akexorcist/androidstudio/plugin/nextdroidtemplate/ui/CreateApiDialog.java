package com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.event.TextChangeListener;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateApiDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldClassName;
    private JTextField textFieldPackagePath;
    private JButton buttonPackagePath;
    private JLabel textViewClassNameInvalida;

    private VirtualFile virtualFile;
    private Project currentProject;
    private String selectedPackage;
    private String className;

    private OnOkClickListener onOkClickListener;
    private OnCancelClickListener onCancelClickListener;

    public CreateApiDialog() {
        setupDialogWindow();
        checkClassNameValid();
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
        setPreferredSize(new Dimension(400, 200));
        pack();
        setLocationRelativeTo(null);
    }

    private void setupView() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        buttonPackagePath.addActionListener(e -> onSelectPackageClick());
        textFieldClassName.getDocument().addDocumentListener((TextChangeListener) event -> checkClassNameValid());
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    private void onOK() {
        className = textFieldClassName.getText();
        selectedPackage = textFieldPackagePath.getText();
        if (onOkClickListener != null) {
            onOkClickListener.onButtonOkClick(virtualFile, currentProject, selectedPackage, className);
        }
        dispose();
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

    public static void main(String[] args) {
        CreateApiDialog dialog = new CreateApiDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


    public void setSelectedPackage(String selectedPackage) {
        this.selectedPackage = selectedPackage;
        textFieldPackagePath.setText(selectedPackage);
    }

    public void addOkClickListener(OnOkClickListener listener) {
        this.onOkClickListener = listener;
    }

    public void addCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    private void checkClassNameValid() {
        if (NextDroidTemplateUtil.isClassNameValid(textFieldClassName.getText())) {
            textViewClassNameInvalida.setVisible(false);
            buttonOK.setEnabled(true);
        } else {
            textViewClassNameInvalida.setVisible(true);
            buttonOK.setEnabled(false);
        }
    }

    public interface OnOkClickListener {
        void onButtonOkClick(VirtualFile file, Project project, String selectedPackage, String className);
    }

    public interface OnCancelClickListener {
        void onButtonCancelClick();
    }
}
