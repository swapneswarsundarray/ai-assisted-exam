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

package com.aentic.exam.service;

import com.aentic.exam.entity.Student;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {
    
    @Autowired
    private VertexAI vertexAI;
    
    @Value("${gemini.model:gemini-1.5-flash}")
    private String modelName;
    
    public String generateQuestion(String topicName, String difficultyLevel, Student student) {
        String prompt = buildQuestionPrompt(topicName, difficultyLevel, student);
        return generateContent(prompt);
    }
    
    public String generateReport(String examData) {
        String prompt = buildReportPrompt(examData);
        return generateContent(prompt);
    }
    
    public String generateUniversalQuestions(String prompt, String apiKey) {
        try {
            // For Gemini API, we need to use the API key differently
            // We'll use the default VertexAI instance and pass the API key in the model
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            
            String enhancedPrompt = "You are an expert educational content creator. Generate high-quality questions for various educational levels and professional contexts.\n\n" + prompt;
            
            System.out.println("Sending request to Gemini API...");
            GenerateContentResponse response = model.generateContent(enhancedPrompt);
            System.out.println("Received response from Gemini API");
            
            if (response == null || response.getCandidatesCount() == 0 || 
                response.getCandidates(0).getContent() == null || 
                response.getCandidates(0).getContent().getPartsCount() == 0) {
                throw new RuntimeException("Empty response from Gemini API");
            }
            
            return response.getCandidates(0).getContent().getParts(0).getText();
            
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            e.printStackTrace();
            
            // Check for specific Gemini errors
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("quota")) {
                throw new RuntimeException("QUOTA_EXCEEDED: Your Gemini API quota has been exceeded. Please check your billing details at https://console.cloud.google.com/billing", e);
            } else if (errorMessage != null && errorMessage.contains("permission")) {
                throw new RuntimeException("PERMISSION_DENIED: Your Gemini API key is invalid or lacks permissions. Please check your API key at https://console.cloud.google.com/apis/credentials", e);
            } else if (errorMessage != null && errorMessage.contains("billing")) {
                throw new RuntimeException("BILLING_REQUIRED: You need to enable billing for Google Cloud. Please check your billing details at https://console.cloud.google.com/billing", e);
            }
            
            throw new RuntimeException("Failed to generate questions with Gemini: " + e.getMessage(), e);
        }
    }
    
    private String generateContent(String prompt) {
        try {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            
            if (response == null || response.getCandidatesCount() == 0 || 
                response.getCandidates(0).getContent() == null || 
                response.getCandidates(0).getContent().getPartsCount() == 0) {
                throw new RuntimeException("Empty response from Gemini API");
            }
            
            return response.getCandidates(0).getContent().getParts(0).getText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate content with Gemini: " + e.getMessage(), e);
        }
    }
    
    private String buildQuestionPrompt(String topicName, String difficultyLevel, Student student) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a multiple choice question about ");
        prompt.append(topicName);
        prompt.append(" with ").append(difficultyLevel).append(" difficulty level.");
        
        if (student != null) {
            prompt.append(" The question should be appropriate for a student with this profile: ");
            prompt.append(buildStudentProfile(student));
        }
        
        prompt.append("\n\nFormat the response as:\n");
        prompt.append("QUESTION: [question text]\n");
        prompt.append("OPTIONS: A: [option A], B: [option B], C: [option C], D: [option D]\n");
        prompt.append("ANSWER: [correct option letter]\n");
        prompt.append("EXPLANATION: [brief explanation]");
        
        return prompt.toString();
    }
    
    private String buildReportPrompt(String examData) {
        return "Analyze the following exam results and provide a detailed report:\n\n" + examData + "\n\n" +
               "Format the response as:\n" +
               "STRENGTH_AREAS: [list of strengths]\n" +
               "IMPROVEMENT_AREAS: [list of improvement areas]\n" +
               "RECOMMENDATIONS: [specific study recommendations]\n" +
               "PERFORMANCE_SUMMARY: [overall performance summary]";
    }
    
    private String buildStudentProfile(Student student) {
        if (student == null) {
            return "No student profile available";
        }
        
        StringBuilder profile = new StringBuilder();
        profile.append("Age: ").append(student.getAge() != null ? student.getAge() : "Not specified").append(", ");
        profile.append("Country: ").append(student.getCountry() != null ? student.getCountry() : "Not specified").append(", ");
        profile.append("Education Level: ").append(student.getEducationLevel() != null ? student.getEducationLevel() : "Not specified").append(", ");
        profile.append("Profession: ").append(student.getProfession() != null ? student.getProfession() : "Not specified").append(", ");
        profile.append("Experience: ").append(student.getExperienceYears() != null ? student.getExperienceYears() + " years" : "Not specified").append(", ");
        profile.append("Interests: ").append(student.getInterests() != null ? student.getInterests() : "Not specified").append(", ");
        profile.append("Preferred Language: ").append(student.getPreferredLanguage() != null ? student.getPreferredLanguage() : "English");
        
        return profile.toString();
    }
}
