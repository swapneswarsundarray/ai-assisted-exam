package com.aentic.exam.controller;

import com.aentic.exam.entity.Topic;
import com.aentic.exam.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/topics")
public class TopicController {
    
    @Autowired
    private TopicService topicService;
    
    @GetMapping
    public String listTopics(Model model) {
        List<Topic> topics = topicService.getAllTopics();
        model.addAttribute("topics", topics);
        return "topic/list";
    }
    
    @GetMapping("/select")
    public String selectTopic(Model model) {
        List<Topic> topics = topicService.getAllTopics();
        model.addAttribute("topics", topics);
        return "topic/select";
    }
    
    @GetMapping("/{id}")
    public String viewTopic(@PathVariable Long id, Model model) {
        Optional<Topic> topic = topicService.getTopicById(id);
        if (topic.isPresent()) {
            model.addAttribute("topic", topic.get());
            return "topic/detail";
        }
        return "redirect:/topics";
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Topic> getTopic(@PathVariable Long id) {
        Optional<Topic> topic = topicService.getTopicById(id);
        return topic.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<Topic>> getAllTopics() {
        List<Topic> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }
    
    @GetMapping("/api/difficulty/{level}")
    @ResponseBody
    public ResponseEntity<List<Topic>> getTopicsByDifficulty(@PathVariable String level) {
        List<Topic> topics = topicService.getTopicsByDifficulty(level);
        return ResponseEntity.ok(topics);
    }
    
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Topic> createTopic(@RequestBody Topic topic) {
        try {
            Topic createdTopic = topicService.createTopic(topic);
            return ResponseEntity.ok(createdTopic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Topic> updateTopic(@PathVariable Long id, @RequestBody Topic topicDetails) {
        try {
            Topic topic = topicService.updateTopic(id, topicDetails);
            return ResponseEntity.ok(topic);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        try {
            topicService.deleteTopic(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
