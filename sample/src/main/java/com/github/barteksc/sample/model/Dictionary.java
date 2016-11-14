package com.github.barteksc.sample.model;

/**
 * Created by Nrtdemo-NB on 11/14/2016.
 */

public class Dictionary {
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefenition() {
        return defenition;
    }

    public void setDefenition(String defenition) {
        this.defenition = defenition;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    private String type;
    private String defenition;
    private String example;
}
