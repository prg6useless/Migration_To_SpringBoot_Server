package moviemate.server.controller;

import moviemate.server.dto.request.ChangePasswordRequest;
import moviemate.server.dto.request.LoginRequest;
import moviemate.server.dto.request.ResetPasswordRequest;
import moviemate.server.dto.response.LoginResponse;
import moviemate.server.dto.response.MessageResponse;
import moviemate.server.dto.response.UserResponse;
import moviemate.server.model.User;
import moviemate.server.security.UserDetailsImpl;
import moviemate.server.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // PUBLIC ENDPOINTS

    /**
     * Create/Register new user
     * URL: localhost:8080/v1/users/register
     * Purpose: Save a User entity
     * 
     * @param user - Request body is a User entity
     * @return Saved User entity
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> saveUser(@RequestBody User user) {
        String message = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(message));
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
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest user) {
        return ResponseEntity.ok(userService.loginUser(user));
    }

    // AUTHENTICATED USERS

    /**
     * This method is called when a PUT request is made
     * URL: localhost:8080/v1/users/{id}/profile
     * Purpose: Update a User entity
     * 
     * @param user - User entity to be updated
     * @return Updated User
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getMyProfile(userDetails.getId()));
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user, id));
    }

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ChangePasswordRequest request) {
        String message = userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    // ADMIN ONLY

    /**
     * Get all users
     * URL: localhost:8080/v1/users/
     * Purpose: Fetches all the users in the user table
     * 
     * @return List of Users
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(userService.getUserById(id));
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUserById(@PathVariable Integer id) {
        String message = userService.deleteUserById(id);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PatchMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> resetPasswordByAdmin(@RequestBody ResetPasswordRequest request) {
        String message = userService.resetPasswordByAdmin(request);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> blockUserByAdmin(@PathVariable Integer id) {
        String message = userService.blockUserByAdmin(id);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserProfileByAdmin(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUserProfileByAdmin(user, id));
    }

}
