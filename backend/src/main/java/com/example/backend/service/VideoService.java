package com.example.backend.service;

import com.example.backend.dto.VideoResult;
import com.example.backend.model.VideoEntity;
import com.example.backend.repository.ChildRepository;
import com.example.backend.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private ChildRepository childRepository;
    @Value("${python.connected.url}")
    private String pythonUrl;

    public Boolean isRunning(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/video/running?childId=" + childId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (Boolean) body.get("running");
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not find running: ", e);
        }
    }

    public Boolean start(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/video/start?childId=" + childId;
            restTemplate.postForObject(url, null, String.class);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Could not start video: ", e);
        }
    }

    public List<VideoEntity> stop(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/video/stop?childId=" + childId;
            ResponseEntity<List<VideoResult>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<List<VideoResult>>() {}
            );
            List<VideoResult> result = response.getBody();
            if (result != null) {
                return saveLabel(parentId, childId, result);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not stop video: ", e);
        }
    }

    public List<VideoEntity> saveLabel(String parentId, String childId, List<VideoResult> result) {
        validate(parentId, childId);
        List<VideoEntity> entities = new ArrayList<>();
        for(VideoResult item : result) {
            String fineName = item.getFileName();
            String label = item.getLabel();
            VideoEntity entity = VideoEntity.builder()
                    .parentId(parentId)
                    .childId(childId)
                    .fileName(fineName)
                    .label(label)
                    .build();
            VideoEntity savedEntity = videoRepository.save(entity);
            entities.add(savedEntity);
        }
        return entities;
    }

    public VideoEntity show(String parentId, String childId, String videoId) {
        validate(parentId, childId);
        if(!childId.equals(videoRepository.findByVideoId(videoId).getChildId())) {
            log.error("Not the owner of the profile");
            throw new RuntimeException("Not the owner of the profile");
        }
        return videoRepository.findByVideoId(videoId);
    }

    public Resource getImage(String parentId, String childId, String videoId) {
        try {
            VideoEntity entity = show(parentId, childId, videoId);
            String fileName = entity.getFileName();

            String url = pythonUrl + "/video/image?fileName=" + fileName;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            if(response.getStatusCode().is2xxSuccessful()) {
                return new ByteArrayResource(response.getBody());
            } else {
                throw new RuntimeException("Failed to retrieve image from Flask server");
            }
        } catch(Exception e) {
            throw new RuntimeException("Could not read file: ", e);
        }
    }

    public List<VideoEntity> showList(String parentId, String childId) {
        validate(parentId, childId);
        return videoRepository.findByChildId(childId);
    }

    public void validate(String parentId, String childId) {
        if (!parentId.equals(childRepository.findByChildId(childId).getParentId())) {
            log.error("Child's parent and current parent do not match.");
            throw new RuntimeException("Child's parent and current parent do not match.");
        }
    }
}
