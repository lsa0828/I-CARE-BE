package com.example.backend.service;

import com.example.backend.dto.GestureCheck;
import com.example.backend.dto.VideoResult;
import com.example.backend.model.GestureEntity;
import com.example.backend.repository.ChildRepository;
import com.example.backend.repository.GestureRepository;
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
import java.util.Optional;

@Slf4j
@Service
public class GestureService {
    @Autowired
    private GestureRepository gestureRepository;
    @Autowired
    private ChildRepository childRepository;
    @Value("${python.connected.url}")
    private String pythonUrl;

    public String start(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/gesture/start?childId=" + childId;
            GestureCheck response = restTemplate.getForObject(url, GestureCheck.class);
            return response.getCor_label();
        } catch (Exception e) {
            throw new RuntimeException("Could not start camera: ", e);
        }
    }

    public List<GestureEntity> stop(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/gesture/stop?childId=" + childId;
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
            throw new RuntimeException("Could not stop camera: ", e);
        }
    }

    public List<GestureEntity> saveLabel(String parentId, String childId, List<VideoResult> result) {
        validate(parentId, childId);
        List<GestureEntity> entities = new ArrayList<>();
        for(VideoResult item : result) {
            String fineName = item.getFileName();
            String label = item.getLabel();
            GestureEntity entity = GestureEntity.builder()
                    .parentId(parentId)
                    .childId(childId)
                    .fileName(fineName)
                    .label(label)
                    .build();
            GestureEntity savedEntity = gestureRepository.save(entity);
            entities.add(savedEntity);
        }
        return entities;
    }

    public String againLabel(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/gesture/again?childId=" + childId;
            GestureCheck response = restTemplate.getForObject(url, GestureCheck.class);
            return response.getCor_label();
        } catch (Exception e) {
            throw new RuntimeException("Could not get again Label: ", e);
        }
    }

    public Resource getHintImage(String parentId, String childId) {
        validate(parentId, childId);
        try {
            String url = pythonUrl + "/gesture/hint?childId=" + childId;
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

    public GestureCheck predict(String parentId, String childId) {
        validate(parentId, childId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = pythonUrl + "/gesture/predict?childId=" + childId;
            return restTemplate.getForObject(url, GestureCheck.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not start camera: ", e);
        }
    }

    public Resource getImage(String parentId, String childId, String gestureId) {
        try {
            GestureEntity entity = getEntity(parentId, childId, gestureId);
            String fileName = entity.getFileName();

            String url = pythonUrl + "/gesture/image?fileName=" + fileName;
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

    public GestureEntity getEntity(String parentId, String childId, String gestureId) {
        validate(parentId, childId);
        Optional<GestureEntity> optionalGesture = gestureRepository.findByGestureId(gestureId);
        GestureEntity gestureEntity = optionalGesture.orElseThrow(() ->
            new RuntimeException("Gesture entity not found for gestureId: " + gestureId)
        );
        if (!childId.equals(gestureEntity.getChildId())) {
            log.error("Not the owner of the profile");
            throw new RuntimeException("Not the owner of the profile");
        }
        return gestureEntity;
    }

    public void validate(String parentId, String childId) {
        if (!parentId.equals(childRepository.findByChildId(childId).getParentId())) {
            log.error("Child's parent and current parent do not match.");
            throw new RuntimeException("Child's parent and current parent do not match.");
        }
    }
}
