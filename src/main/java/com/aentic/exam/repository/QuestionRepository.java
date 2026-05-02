package com.aentic.exam.repository;

import com.aentic.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicId(Long topicId);
    List<Question> findByTopicIdAndDifficultyLevel(Long topicId, String difficultyLevel);
    
    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId ORDER BY RANDOM()")
    List<Question> findRandomQuestionsByTopic(@Param("topicId") Long topicId);
    
    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId AND q.difficultyLevel = :level ORDER BY RANDOM()")
    List<Question> findRandomQuestionsByTopicAndDifficulty(@Param("topicId") Long topicId, @Param("level") String level);
    
    @Query(value = "SELECT * FROM questions WHERE topic_id = :topicId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findLimitedRandomQuestionsByTopic(@Param("topicId") Long topicId, @Param("limit") int limit);
}
