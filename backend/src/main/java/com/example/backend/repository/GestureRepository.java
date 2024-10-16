package com.example.backend.repository;

import com.example.backend.model.GestureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GestureRepository extends JpaRepository<GestureEntity, String> {
    Optional<GestureEntity> findByGestureId(String gestureId);
}
