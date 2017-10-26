package com.akexorcist.androidstudio.plugin.nextdroidtemplate.ui;

import com.akexorcist.androidstudio.plugin.nextdroidtemplate.event.TextChangeListener;
import com.akexorcist.androidstudio.plugin.nextdroidtemplate.util.NextDroidTemplateUtil;
import com.google.common.base.CaseFormat;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateActivityDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonPackagePath;
    private JTextField textFieldClassName;
    private JTextField textFieldPackagePath;
    private JTextField textFieldLayoutName;
    private JLabel textViewClassNameInvalid;

    private Project currentProject;
    private String selectedPackage;

    private OnOkClickListener onOkClickListener;
    private OnCancelClickListener onCancelClickListener;

    public CreateActivityDialog() {
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
        textFieldClassName.getDocument().addDocumentListener((TextChangeListener) event -> {
            checkClassNameValid();
            updateLayoutName();
        });
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    private void onOK() {
        selectedPackage = textFieldPackagePath.getText();
        String className = textFieldClassName.getText();
        String layoutName = textFieldLayoutName.getText();
        if (onOkClickListener != null) {
            onOkClickListener.onButtonOkClick(selectedPackage, className, layoutName);
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
        CreateActivityDialog dialog = new CreateActivityDialog();
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

    private void updateLayoutName() {
        String className = textFieldClassName.getText().replaceAll("[^A-Za-z0-9]", "");
        String classNameLowerUnderScore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
        textFieldLayoutName.setText("layout_activity_" + classNameLowerUnderScore);
    }

    private void checkClassNameValid() {
        if (NextDroidTemplateUtil.isClassNameValid(textFieldClassName.getText())) {
            textViewClassNameInvalid.setVisible(false);
            buttonOK.setEnabled(true);
        } else {
            textViewClassNameInvalid.setVisible(true);
            buttonOK.setEnabled(false);
        }
    }

    public interface OnOkClickListener {
        void onButtonOkClick(String selectedPackage, String className, String layoutName);
    }

    public interface OnCancelClickListener {
        void onButtonCancelClick();
    }
}
