package com.videotogether.controller;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final String uploadDir = System.getProperty("java.io.tmpdir") + "/videotogether_uploads/";

    public VideoController() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            
            Path path = Paths.get(uploadDir + fileName);
            // Transfer stream directly to file to prevent OutOfMemoryError on large files
            file.transferTo(path.toFile());

            response.put("fileName", fileName);
            response.put("streamUrl", "/api/video/stream/" + fileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/stream/{fileName}")
    public ResponseEntity<ResourceRegion> streamVideo(@PathVariable String fileName, @RequestHeader HttpHeaders headers) throws IOException {
        File file = new File(uploadDir + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        UrlResource video = new UrlResource(file.toURI());
        ResourceRegion region = getResourceRegion(video, headers);
        
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType != null) {
                mediaType = MediaType.parseMediaType(mimeType);
            }
        } catch (IOException e) {}

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .body(region);
    }

    private ResourceRegion getResourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
        long contentLength = video.contentLength();
        List<HttpRange> ranges = headers.getRange();
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1); // 1MB chunks
            return new ResourceRegion(video, start, rangeLength);
        } else {
            long rangeLength = Math.min(1024 * 1024, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }
    }
}
