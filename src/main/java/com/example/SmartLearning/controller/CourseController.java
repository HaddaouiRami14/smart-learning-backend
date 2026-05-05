package com.example.SmartLearning.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.service.CourseService;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

@Autowired
private CourseService courseService;

@Autowired
private UserRepository userRepository;


 @GetMapping
 public ResponseEntity<List<CourseDTO>> getAllCourses() {
 List<CourseDTO> courses = courseService.getAllCourses();
 return ResponseEntity.ok(courses);
 }

@GetMapping("/count-learners")
public long countLearners() {
    return userRepository.countByRole(Role.APPRENANT);
}
  
}
