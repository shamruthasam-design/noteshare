package com.example.noteshare.controller;

import com.example.noteshare.entity.Note;
import com.example.noteshare.entity.Rating;
import com.example.noteshare.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public Rating addRating(@Valid @RequestBody Rating rating) {
        return ratingService.addRating(rating);
    }

    @GetMapping
    public List<Rating> getAllRatings() {
        return ratingService.getAllRatings();
    }

    @GetMapping("/note/{noteId}")
    public List<Rating> getRatingsByNote(
            @PathVariable Long noteId) {

        return ratingService.getRatingsByNote(noteId);
    }

    @GetMapping("/note/{noteId}/average")
    public Map<String, Object> getAverageRating(
            @PathVariable Long noteId) {

        return Map.of(
                "noteId", noteId,
                "averageRating",
                ratingService.getAverageRating(noteId)
        );
    }

    // Top-rated notes by subject
    @GetMapping("/subject/{subjectId}/top")
    public List<Note> getTopRatedNotesBySubject(
            @PathVariable Long subjectId) {

        return ratingService
                .getTopRatedNotesBySubject(subjectId);
    }
}