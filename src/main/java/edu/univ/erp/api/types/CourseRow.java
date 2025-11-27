package edu.univ.erp.api.types;

public record CourseRow(String code,String title,int credits){@Override public String toString(){return code+" - "+title;}}
