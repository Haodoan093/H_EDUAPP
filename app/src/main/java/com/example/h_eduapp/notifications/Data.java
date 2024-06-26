package com.example.h_eduapp.notifications;

public class Data {

    private String user,body, titile, sent,notificationType;

    private Integer icon;

    public Data() {
    }

    public Data(String user, String body, String titile, String sent, String notificationType, Integer icon) {
        this.user = user;
        this.body = body;
        this.titile = titile;
        this.sent = sent;
        this.notificationType = notificationType;
        this.icon = icon;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Data(String user, String body, String titile, String sent, Integer icon) {
        this.user = user;
        this.body = body;
        this.titile = titile;
        this.sent = sent;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitile() {
        return titile;
    }

    public void setTitile(String titile) {
        this.titile = titile;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
