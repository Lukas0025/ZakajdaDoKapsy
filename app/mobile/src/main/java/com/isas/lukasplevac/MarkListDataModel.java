package com.isas.lukasplevac;

public class MarkListDataModel {

    String mark;
    String subject;
    String desc;

    public MarkListDataModel(String mark, String subject, String desc) {
        this.mark = mark;
        this.subject = subject;
        this.desc = desc;

    }

    public String getMark() {
        return mark;
    }

    public String getSubject() {
        return subject;
    }

    public String getDesc() {
        return desc;
    }
}