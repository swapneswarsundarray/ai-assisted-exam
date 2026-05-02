package com.aentic.exam.repository;

import com.aentic.exam.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByExamId(Long examId);
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByExamIdAndIsCorrect(Long examId, Boolean isCorrect);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.exam.id = :examId AND a.isCorrect = true")
    Long countCorrectAnswersByExamId(@Param("examId") Long examId);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.exam.id = :examId")
    Long countTotalAnswersByExamId(@Param("examId") Long examId);
    
    @Query("SELECT AVG(a.timeTakenSeconds) FROM Answer a WHERE a.exam.id = :examId")
    Double getAverageTimeByExamId(@Param("examId") Long examId);
}
