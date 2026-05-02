package com.aentic.exam.service;

import com.aentic.exam.entity.Exam;
import com.aentic.exam.entity.Question;
import com.aentic.exam.entity.Student;
import com.aentic.exam.entity.Topic;
import com.aentic.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private GeminiService geminiService;
    
    public Question createQuestion(Question question, Long topicId) {
        Optional<Topic> topic = topicService.getTopicById(topicId);
        if (topic.isEmpty()) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        
        question.setTopic(topic.get());
        return questionRepository.save(question);
    }
    
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }
    
    public List<Question> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicId(topicId);
    }
    
    public List<Question> getQuestionsByTopicAndDifficulty(Long topicId, String difficultyLevel) {
        return questionRepository.findByTopicIdAndDifficultyLevel(topicId, difficultyLevel);
    }
    
    public List<Question> getRandomQuestionsByTopic(Long topicId, int limit) {
        return questionRepository.findLimitedRandomQuestionsByTopic(topicId, limit);
    }
    
    public Question updateQuestion(Long id, Question questionDetails) {
        Optional<Question> existingQuestion = questionRepository.findById(id);
        if (existingQuestion.isEmpty()) {
            throw new RuntimeException("Question not found with id: " + id);
        }
        
        Question question = existingQuestion.get();
        question.setQuestionText(questionDetails.getQuestionText());
        question.setQuestionType(questionDetails.getQuestionType());
        question.setOptions(questionDetails.getOptions());
        question.setCorrectAnswer(questionDetails.getCorrectAnswer());
        question.setExplanation(questionDetails.getExplanation());
        question.setDifficultyLevel(questionDetails.getDifficultyLevel());
        question.setPoints(questionDetails.getPoints());
        
        return questionRepository.save(question);
    }
    
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }
    
    public List<Question> generateQuestionsForTopic(Long topicId, int count, String difficultyLevel) {
        List<Question> existingQuestions = getQuestionsByTopicAndDifficulty(topicId, difficultyLevel);
        
        if (existingQuestions.size() >= count) {
            return questionRepository.findLimitedRandomQuestionsByTopic(topicId, count);
        } else {
            return generateAIQuestions(topicId, count - existingQuestions.size(), difficultyLevel);
        }
    }
    
    private List<Question> generateAIQuestions(Long topicId, int count, String difficultyLevel) {
        Optional<Topic> topic = topicService.getTopicById(topicId);
        if (topic.isEmpty()) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        
        List<Question> aiQuestions = new java.util.ArrayList<>();
        
        try {
            for (int i = 0; i < count; i++) {
                // Get a student from the first exam for this topic to personalize questions
                Student student = null;
                List<Exam> exams = topic.get().getExams();
                if (exams != null && !exams.isEmpty()) {
                    student = exams.get(0).getStudent();
                }
                
                String aiGeneratedContent = geminiService.generateQuestion(topic.get().getName(), difficultyLevel, student);
                Question question = parseAIGeneratedQuestion(aiGeneratedContent, topic.get(), difficultyLevel);
                
                aiQuestions.add(questionRepository.save(question));
            }
        } catch (Exception e) {
            // Fallback to simple question generation if OpenAI fails
            for (int i = 0; i < count; i++) {
                Question question = new Question();
                question.setQuestionText(generateQuestionText(topic.get().getName(), difficultyLevel));
                question.setQuestionType("MULTIPLE_CHOICE");
                question.setOptions(generateOptions());
                question.setCorrectAnswer("A");
                question.setExplanation("Generated explanation for " + topic.get().getName());
                question.setDifficultyLevel(difficultyLevel);
                question.setPoints(1);
                question.setTopic(topic.get());
                
                aiQuestions.add(questionRepository.save(question));
            }
        }
        
        return aiQuestions;
    }
    
    private String generateQuestionText(String topicName, String difficultyLevel) {
        return "What is the correct answer for this " + difficultyLevel.toLowerCase() + " question about " + topicName + "?";
    }
    
    private String generateOptions() {
        return "A: Option A,B: Option B,C: Option C,D: Option D";
    }
    
    private Question parseAIGeneratedQuestion(String aiContent, Topic topic, String difficultyLevel) {
        Question question = new Question();
        question.setTopic(topic);
        question.setDifficultyLevel(difficultyLevel);
        question.setQuestionType("MULTIPLE_CHOICE");
        question.setPoints(1);
        
        String[] lines = aiContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("QUESTION:")) {
                question.setQuestionText(line.substring("QUESTION:".length()).trim());
            } else if (line.startsWith("OPTIONS:")) {
                question.setOptions(line.substring("OPTIONS:".length()).trim());
            } else if (line.startsWith("ANSWER:")) {
                question.setCorrectAnswer(line.substring("ANSWER:".length()).trim());
            } else if (line.startsWith("EXPLANATION:")) {
                question.setExplanation(line.substring("EXPLANATION:".length()).trim());
            }
        }
        
        // Fallback if parsing fails
        if (question.getQuestionText() == null || question.getQuestionText().isEmpty()) {
            question.setQuestionText(generateQuestionText(topic.getName(), difficultyLevel));
            question.setOptions(generateOptions());
            question.setCorrectAnswer("A");
            question.setExplanation("Generated explanation for " + topic.getName());
        }
        
        return question;
    }
}
