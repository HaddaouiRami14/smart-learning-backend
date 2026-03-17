package com.example.SmartLearning.service;

import java.io.IOException;
import java.util.List;

import java.util.stream.Collectors;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.SmartLearning.DTO.ChapterResourceResponse;
import com.example.SmartLearning.DTO.ChapterResponse;
import com.example.SmartLearning.DTO.CreateChapterRequest;
import com.example.SmartLearning.DTO.UpdateChapterRequest;
import com.example.SmartLearning.Repository.ChapterRepository;
import com.example.SmartLearning.Repository.ChapterResourceRepository;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.ChapterResource;
import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.Enum.ResourceType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final ChapterResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    
    
    // Configuration des limites
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MEMORY_THRESHOLD = 100 * 1024 * 1024; // 100MB pour la mémoire
    
    
    
   
    public ChapterResponse createChapter(Long courseId, CreateChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        
        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrder = chapterRepository.findMaxOrderIndexByCourseId(courseId)
                    .orElse(0);
            orderIndex = maxOrder + 1;
        }
        
        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());
        chapter.setOrderIndex(orderIndex);
        chapter.setCourse(course);
        
        Chapter savedChapter = chapterRepository.save(chapter);
        return mapToResponse(savedChapter);
    }
    
    
    public List<ChapterResponse> getChaptersByCourse(Long courseId) {
        
        courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        return chapterRepository.findByCourseIdOrderByOrderIndex(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    
    public ChapterResponse getChapter(Long courseId, Long chapterId) {
        Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        return mapToResponse(chapter);
    }
    
    
    public ChapterResponse updateChapter(Long courseId, Long chapterId, UpdateChapterRequest request) {
        Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        if (request.getTitle() != null) {
            chapter.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            chapter.setContent(request.getContent());
        }
        if (request.getOrderIndex() != null) {
            chapter.setOrderIndex(request.getOrderIndex());
        }
        
        Chapter updated = chapterRepository.save(chapter);
        return mapToResponse(updated);
    }
    
   
    public void deleteChapter(Long courseId, Long chapterId) {
        Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        //chapterRepository.delete(chapter);
        deleteChapterWithDependencies(chapter);
    }


    private void deleteChapterWithDependencies(Chapter chapter) {
    Long chapterId = chapter.getId();

    chapterRepository.deleteById(chapterId);
}
    
    

    public ChapterResourceResponse uploadResource(Long courseId, Long chapterId, 
                                                   MultipartFile file, Long uploadedBy) throws IOException {
        try {
            
            
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }
            
            
            
            long fileSize = file.getSize();
            if (fileSize == 0) {
                throw new IllegalArgumentException("Le fichier est vide (taille = 0)");
            }
            
            if (fileSize > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                    String.format("Le fichier est trop volumineux: %d MB (max: %d MB)", 
                        fileSize / (1024 * 1024), 
                        MAX_FILE_SIZE / (1024 * 1024))
                );
            }
            
            
            
            Chapter chapter = chapterRepository.findByIdAndCourseId(chapterId, courseId)
                    .orElseThrow(() -> new RuntimeException("Chapter not found"));
            
            
                    
            String mimeType = file.getContentType();
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "application/octet-stream";
            }
            
            log.info("Uploading file: {} (size: {} bytes, type: {})", 
                file.getOriginalFilename(), fileSize, mimeType);
            
            
                
            if (fileSize > MEMORY_THRESHOLD) {
                Runtime runtime = Runtime.getRuntime();
                long freeMemory = runtime.freeMemory();
                if (freeMemory < fileSize * 1.5) {
                    throw new RuntimeException(
                        "Mémoire insuffisante pour traiter ce fichier. " +
                        "Libre: " + (freeMemory / (1024 * 1024)) + "MB, " +
                        "Nécessaire: " + ((fileSize * 1.5) / (1024 * 1024)) + "MB"
                    );
                }
            }
            
            
            
            ChapterResource resource = new ChapterResource();
            resource.setFileName(file.getOriginalFilename());
            resource.setFileData(file.getBytes()); // ⚠️ Charge en mémoire (OK pour les fichiers < 100MB)
            resource.setMimeType(mimeType);
            resource.setFileSize(fileSize);
            
            
            
            ResourceType resourceType = determineResourceType(mimeType);
            resource.setResourceType(resourceType);
            
            resource.setChapter(chapter);
            resource.setUploadedBy(uploadedBy);
            
            
            
            ChapterResource saved = resourceRepository.save(resource);
            
            log.info("File uploaded successfully: {} (ID: {})", 
                saved.getFileName(), saved.getId());
            
            return mapResourceToResponse(saved);
            
        } catch (IllegalArgumentException e) {
            log.warn("File upload validation error: {}", e.getMessage());
            throw e;
        } catch (OutOfMemoryError e) {
            log.error("Out of memory while uploading file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException(
                "Le fichier est trop volumineux pour être traité en mémoire. " +
                "Veuillez utiliser un fichier plus petit.", e
            );
        } catch (IOException e) {
            log.error("IO error while uploading file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Erreur lors de la lecture du fichier: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage(), e);
        }
    }
    
   
    
    public List<ChapterResourceResponse> getChapterResources(Long courseId, Long chapterId) {
        // Vérifier que le chapitre existe
        chapterRepository.findByIdAndCourseId(chapterId, courseId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        return resourceRepository.findByChapterId(chapterId)
                .stream()
                .map(this::mapResourceToResponse)
                .collect(Collectors.toList());
    }
    
    
    
    public ChapterResource downloadResource(Long courseId, Long chapterId, Long resourceId) {
        ChapterResource resource = resourceRepository.findByIdAndChapterId(resourceId, chapterId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        
        // Vérifier que la ressource appartient au bon cours
        if (!resource.getChapter().getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Resource does not belong to this course");
        }
        
        return resource;
    }
    
   
    
    public void deleteResource(Long courseId, Long chapterId, Long resourceId) {
        ChapterResource resource = resourceRepository.findByIdAndChapterId(resourceId, chapterId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        
        // Vérifier que la ressource appartient au bon cours
        if (!resource.getChapter().getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Resource does not belong to this course");
        }
        
        resourceRepository.delete(resource);
    }
    
    
    
    
    private ResourceType determineResourceType(String mimeType) {
        if (mimeType == null) {
            return ResourceType.OTHER;
        }
        
        if (mimeType.startsWith("video/")) {
            return ResourceType.VIDEO;
        } else if (mimeType.startsWith("image/")) {
            return ResourceType.IMAGE;
        } else if (mimeType.contains("pdf")) {
            return ResourceType.PDF;
        } else if (mimeType.startsWith("audio/")) {
            return ResourceType.AUDIO;
        } else if (mimeType.contains("document") || mimeType.contains("word")) {
            return ResourceType.DOCUMENT;
        } else if (mimeType.contains("zip") || mimeType.contains("rar")) {
            return ResourceType.ARCHIVE;
        }
        
        return ResourceType.OTHER;
    }
    
    private ChapterResponse mapToResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getContent(),
                chapter.getOrderIndex(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt(),
                chapter.getResources() != null ? 
                    chapter.getResources().stream()
                        .map(this::mapResourceToResponse)
                        .collect(Collectors.toList()) : 
                    List.of()
        );
    }
    
    private ChapterResourceResponse mapResourceToResponse(ChapterResource resource) {
        return new ChapterResourceResponse(
                resource.getId(),
                resource.getFileName(),
                resource.getResourceType().toString(),
                resource.getMimeType(),
                resource.getFileSize(),
                resource.getCreatedAt()
        );
    }
}

