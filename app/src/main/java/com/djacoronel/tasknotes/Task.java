package com.djacoronel.tasknotes;

class Task {
    private String title, text, date, priority, dateFinished , tag;
    private long id;

    Task(long id, String title, String text, String date, String priority) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date = date;
        this.priority = priority;
    }

    Task(long id, String title, String text, String date, String dateFinished, String priority, String tag) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date = date;
        this.dateFinished = dateFinished;
        this.priority = priority;
        this.tag = tag;
    }

    void update(String title, String text, String date, String priority, String tag){
        this.title = title;
        this.text = text;
        this.date = date;
        this.priority = priority;
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(String dateFinished) {
        this.dateFinished = dateFinished;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
