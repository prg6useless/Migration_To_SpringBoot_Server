package moviemate.server.controller;

import moviemate.server.model.User;
import moviemate.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class is where all the user requests are handled and
 * required/appropriate
 * responses are sent
 */
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Get all users
     * URL: localhost:8080/v1/users/
     * Purpose: Fetches all the users in the user table
     * 
     * @return List of Users
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    /**
     * Get User by id
     * URL: localhost:8080/v1/users/{id}
     * Purpose: Fetches user with the given id
     * 
     * @param id - user id
     * @return User with the given id
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(userService.getUserById(id));
    }

    /**
     * Create/Register new user
     * URL: localhost:8080/v1/users/register
     * Purpose: Save a User entity
     * 
     * @param user - Request body is a User entity
     * @return Saved User entity
     */
    @PostMapping("/register")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        return ResponseEntity.ok().body(userService.saveUser(user));
    }

    /**
     * This method is called when a POST request is made
     * URL: localhost:8080/v1/users/lgin
     * Purpose: Login a User entity
     * 
     * @param user - Request body is a User entity
     * @return Saved User entity
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User user) {
        String token = userService.loginUser(user);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    /**
     * This method is called when a PUT request is made
     * URL: localhost:8080/v1/users/{id}/profile
     * Purpose: Update a User entity
     * 
     * @param user - User entity to be updated
     * @return Updated User
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok().body(userService.updateUser(user, id));
    }

    /**
     * This method is called when a DELETE request is made
     * URL: localhost:8080/v1/users/1 (or any other id)
     * Purpose: Delete a User entity
     * 
     * @param id - user's id to be deleted
     * @return a String message indicating user record has been deleted successfully
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Integer id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().body("Deleted user successfully");
    }
}
