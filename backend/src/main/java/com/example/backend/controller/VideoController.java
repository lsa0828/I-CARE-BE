package com.example.backend.controller;

import com.example.backend.dto.VideoDTO;
import com.example.backend.model.VideoEntity;
import com.example.backend.service.VideoService;
import com.google.common.net.HttpHeaders;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/video")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @GetMapping("/running")
    public ResponseEntity<?> isVideoRunning(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        Boolean running = videoService.isRunning(parentId, childId);
        JSONObject jsonObject = new JSONObject();
        if (running) {
            jsonObject.put("running", true);
        } else {
            jsonObject.put("running", false);
        }
        return ResponseEntity.ok().body(jsonObject);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startVideo(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        Boolean result = videoService.start(parentId, childId);
        JSONObject jsonObject = new JSONObject();
        if (result) {
            jsonObject.put("message", "Start Video");
        } else {
            jsonObject.put("message", "None");
        }
        return ResponseEntity.ok().body(jsonObject);
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopVideo(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        List<VideoEntity> entities = videoService.stop(parentId, childId);
        if (entities.isEmpty()) {
            return ResponseEntity.ok().body(Collections.emptyList());
        }
        List<VideoDTO> dtos = entities.stream().map(VideoDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/image")
    public ResponseEntity<?> getImage(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId, @RequestParam("videoId") String videoId) {
        Resource resource = videoService.getImage(parentId, childId, videoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("/list")
    public ResponseEntity<?> showVideoList(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        List<VideoEntity> entities = videoService.showList(parentId, childId);
        if (entities.isEmpty()) {
            return ResponseEntity.ok().body(Collections.emptyList());
        }
        List<VideoDTO> dtos = entities.stream().map(VideoDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(dtos);
    }
}
