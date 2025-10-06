package com.example.evm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator; // Cần thiết cho Feature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        
        //Đăng ký Module cho Java 8 Time (Giải quyết lỗi LocalDateTime trước đó, phòng trường hợp chưa fix)
        mapper.registerModule(new JavaTimeModule()); 
        
        return mapper;
    }
}