package com.djacoronel.tasknotes;

public class Tag {
    private String name, color, pinned;
    private long id;

    public Tag(long id, String name, String color, String pinned){
        this.id = id;
        this.name = name;
        this.color = color;
        this.pinned = pinned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPinned() {
        return pinned;
    }

    public void setPinned(String pinned) {
        this.pinned = pinned;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
