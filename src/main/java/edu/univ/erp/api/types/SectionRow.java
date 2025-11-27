package edu.univ.erp.api.types;

public record SectionRow(int sectionId,String courseCode,String dayTime,String room,int capacity,int enrolled,String instructorName){@Override public String toString(){return courseCode+" - "+dayTime+" ("+room+")";}}
