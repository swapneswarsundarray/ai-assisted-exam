package com.aentic.exam.repository;

import com.aentic.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByExamCode(String examCode);
    List<Exam> findByStudentId(Long studentId);
    List<Exam> findByTopicId(Long topicId);
    List<Exam> findByStatus(String status);
    List<Exam> findByStudentIdAndStatus(Long studentId, String status);
}
