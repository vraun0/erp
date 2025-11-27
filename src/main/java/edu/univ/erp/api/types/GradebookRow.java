package edu.univ.erp.api.types;

public record GradebookRow(int enrollmentId,int studentId,String studentName,int rollNumber,Integer midtermScore,Integer finalExamScore,Integer projectScore,Integer finalScore){}
