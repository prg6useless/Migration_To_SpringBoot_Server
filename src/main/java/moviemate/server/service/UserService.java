package moviemate.server.service;

import moviemate.server.model.User;
import moviemate.server.dto.request.ChangePasswordRequest;
import moviemate.server.dto.request.LoginRequest;
import moviemate.server.dto.request.ResetPasswordRequest;
import moviemate.server.dto.response.LoginResponse;
import moviemate.server.dto.response.UserResponse;
import moviemate.server.exception.UserNotFoundException;
import moviemate.server.model.Role;
import moviemate.server.repository.UserRepository;
import moviemate.server.utils.HashUtil;
import moviemate.server.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service layer is where all the business logic lies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private HashUtil hashUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─── Helper: map User → UserResponse ──────────────────────────────────────
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .image(user.getImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    // register
    public String saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hash
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRoles(List.of(userRole));

        userRepository.save(user);
        log.info("User with id: {} saved successfully", user.getId());
        return "User registered successfully";
    }

    // login
    public LoginResponse loginUser(LoginRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Invalid password"); // can also create a custom InvalidCredentialsException
        }

        if (!existingUser.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        String token = hashUtil.generateToken(existingUser);
        return new LoginResponse(token, "User logged in successfully");
    }

    // get my profile
    public UserResponse getMyProfile(Integer id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toUserResponse(existingUser);
    }

    // update my profile
    public UserResponse updateUser(User userUpdates, Integer id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }
        if (userUpdates.getEmail() != null) {
            existingUser.setEmail(userUpdates.getEmail());
        }
        // if (userUpdates.getPassword() != null) {
        // existingUser.setPassword(userUpdates.getPassword());
        // }
        // if (userUpdates.getImage() != null) {
        // existingUser.setImage(userUpdates.getImage());
        // }
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        log.info("User with id: {} updated successfully", updatedUser.getId());
        return toUserResponse(updatedUser);
    }

    public String changePassword(Integer id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed for user with id: {}", id);
        return "Password changed successfully";
    }

    // get all users (only admin access)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // get user by id (only admin access)
    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toUserResponse(user);
    }

    // delete user byu id (only admin access)
    public String deleteUserById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User with id: {} deleted successfully", id);
        return "User deleted successfully";
    }

    // reset user password (only admin access)
    public String resetPasswordByAdmin(ResetPasswordRequest request) {
        User existingUser = userRepository.findById(request.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getId()));
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        existingUser.setPassword(encodedPassword);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
        log.info("Password reset by admin for user with id: {}", request.getId());
        return "Password reset successfully";
    }

    // reset user password (only admin access)
    public String blockUserByAdmin(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setIsActive(false);
        user.setIsEmailVerified(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("User with id: {} blocked by admin", id);
        return "User blocked successfully";
    }

    // reset user password (only admin access)
    public UserResponse updateUserProfileByAdmin(User userUpdates, Integer id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }
        if (userUpdates.getEmail() != null) {
            existingUser.setEmail(userUpdates.getEmail());
        }
        if (userUpdates.getIsActive() != null) {
            existingUser.setIsActive(userUpdates.getIsActive());
        }
        // if (userUpdates.getImage() != null) {
        // existingUser.setImage(userUpdates.getImage());
        // }
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        log.info("User with id: {} updated successfully", updatedUser.getId());
        return toUserResponse(updatedUser);
    }

}
