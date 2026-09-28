package com.example.noteshare.service;

import com.example.noteshare.entity.Note;
import com.example.noteshare.entity.Rating;
import com.example.noteshare.entity.Student;
import com.example.noteshare.repository.NoteRepository;
import com.example.noteshare.repository.RatingRepository;
import com.example.noteshare.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final StudentRepository studentRepository;
    private final NoteRepository noteRepository;

    public RatingService(
            RatingRepository ratingRepository,
            StudentRepository studentRepository,
            NoteRepository noteRepository) {

        this.ratingRepository = ratingRepository;
        this.studentRepository = studentRepository;
        this.noteRepository = noteRepository;
    }

    public Rating addRating(Rating rating) {

        if (rating.getScore() < 1 || rating.getScore() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        if (rating.getStudent() == null ||
                rating.getStudent().getId() == null) {
            throw new RuntimeException("Student is required");
        }

        if (rating.getNote() == null ||
                rating.getNote().getId() == null) {
            throw new RuntimeException("Note is required");
        }

        Long studentId = rating.getStudent().getId();
        Long noteId = rating.getNote().getId();

        if (ratingRepository
                .findByStudentIdAndNoteId(studentId, noteId)
                .isPresent()) {

            throw new RuntimeException(
                    "You have already rated this note");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new RuntimeException("Note not found"));

        rating.setStudent(student);
        rating.setNote(note);

        return ratingRepository.save(rating);
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    public List<Rating> getRatingsByNote(Long noteId) {
        return ratingRepository.findByNoteId(noteId);
    }

    public double getAverageRating(Long noteId) {

        List<Rating> ratings =
                ratingRepository.findByNoteId(noteId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        double total = 0;

        for (Rating rating : ratings) {
            total += rating.getScore();
        }

        return total / ratings.size();
    }

    // Get top-rated notes for a subject
    public List<Note> getTopRatedNotesBySubject(Long subjectId) {

        List<Note> notes =
                noteRepository.findBySubjectId(subjectId);

        List<Note> ratedNotes = new ArrayList<>(notes);

        ratedNotes.sort(
                Comparator.comparingDouble(
                        (Note note) -> getAverageRating(note.getId())
                ).reversed()
        );

        return ratedNotes;
    }
}