package com.example.noteshare.controller;

import com.example.noteshare.entity.Note;
import com.example.noteshare.entity.Student;
import com.example.noteshare.entity.Subject;
import com.example.noteshare.service.NoteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    private final Path uploadDirectory = Paths.get("uploads");

    public NoteController(NoteService noteService) {
        this.noteService = noteService;

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory");
        }
    }

    // CREATE - Upload a note
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Note createNote(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam("unitNumber") Integer unitNumber,
            @RequestParam("studentId") Long studentId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a PDF file");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null ||
                !originalName.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are allowed");
        }

        String fileName = System.currentTimeMillis() + "_" + originalName;

        Path filePath = uploadDirectory.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        Note note = new Note();

        note.setTitle(title);
        note.setDescription(description);
        note.setUnitNumber(unitNumber);
        note.setFilePath(filePath.toString());

        Student student = new Student();
        student.setId(studentId);

        Subject subject = new Subject();
        subject.setId(subjectId);

        note.setStudent(student);
        note.setSubject(subject);

        return noteService.createNote(note);
    }

    // READ - Get all notes
    @GetMapping
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    // READ - Get note by ID
    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }

    // READ - Search notes
    @GetMapping("/search")
    public List<Note> searchNotes(@RequestParam String title) {
        return noteService.searchNotes(title);
    }

    // READ - Notes by subject
    @GetMapping("/subject/{subjectId}")
    public List<Note> getNotesBySubject(
            @PathVariable Long subjectId) {

        return noteService.getNotesBySubject(subjectId);
    }

    // READ - Notes uploaded by student
    @GetMapping("/student/{studentId}")
    public List<Note> getNotesByStudent(
            @PathVariable Long studentId) {

        return noteService.getNotesByStudent(studentId);
    }

    // UPDATE - Only original uploader can update
    @PutMapping("/{id}")
    public Note updateNote(
            @PathVariable Long id,
            @RequestBody Note updatedNote,
            @RequestParam Long studentId) {

        return noteService.updateNote(id, updatedNote, studentId);
    }

    // DELETE - Only original uploader can delete
    @DeleteMapping("/{id}")
    public String deleteNote(
            @PathVariable Long id,
            @RequestParam Long studentId) {

        noteService.deleteNote(id, studentId);

        return "Note deleted successfully";
    }

    // DOWNLOAD - Download PDF
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id) throws IOException {

        Note note = noteService.getNoteById(id);

        Path filePath = Paths.get(note.getFilePath());

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = Files.readAllBytes(filePath);

        String fileName = filePath.getFileName().toString();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }
}