package edu.univ.erp.api.types;

public record StudentRow(int studentId,String name,int rollNumber,String program){@Override public String toString(){return name+" ("+rollNumber+")";}}
