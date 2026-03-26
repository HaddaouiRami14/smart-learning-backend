package com.example.SmartLearning.DTO;

import java.time.LocalDateTime;

import com.example.SmartLearning.Enum.ActivityType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityDTO {
    private Long        id;
    private ActivityType type;
    private String      title;
    private String      description;
    private String      timeAgo;      
    private LocalDateTime createdAt;
}
