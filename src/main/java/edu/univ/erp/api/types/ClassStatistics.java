package edu.univ.erp.api.types;

import java.util.Map;

public record ClassStatistics(double averageScore,int minScore,int maxScore,double stdDev,int studentCount,Map<String,Integer>gradeDistribution){}
