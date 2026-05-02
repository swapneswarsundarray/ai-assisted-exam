/*
 * Copyright 2024 Aentic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aentic.exam.controller;

import com.aentic.exam.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generator")
public class QuestionGeneratorController {
    
    @Autowired
    private GeminiService geminiService;
    
    @GetMapping("/")
    public String generatorIndex() {
        return "generator/index";
    }
    
    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> generateQuestions(@RequestBody Map<String, Object> request) {
        try {
            // Log incoming request for debugging
            System.out.println("Received request: " + request);
            
            String userType = (String) request.get("userType");
            String difficulty = (String) request.get("difficulty");
            String topic = (String) request.get("topic");
            String questionCount = (String) request.get("questionCount");
            String questionType = (String) request.get("questionType");
            String apiKey = (String) request.get("apiKey");
            
            // Validate required fields
            if (userType == null || difficulty == null || topic == null || questionCount == null || questionType == null || apiKey == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Missing required fields. Please fill all required information including your Gemini API key.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate API key format (Gemini API keys are different)
            if (apiKey == null || apiKey.trim().isEmpty() || apiKey.length() < 10) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Invalid Gemini API key format. Please check your API key and try again.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if Gemini service is available
            if (geminiService == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Gemini service is not available. Please check your API key configuration.");
                return ResponseEntity.status(503).body(errorResponse);
            }
            
            // Build context for AI
            StringBuilder context = new StringBuilder();
            context.append("Generate ").append(questionCount).append(" ");
            context.append(difficulty).append(" difficulty ");
            context.append(questionType).append(" questions about ").append(topic).append(". ");
            
            if ("student".equals(userType)) {
                String grade = (String) request.get("grade");
                context.append("These questions are for a ").append(grade).append(" grade student. ");
                context.append("Make the questions age-appropriate and educational. ");
            } else {
                String profession = (String) request.get("profession");
                String audience = (String) request.get("audience");
                context.append("These questions are for a ").append(profession).append(". ");
                if (audience != null && !audience.trim().isEmpty()) {
                    context.append("The target audience is: ").append(audience).append(". ");
                }
                context.append("Make the questions professional and relevant. ");
            }
            
            // Add specific instructions based on difficulty
            switch (difficulty.toLowerCase()) {
                case "easy":
                    context.append("Keep questions simple, straightforward, and easy to understand. ");
                    break;
                case "medium":
                    context.append("Make questions moderately challenging with some complexity. ");
                    break;
                case "hard":
                    context.append("Create complex, challenging questions that require deep thinking. ");
                    break;
            }
            
            // Add format instructions
            context.append("Format each question as follows:\n");
            if ("multiple_choice".equals(questionType)) {
                context.append("QUESTION: [question text]\n");
                context.append("OPTIONS: A: [option A], B: [option B], C: [option C], D: [option D]\n");
                context.append("ANSWER: [correct option letter]\n");
                context.append("EXPLANATION: [brief explanation]\n");
                context.append("---\n");
            } else if ("true_false".equals(questionType)) {
                context.append("QUESTION: [statement]\n");
                context.append("ANSWER: [True/False]\n");
                context.append("EXPLANATION: [brief explanation]\n");
                context.append("---\n");
            } else if ("short_answer".equals(questionType)) {
                context.append("QUESTION: [question text]\n");
                context.append("ANSWER: [expected answer]\n");
                context.append("EXPLANATION: [brief explanation]\n");
                context.append("---\n");
            } else {
                context.append("Mix of multiple choice, true/false, and short answer questions.\n");
                context.append("For multiple choice: QUESTION: [text], OPTIONS: A: [A], B: [B], C: [C], D: [D], ANSWER: [letter], EXPLANATION: [text]\n");
                context.append("For true/false: QUESTION: [statement], ANSWER: [True/False], EXPLANATION: [text]\n");
                context.append("For short answer: QUESTION: [text], ANSWER: [answer], EXPLANATION: [text]\n");
                context.append("---\n");
            }
            
            // Generate questions using Gemini with user-provided API key
            System.out.println("Calling Gemini with context: " + context);
            String aiResponse = geminiService.generateUniversalQuestions(context.toString(), apiKey);
            System.out.println("Gemini response received: " + aiResponse);
            
            // Parse the response
            List<Map<String, String>> questions = parseQuestions(aiResponse, questionType);
            System.out.println("Parsed " + questions.size() + " questions");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions);
            response.put("message", "Questions generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error generating questions: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate questions: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testOpenAI(@RequestBody Map<String, Object> request) {
        try {
            String apiKey = (String) request.get("apiKey");
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Invalid API key format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            System.out.println("Testing Gemini API with provided key...");
            
            // Simple test prompt
            String testPrompt = "Generate one simple multiple choice question about basic math. Format: QUESTION: [question], OPTIONS: A: [option], B: [option], C: [option], D: [option], ANSWER: [letter], EXPLANATION: [explanation]";
            
            String aiResponse = geminiService.generateUniversalQuestions(testPrompt, apiKey);
            System.out.println("Test response: " + aiResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gemini API test successful");
            response.put("response", aiResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Gemini API Test Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Gemini API test failed: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPDF(@RequestParam String questions) {
        try {
            // Parse questions from parameter
            // Generate PDF
            // Return PDF bytes
            byte[] pdfBytes = generatePDF(questions);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"questions.pdf\"")
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    private List<Map<String, String>> parseQuestions(String aiResponse, String questionType) {
        List<Map<String, String>> questions = new ArrayList<>();
        
        String[] questionBlocks = aiResponse.split("---");
        
        for (String block : questionBlocks) {
            if (block.trim().isEmpty()) continue;
            
            Map<String, String> question = new HashMap<>();
            
            String[] lines = block.trim().split("\n");
            
            for (String line : lines) {
                if (line.startsWith("QUESTION:")) {
                    question.put("questionText", line.substring(9).trim());
                } else if (line.startsWith("OPTIONS:")) {
                    question.put("options", line.substring(8).trim());
                } else if (line.startsWith("ANSWER:")) {
                    question.put("correctAnswer", line.substring(7).trim());
                } else if (line.startsWith("EXPLANATION:")) {
                    question.put("explanation", line.substring(11).trim());
                }
            }
            
            if (question.containsKey("questionText")) {
                questions.add(question);
            }
        }
        
        return questions;
    }
    
    private byte[] generatePDF(String questionsJson) {
        // Implement PDF generation
        // For now, return empty bytes
        return new byte[0];
    }
}
