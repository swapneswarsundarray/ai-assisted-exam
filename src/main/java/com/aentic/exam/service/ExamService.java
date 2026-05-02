package com.aentic.exam.service;

import com.aentic.exam.dto.ExamSubmissionDto;
import com.aentic.exam.entity.*;
import com.aentic.exam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExamService {
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AnswerRepository answerRepository;
    
    public Exam startExam(Long studentId, Long topicId) {
        Optional<Student> student = studentService.getStudentById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("Student not found with id: " + studentId);
        }
        
        Optional<Topic> topic = topicService.getTopicById(topicId);
        if (topic.isEmpty()) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        
        Exam exam = new Exam();
        exam.setStudent(student.get());
        exam.setTopic(topic.get());
        exam.setDurationMinutes(topic.get().getDurationMinutes());
        exam.setStartTime(LocalDateTime.now());
        exam.setStatus("IN_PROGRESS");
        exam.setMaxScore(calculateMaxScore(topicId));
        
        return examRepository.save(exam);
    }
    
    private Integer calculateMaxScore(Long topicId) {
        List<Question> questions = questionService.getQuestionsByTopic(topicId);
        return questions.stream().mapToInt(Question::getPoints).sum();
    }
    
    public Optional<Exam> getExamById(Long id) {
        return examRepository.findById(id);
    }
    
    public Optional<Exam> getExamByCode(String examCode) {
        return examRepository.findByExamCode(examCode);
    }
    
    public List<Exam> getExamsByStudent(Long studentId) {
        return examRepository.findByStudentId(studentId);
    }
    
    @Transactional
    public Exam submitExam(ExamSubmissionDto submissionDto) {
        Optional<Exam> examOpt = examRepository.findByExamCode(submissionDto.getExamCode());
        if (examOpt.isEmpty()) {
            throw new RuntimeException("Exam not found with code: " + submissionDto.getExamCode());
        }
        
        Exam exam = examOpt.get();
        if (!"IN_PROGRESS".equals(exam.getStatus())) {
            throw new RuntimeException("Exam is not in progress");
        }
        
        exam.setEndTime(LocalDateTime.now());
        exam.setStatus("COMPLETED");
        
        saveAnswers(exam, submissionDto.getAnswers());
        calculateExamResults(exam);
        
        return examRepository.save(exam);
    }
    
    private void saveAnswers(Exam exam, Map<Long, String> answers) {
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Optional<Question> question = questionService.getQuestionById(entry.getKey());
            if (question.isPresent()) {
                Answer answer = new Answer();
                answer.setExam(exam);
                answer.setQuestion(question.get());
                answer.setAnswerText(entry.getValue());
                answer.setIsCorrect(entry.getValue().equals(question.get().getCorrectAnswer()));
                
                answerRepository.save(answer);
            }
        }
    }
    
    private void calculateExamResults(Exam exam) {
        List<Answer> answers = answerRepository.findByExamId(exam.getId());
        
        int totalScore = 0;
        for (Answer answer : answers) {
            if (answer.getIsCorrect()) {
                totalScore += answer.getQuestion().getPoints();
            }
        }
        
        exam.setTotalScore(totalScore);
        exam.calculateResults();
    }
    
    public List<Question> getExamQuestions(Long examId) {
        Optional<Exam> exam = getExamById(examId);
        if (exam.isEmpty()) {
            throw new RuntimeException("Exam not found with id: " + examId);
        }
        
        return questionService.getRandomQuestionsByTopic(exam.get().getTopic().getId(), 
                exam.get().getTopic().getQuestionCount());
    }
    
    public void updateExamStatus(Long examId, String status) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            throw new RuntimeException("Exam not found with id: " + examId);
        }
        
        Exam exam = examOpt.get();
        exam.setStatus(status);
        
        if ("COMPLETED".equals(status) && exam.getEndTime() == null) {
            exam.setEndTime(LocalDateTime.now());
        }
        
        examRepository.save(exam);
    }
}
