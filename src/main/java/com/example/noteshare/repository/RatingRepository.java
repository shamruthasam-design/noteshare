package com.example.noteshare.repository;

import com.example.noteshare.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByStudentIdAndNoteId(Long studentId, Long noteId);

    List<Rating> findByNoteId(Long noteId);
}