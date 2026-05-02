package com.aentic.exam.controller;

import com.aentic.exam.dto.StudentRegistrationDto;
import com.aentic.exam.entity.Student;
import com.aentic.exam.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/students")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new StudentRegistrationDto());
        return "student/register";
    }
    
    @PostMapping("/register")
    public String registerStudent(@Valid @ModelAttribute("student") StudentRegistrationDto registrationDto,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "student/register";
        }
        
        try {
            Student student = studentService.registerStudent(registrationDto);
            return "redirect:/students/success?studentId=" + student.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "student/register";
        }
    }
    
    @GetMapping("/success")
    public String registrationSuccess(@RequestParam Long studentId, Model model) {
        Optional<Student> student = studentService.getStudentById(studentId);
        if (student.isPresent()) {
            model.addAttribute("student", student.get());
            return "student/success";
        }
        return "redirect:/students/register";
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/email/{email}")
    @ResponseBody
    public ResponseEntity<Student> getStudentByEmail(@PathVariable String email) {
        Optional<Student> student = studentService.getStudentByEmail(email);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Student> updateStudent(@PathVariable Long id,
                                               @Valid @RequestBody StudentRegistrationDto registrationDto) {
        try {
            Student student = studentService.updateStudent(id, registrationDto);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
