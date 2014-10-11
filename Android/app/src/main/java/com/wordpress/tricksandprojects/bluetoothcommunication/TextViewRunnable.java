package com.wordpress.tricksandprojects.bluetoothcommunication;

import android.widget.TextView;

public class TextViewRunnable implements Runnable {

    private final String text;
    private final TextView textView;

    public TextViewRunnable( TextView textView, String text ){

        this.text = text;
        this.textView = textView;

    }

    @Override
    public void run() {
        this.textView.setText(text);
    }
}
