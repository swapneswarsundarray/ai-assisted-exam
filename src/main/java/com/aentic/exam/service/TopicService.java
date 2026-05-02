package com.aentic.exam.service;

import com.aentic.exam.entity.Topic;
import com.aentic.exam.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    
    @Autowired
    private TopicRepository topicRepository;
    
    public Topic createTopic(Topic topic) {
        if (topicRepository.existsByName(topic.getName())) {
            throw new RuntimeException("Topic with name " + topic.getName() + " already exists");
        }
        return topicRepository.save(topic);
    }
    
    public Optional<Topic> getTopicById(Long id) {
        return topicRepository.findById(id);
    }
    
    public Optional<Topic> getTopicByName(String name) {
        return topicRepository.findByName(name);
    }
    
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }
    
    public List<Topic> getTopicsByDifficulty(String difficultyLevel) {
        return topicRepository.findByDifficultyLevel(difficultyLevel);
    }
    
    public Topic updateTopic(Long id, Topic topicDetails) {
        Optional<Topic> existingTopic = topicRepository.findById(id);
        if (existingTopic.isEmpty()) {
            throw new RuntimeException("Topic not found with id: " + id);
        }
        
        Topic topic = existingTopic.get();
        
        if (!topic.getName().equals(topicDetails.getName()) && 
            topicRepository.existsByName(topicDetails.getName())) {
            throw new RuntimeException("Topic name already in use: " + topicDetails.getName());
        }
        
        topic.setName(topicDetails.getName());
        topic.setDescription(topicDetails.getDescription());
        topic.setDifficultyLevel(topicDetails.getDifficultyLevel());
        topic.setDurationMinutes(topicDetails.getDurationMinutes());
        topic.setQuestionCount(topicDetails.getQuestionCount());
        
        return topicRepository.save(topic);
    }
    
    public void deleteTopic(Long id) {
        if (!topicRepository.existsById(id)) {
            throw new RuntimeException("Topic not found with id: " + id);
        }
        topicRepository.deleteById(id);
    }
}
