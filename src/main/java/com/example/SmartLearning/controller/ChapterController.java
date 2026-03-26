package com.example.SmartLearning.controller;

import com.example.SmartLearning.DTO.ChapterResponse;
import com.example.SmartLearning.DTO.ChapterResourceResponse;
import com.example.SmartLearning.DTO.CreateChapterRequest;
import com.example.SmartLearning.DTO.UpdateChapterRequest;
import com.example.SmartLearning.model.ChapterResource;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/formateur/courses/{courseId}/chapters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('FORMATEUR')")
@Slf4j
public class ChapterController {
    
    private final ChapterService chapterService;
    
    
    
    @GetMapping
    public ResponseEntity<List<ChapterResponse>> getChapters(@PathVariable Long courseId) {
        List<ChapterResponse> chapters = chapterService.getChaptersByCourse(courseId);
        return ResponseEntity.ok(chapters);
    }
    
    
    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> getChapter(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        ChapterResponse chapter = chapterService.getChapter(courseId, chapterId);
        return ResponseEntity.ok(chapter);
    }
    
    
    @PostMapping
    public ResponseEntity<ChapterResponse> createChapter(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateChapterRequest request,
            Authentication authentication
    ) {
        ChapterResponse chapter = chapterService.createChapter(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(chapter);
    }
    
    
    @PutMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @Valid @RequestBody UpdateChapterRequest request
    ) {
        ChapterResponse updated = chapterService.updateChapter(courseId, chapterId, request);
        return ResponseEntity.ok(updated);
    }
    
  
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<Void> deleteChapter(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        chapterService.deleteChapter(courseId, chapterId);
        return ResponseEntity.noContent().build();
    }
    
    
    
    @GetMapping("/{chapterId}/resources")
    public ResponseEntity<List<ChapterResourceResponse>> getResources(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        List<ChapterResourceResponse> resources = chapterService.getChapterResources(courseId, chapterId);
        return ResponseEntity.ok(resources);
    }
    
    
    @PostMapping("/{chapterId}/resources")
    public ResponseEntity<?> uploadResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le fichier est vide"));
            }
            
            log.info("Upload request received - File: {}, Size: {} bytes, CourseId: {}, ChapterId: {}", 
                file.getOriginalFilename(), file.getSize(), courseId, chapterId);
            
            JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
            
            ChapterResourceResponse resource = chapterService.uploadResource(
                    courseId, chapterId, file, principal.getId()
            );
            
            log.info("File uploaded successfully - ResourceId: {}", resource.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(resource);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error during upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (IOException e) {
            log.error("IO error during upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur de lecture du fichier: " + e.getMessage()));
                
        } catch (RuntimeException e) {
            log.error("Runtime error during upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            log.error("Unexpected error during upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur serveur: " + e.getMessage()));
        }
    }
    
    
    @GetMapping("/{chapterId}/resources/{resourceId}/download")
    public ResponseEntity<Resource> downloadResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long resourceId
    ) {
        ChapterResource resource = chapterService.downloadResource(courseId, chapterId, resourceId);
        
        ByteArrayResource byteResource = new ByteArrayResource(resource.getFileData());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resource.getMimeType()))
                .contentLength(resource.getFileSize())
                .body(byteResource);
    }
    
    
    
    @DeleteMapping("/{chapterId}/resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long resourceId
    ) {
        chapterService.deleteResource(courseId, chapterId, resourceId);
        return ResponseEntity.noContent().build();
    }
    
    
    
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}