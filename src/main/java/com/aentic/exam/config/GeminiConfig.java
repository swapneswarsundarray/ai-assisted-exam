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

package com.aentic.exam.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    
    @Value("${gemini.project.id:your-project-id}")
    private String projectId;
    
    @Value("${gemini.location:us-central1}")
    private String location;
    
    @Value("${gemini.model:gemini-1.5-flash}")
    private String modelName;
    
    @Bean
    public VertexAI vertexAI() {
        return new VertexAI(projectId, location);
    }
    
    @Bean
    public String geminiModelName() {
        return modelName;
    }
}
