package edu.univ.erp.api.types;

public record InstructorRow(int instructorId,String name,String department){@Override public String toString(){return name+" ("+department+")";}}
