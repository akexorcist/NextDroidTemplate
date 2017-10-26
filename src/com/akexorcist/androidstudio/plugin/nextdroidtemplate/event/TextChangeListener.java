package com.akexorcist.androidstudio.plugin.nextdroidtemplate.event;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface TextChangeListener extends DocumentListener {
    void onTextChanged(DocumentEvent event);

    @Override
    default void insertUpdate(DocumentEvent event) {
        onTextChanged(event);
    }

    @Override
    default void removeUpdate(DocumentEvent event) {
        onTextChanged(event);
    }

    @Override
    default void changedUpdate(DocumentEvent event) {
        onTextChanged(event);
    }
}
