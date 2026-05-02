package com.aentic.exam.controller;

import com.aentic.exam.dto.ExamSubmissionDto;
import com.aentic.exam.entity.Exam;
import com.aentic.exam.entity.Question;
import com.aentic.exam.service.ExamService;
import com.aentic.exam.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/exams")
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping("/start")
    public String showStartExamForm(@RequestParam Long studentId, @RequestParam Long topicId, Model model) {
        model.addAttribute("studentId", studentId);
        model.addAttribute("topicId", topicId);
        return "exam/start";
    }
    
    @PostMapping("/start")
    public String startExam(@RequestParam Long studentId, @RequestParam Long topicId, Model model) {
        try {
            Exam exam = examService.startExam(studentId, topicId);
            return "redirect:/exams/take/" + exam.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "exam/start";
        }
    }
    
    @GetMapping("/take/{examId}")
    public String takeExam(@PathVariable Long examId, Model model) {
        Optional<Exam> exam = examService.getExamById(examId);
        if (exam.isEmpty() || !"IN_PROGRESS".equals(exam.get().getStatus())) {
            return "redirect:/topics/select";
        }
        
        List<Question> questions = examService.getExamQuestions(examId);
        model.addAttribute("exam", exam.get());
        model.addAttribute("questions", questions);
        model.addAttribute("duration", exam.get().getDurationMinutes());
        
        return "exam/take";
    }
    
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<Exam> submitExam(@Valid @RequestBody ExamSubmissionDto submissionDto) {
        try {
            Exam exam = examService.submitExam(submissionDto);
            return ResponseEntity.ok(exam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/results/{examId}")
    public String showResults(@PathVariable Long examId, Model model) {
        Optional<Exam> exam = examService.getExamById(examId);
        if (exam.isEmpty()) {
            return "redirect:/topics/select";
        }
        
        model.addAttribute("exam", exam.get());
        return "exam/results";
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Exam> getExam(@PathVariable Long id) {
        Optional<Exam> exam = examService.getExamById(id);
        return exam.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/code/{examCode}")
    @ResponseBody
    public ResponseEntity<Exam> getExamByCode(@PathVariable String examCode) {
        Optional<Exam> exam = examService.getExamByCode(examCode);
        return exam.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/student/{studentId}")
    @ResponseBody
    public ResponseEntity<List<Exam>> getExamsByStudent(@PathVariable Long studentId) {
        List<Exam> exams = examService.getExamsByStudent(studentId);
        return ResponseEntity.ok(exams);
    }
    
    @GetMapping("/api/{examId}/questions")
    @ResponseBody
    public ResponseEntity<List<Question>> getExamQuestions(@PathVariable Long examId) {
        try {
            List<Question> questions = examService.getExamQuestions(examId);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/api/{examId}/status")
    @ResponseBody
    public ResponseEntity<Void> updateExamStatus(@PathVariable Long examId, @RequestBody Map<String, String> status) {
        try {
            examService.updateExamStatus(examId, status.get("status"));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
