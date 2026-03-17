package com.example.SmartLearning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {
 
 @Autowired
 private CourseService courseService;
 
 @GetMapping
 public ResponseEntity<List<CourseDTO>> getAllCourses() {
 List<CourseDTO> courses = courseService.getAllCoursesForAdmin();
 return ResponseEntity.ok(courses);
 }
 
 @GetMapping("/{id}")
 public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
 CourseDTO course = courseService.getCourseById(id);
 return ResponseEntity.ok(course);
 }
 
 @PatchMapping("/{id}/activate")
 public ResponseEntity<CourseDTO> activateCourse(@PathVariable Long id) {
 CourseDTO activatedCourse = courseService.activateCourse(id);
 return ResponseEntity.ok(activatedCourse);
 }
 
 @PatchMapping("/{id}/deactivate")
 public ResponseEntity<CourseDTO> deactivateCourse(@PathVariable Long id) {
 CourseDTO deactivatedCourse = courseService.deactivateCourse(id);
 return ResponseEntity.ok(deactivatedCourse);
 }
 
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
 courseService.adminDeleteCourse(id);
 return ResponseEntity.noContent().build();
 }
 
 @PutMapping("/{id}")
 public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id,@Valid @RequestBody CourseDTO courseDTO) {
 CourseDTO updatedCourse = courseService.adminUpdateCourse(id, courseDTO);
 return ResponseEntity.ok(updatedCourse);
 }
}
