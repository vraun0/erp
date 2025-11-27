package edu.univ.erp.api.types;

import java.time.LocalDate;

public record AttendanceRow(int attendanceId,int enrollmentId,LocalDate date,String status){}
