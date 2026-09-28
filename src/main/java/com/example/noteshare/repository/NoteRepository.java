package com.example.noteshare.repository;

import com.example.noteshare.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByTitleContainingIgnoreCase(String title);

    List<Note> findBySubjectId(Long subjectId);

    List<Note> findByStudentId(Long studentId);
}