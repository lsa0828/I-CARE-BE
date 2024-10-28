package com.example.backend.controller;

import com.example.backend.dto.GestureCheck;
import com.example.backend.dto.GestureDTO;
import com.example.backend.dto.VideoDTO;
import com.example.backend.model.GestureEntity;
import com.example.backend.service.GestureService;
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
@RequestMapping("api/gesture")
public class GestureController {
    @Autowired
    private GestureService gestureService;

    @GetMapping("/start")
    public ResponseEntity<?> startCamera(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        String corLabel = gestureService.start(parentId, childId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("corLabel", corLabel);
        return ResponseEntity.ok().body(jsonObject);
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopCamera(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        List<GestureEntity> entities = gestureService.stop(parentId, childId);
        if (entities.isEmpty()) {
            return ResponseEntity.ok().body(Collections.emptyList());
        }
        List<GestureDTO> dtos = entities.stream().map(GestureDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/again")
    public ResponseEntity<?> againLabel(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        String corLabel = gestureService.againLabel(parentId, childId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("corLabel", corLabel);
        return ResponseEntity.ok().body(jsonObject);
    }

    @GetMapping("/image/hint")
    public ResponseEntity<?> getHintImage(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        Resource resource = gestureService.getHintImage(parentId, childId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
/*
    @GetMapping("/predict")
    public ResponseEntity<?> predictGesture(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId) {
        GestureCheck checks = gestureService.predict(parentId, childId);
        return ResponseEntity.ok().body(checks);
    }
*/
    @GetMapping("/image")
    public ResponseEntity<?> getResultImage(@AuthenticationPrincipal String parentId, @RequestParam("childId") String childId, @RequestParam("gestureId") String gestureId) {
        Resource resource = gestureService.getImage(parentId, childId, gestureId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
