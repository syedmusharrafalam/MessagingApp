package com.studios.hourglass;

/**
 * Created by A. Pires on 17/05/2014.
 */
public class ThreadItem {

    private String name;
    private String text;
    private String msgCount;

    public ThreadItem(String name, String text, String msgCount) {
        this.name = name;
        this.text = text;
        this.msgCount = msgCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(String msgCount) {
        this.msgCount = msgCount;
    }
}
