package com.example.noteshare.service;

import com.example.noteshare.entity.Note;
import com.example.noteshare.repository.NoteRepository;
import com.example.noteshare.repository.StudentRepository;
import com.example.noteshare.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public NoteService(NoteRepository noteRepository,
                       StudentRepository studentRepository,
                       SubjectRepository subjectRepository) {
        this.noteRepository = noteRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    // Create / Upload note
    public Note createNote(Note note) {

        if (note.getStudent() == null || note.getStudent().getId() == null) {
            throw new RuntimeException("Student is required");
        }

        if (note.getSubject() == null || note.getSubject().getId() == null) {
            throw new RuntimeException("Subject is required");
        }

        var student = studentRepository.findById(note.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        var subject = subjectRepository.findById(note.getSubject().getId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        note.setStudent(student);
        note.setSubject(subject);
        note.setUploadedAt(LocalDateTime.now());

        return noteRepository.save(note);
    }

    // Get all notes
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // Get note by ID
    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    // Search notes by title
    public List<Note> searchNotes(String title) {
        return noteRepository.findByTitleContainingIgnoreCase(title);
    }

    // Get notes by subject
    public List<Note> getNotesBySubject(Long subjectId) {
        return noteRepository.findBySubjectId(subjectId);
    }

    // Get notes uploaded by a student
    public List<Note> getNotesByStudent(Long studentId) {
        return noteRepository.findByStudentId(studentId);
    }

    // Update note - only original uploader can update
    public Note updateNote(Long noteId, Note updatedNote, Long studentId) {

        Note existingNote = getNoteById(noteId);

        if (!existingNote.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Only the original uploader can update this note");
        }

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setDescription(updatedNote.getDescription());
        existingNote.setFilePath(updatedNote.getFilePath());
        existingNote.setUnitNumber(updatedNote.getUnitNumber());

        return noteRepository.save(existingNote);
    }

    // Delete note - only original uploader can delete
    public void deleteNote(Long noteId, Long studentId) {

        Note existingNote = getNoteById(noteId);

        if (!existingNote.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Only the original uploader can delete this note");
        }

        noteRepository.delete(existingNote);
    }
}