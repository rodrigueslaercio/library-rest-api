package project.library.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.library.models.Book;
import project.library.models.User;
import project.library.repositories.UserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;
import project.library.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/users")
    public ResponseEntity<Object> create(@RequestBody User userModel) {
        var user = userRepository.findByEmail(userModel.getEmail());

        if(user != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already registered");
        }

        var hashPassword = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());

        userModel.setPassword(hashPassword);
        var userCreated = this.userRepository.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userCreated);
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers(HttpServletRequest request) {
        var user = this.userRepository.findById((UUID) request.getAttribute("idUser"));

        if (user.get().getAdmin() == null || user.get().getAdmin().equals(false)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not allowed");
        }

        List<User> userList = userRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }


    @GetMapping("/users/books/{id}")
    public ResponseEntity<Object> getBooksByUserId(@PathVariable(value = "id") UUID id) {
        Optional<User> user = userRepository.findById(id);

        if(user.get().getBooks().isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User has no books registered");
        }

        return ResponseEntity.status(HttpStatus.OK).body(user.get().getBooks());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> update(@PathVariable(value = "id") UUID id, @RequestBody User userModel,
                                         HttpServletRequest request) {
        Optional<User> user = userRepository.findById(id);

        var idUser = request.getAttribute("idUser");

        if(!user.get().getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not Allowed");
        }

        var selectedUser = user.get();
        Utils.copyNonNullProperties(userModel, selectedUser);
        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(selectedUser));
    }
}
