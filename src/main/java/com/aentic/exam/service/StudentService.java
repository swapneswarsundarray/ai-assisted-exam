package com.aentic.exam.service;

import com.aentic.exam.dto.StudentRegistrationDto;
import com.aentic.exam.entity.Student;
import com.aentic.exam.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    public Student registerStudent(StudentRegistrationDto registrationDto) {
        if (studentRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Student with email " + registrationDto.getEmail() + " already exists");
        }
        
        Student student = new Student();
        student.setFirstName(registrationDto.getFirstName());
        student.setLastName(registrationDto.getLastName());
        student.setEmail(registrationDto.getEmail());
        student.setPhone(registrationDto.getPhone());
        student.setCountry(registrationDto.getCountry());
        student.setEducationLevel(registrationDto.getEducationLevel());
        student.setPreferredLanguage(registrationDto.getPreferredLanguage());
        student.setInterests(registrationDto.getInterests());
        student.setProfession(registrationDto.getProfession());
        student.setAge(registrationDto.getAge());
        student.setExperienceYears(registrationDto.getExperienceYears());
        
        return studentRepository.save(student);
    }
    
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }
    
    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }
    
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    public Student updateStudent(Long id, StudentRegistrationDto registrationDto) {
        Optional<Student> existingStudent = studentRepository.findById(id);
        if (existingStudent.isEmpty()) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        
        Student student = existingStudent.get();
        
        if (!student.getEmail().equals(registrationDto.getEmail()) && 
            studentRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already in use: " + registrationDto.getEmail());
        }
        
        student.setFirstName(registrationDto.getFirstName());
        student.setLastName(registrationDto.getLastName());
        student.setEmail(registrationDto.getEmail());
        student.setPhone(registrationDto.getPhone());
        student.setCountry(registrationDto.getCountry());
        student.setEducationLevel(registrationDto.getEducationLevel());
        student.setPreferredLanguage(registrationDto.getPreferredLanguage());
        student.setInterests(registrationDto.getInterests());
        student.setProfession(registrationDto.getProfession());
        student.setAge(registrationDto.getAge());
        student.setExperienceYears(registrationDto.getExperienceYears());
        
        return studentRepository.save(student);
    }
    
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
}
