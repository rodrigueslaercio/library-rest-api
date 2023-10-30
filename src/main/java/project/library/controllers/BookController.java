package project.library.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.library.dtos.BookRecordDTO;
import project.library.models.Book;
import project.library.repositories.BookRepository;
import project.library.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class BookController {

    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/books")
    public ResponseEntity<Object> createBook(@RequestBody @Valid BookRecordDTO bookRecordDTO, HttpServletRequest request) {
        var user = userRepository.findById((UUID) request.getAttribute("idUser"));

        if (user.get().getAdmin().equals(false)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Not allowed");
        }

        var book = new Book();
        BeanUtils.copyProperties(bookRecordDTO, book);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookRepository.save(book));
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        if(!books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(books);
        }

        // TODO Error message when list is empty
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Object> getOneBook(@PathVariable(value = "id") UUID id) {
        Optional<Book> book = bookRepository.findById(id);
        if(book.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(book.get());
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Object> updateBook(@PathVariable(value = "id") UUID id,
                                             @RequestBody @Valid BookRecordDTO bookRecordDTO, HttpServletRequest request) {

        var user = userRepository.findById((UUID) request.getAttribute("idUser"));
        if(user.get().getAdmin().equals(false)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Not allowed");
        }

        Optional<Book> book = bookRepository.findById(id);
        if(book.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
        var bookFound = book.get();
        BeanUtils.copyProperties(bookRecordDTO, bookFound);
        return ResponseEntity.status(HttpStatus.OK).body(bookRepository.save(bookFound));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Object> deleteBook(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        Optional<Book> book = bookRepository.findById(id);
        var user = userRepository.findById((UUID) request.getAttribute("idUser"));

        if(user.get().getAdmin().equals(false)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not allowed");
        }

        if(book.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }

        bookRepository.delete(book.get());
        return ResponseEntity.status(HttpStatus.OK).body("Book deleted successfully");
    }
}
