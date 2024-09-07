package com.example.backend.repository;

import com.example.backend.model.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, String> {
    VideoEntity findByVideoId(String videoId);

    List<VideoEntity> findByChildId(String childId);
}
