package com.aentic.exam.service;

import com.aentic.exam.entity.Answer;
import com.aentic.exam.entity.Exam;
import com.aentic.exam.entity.Report;
import com.aentic.exam.repository.AnswerRepository;
import com.aentic.exam.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    public Report generateReport(Exam exam) {
        Optional<Report> existingReport = reportRepository.findByExamId(exam.getId());
        if (existingReport.isPresent()) {
            return existingReport.get();
        }
        
        Report report = new Report();
        report.setExam(exam);
        
        List<Answer> answers = answerRepository.findByExamId(exam.getId());
        
        report.setTotalQuestions(answers.size());
        report.setCorrectAnswers((int) answers.stream().filter(Answer::getIsCorrect).count());
        report.setWrongAnswers(report.getTotalQuestions() - report.getCorrectAnswers());
        
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            long totalMinutes = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
            report.setTotalTimeMinutes((int) totalMinutes);
            
            if (report.getTotalQuestions() > 0) {
                report.setAverageTimePerQuestion((double) totalMinutes / report.getTotalQuestions());
            }
        }
        
        try {
            String examData = prepareExamDataForAI(exam, answers);
            String aiAnalysis = geminiService.generateReport(examData);
            
            // Parse AI response
            String[] lines = aiAnalysis.split("\n");
            for (String line : lines) {
                if (line.startsWith("STRENGTHS:")) {
                    report.setStrengthAreas(line.substring("STRENGTHS:".length()).trim());
                } else if (line.startsWith("IMPROVEMENTS:")) {
                    report.setImprovementAreas(line.substring("IMPROVEMENTS:".length()).trim());
                } else if (line.startsWith("RECOMMENDATIONS:")) {
                    report.setDetailedAnalysis(line.substring("RECOMMENDATIONS:".length()).trim());
                }
            }
        } catch (Exception e) {
            // Fallback to basic analysis if OpenAI fails
            report.setStrengthAreas(generateStrengthAreas(answers));
            report.setImprovementAreas(generateImprovementAreas(answers));
            report.setDetailedAnalysis(generateDetailedAnalysis(exam, answers));
        }
        
        return reportRepository.save(report);
    }
    
    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }
    
    public Optional<Report> getReportByCode(String reportCode) {
        return reportRepository.findByReportCode(reportCode);
    }
    
    public Optional<Report> getReportByExamId(Long examId) {
        return reportRepository.findByExamId(examId);
    }
    
    private String generateStrengthAreas(List<Answer> answers) {
        StringBuilder strengths = new StringBuilder();
        
        long correctCount = answers.stream().filter(Answer::getIsCorrect).count();
        if (correctCount > answers.size() * 0.8) {
            strengths.append("Excellent overall performance with high accuracy rate. ");
        }
        
        double avgTime = answers.stream()
                .filter(a -> a.getTimeTakenSeconds() != null)
                .mapToInt(Answer::getTimeTakenSeconds)
                .average()
                .orElse(0.0);
        
        if (avgTime > 0 && avgTime < 30) {
            strengths.append("Quick response time demonstrates good time management. ");
        }
        
        return strengths.length() > 0 ? strengths.toString() : "Areas of strength identified through consistent correct answers.";
    }
    
    private String generateImprovementAreas(List<Answer> answers) {
        StringBuilder improvements = new StringBuilder();
        
        long incorrectCount = answers.stream().filter(a -> !a.getIsCorrect()).count();
        if (incorrectCount > answers.size() * 0.3) {
            improvements.append("Focus on understanding fundamental concepts. ");
        }
        
        double avgTime = answers.stream()
                .filter(a -> a.getTimeTakenSeconds() != null)
                .mapToInt(Answer::getTimeTakenSeconds)
                .average()
                .orElse(0.0);
        
        if (avgTime > 60) {
            improvements.append("Practice time management to complete questions more efficiently. ");
        }
        
        return improvements.length() > 0 ? improvements.toString() : "Continue practicing to strengthen weak areas.";
    }
    
    private String generateDetailedAnalysis(Exam exam, List<Answer> answers) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("Exam Performance Analysis\n");
        analysis.append("Exam Code: ").append(exam.getExamCode()).append("\n");
        analysis.append("Topic: ").append(exam.getTopic().getName()).append("\n");
        analysis.append("Student: ").append(exam.getStudent().getFullName()).append("\n");
        analysis.append("Score: ").append(exam.getTotalScore()).append("/").append(exam.getMaxScore());
        analysis.append(" (").append(String.format("%.1f", exam.getPercentage())).append("%)\n");
        
        if (exam.getGrade() != null) {
            analysis.append("Grade: ").append(exam.getGrade()).append("\n");
        }
        
        analysis.append("\nDetailed Results:\n");
        analysis.append("Total Questions: ").append(answers.size()).append("\n");
        analysis.append("Correct Answers: ").append(answers.stream().filter(Answer::getIsCorrect).count()).append("\n");
        analysis.append("Wrong Answers: ").append(answers.stream().filter(a -> !a.getIsCorrect()).count()).append("\n");
        
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            long duration = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
            analysis.append("Duration: ").append(duration).append(" minutes\n");
        }
        
        analysis.append("\nRecommendations:\n");
        if (exam.getPercentage() >= 80) {
            analysis.append("Excellent performance! Keep up the good work.\n");
        } else if (exam.getPercentage() >= 60) {
            analysis.append("Good performance with room for improvement.\n");
        } else {
            analysis.append("Additional study and practice recommended.\n");
        }
        
        return analysis.toString();
    }
    
    private String prepareExamDataForAI(Exam exam, List<Answer> answers) {
        StringBuilder data = new StringBuilder();
        
        data.append("Student: ").append(exam.getStudent().getFullName()).append("\n");
        data.append("Topic: ").append(exam.getTopic().getName()).append("\n");
        data.append("Score: ").append(exam.getTotalScore()).append("/").append(exam.getMaxScore());
        data.append(" (").append(String.format("%.1f", exam.getPercentage())).append("%)\n");
        data.append("Grade: ").append(exam.getGrade() != null ? exam.getGrade() : "N/A").append("\n");
        
        data.append("\nPerformance Details:\n");
        data.append("Total Questions: ").append(answers.size()).append("\n");
        data.append("Correct Answers: ").append(answers.stream().filter(Answer::getIsCorrect).count()).append("\n");
        data.append("Wrong Answers: ").append(answers.stream().filter(a -> !a.getIsCorrect()).count()).append("\n");
        
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            long duration = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
            data.append("Duration: ").append(duration).append(" minutes\n");
        }
        
        data.append("\nQuestion-wise Performance:\n");
        for (Answer answer : answers) {
            data.append("Q: ").append(answer.getQuestion().getQuestionText()).append("\n");
            data.append("Student Answer: ").append(answer.getAnswerText()).append("\n");
            data.append("Correct: ").append(answer.getIsCorrect() ? "Yes" : "No").append("\n");
            data.append("---\n");
        }
        
        return data.toString();
    }
}
