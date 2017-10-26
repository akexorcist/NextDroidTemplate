package com.akexorcist.androidstudio.plugin.nextdroidtemplate.util;

import com.intellij.openapi.editor.Document;

/*
 * https://github.com/JorgeDC/AndroidManifestFitter
 */

public class WriteAction implements Runnable {
    private String text;
    private Document document;

    public WriteAction(String text, Document document) {
        this.text = text;
        this.document = document;
    }

    public void run() {
        document.setText(text);
    }
}
