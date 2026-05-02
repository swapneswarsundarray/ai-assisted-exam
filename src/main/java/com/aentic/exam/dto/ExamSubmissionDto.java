package com.aentic.exam.dto;

import java.util.Map;

public class ExamSubmissionDto {
    private String examCode;
    private Map<Long, String> answers;
    private Integer totalTimeSeconds;

    public ExamSubmissionDto() {}

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public Map<Long, String> getAnswers() { return answers; }
    public void setAnswers(Map<Long, String> answers) { this.answers = answers; }

    public Integer getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(Integer totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }
}
