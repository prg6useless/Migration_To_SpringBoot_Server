package moviemate.server.service;

import moviemate.server.model.User;
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
import java.util.Optional;

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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        log.info("User with id: {} doesn't exist", id);
        return null;
    }

    public User saveUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRoles(List.of(userRole));

        User savedUser = userRepository.save(user);

        log.info("User with id: {} saved successfully", user.getId());
        return savedUser;
    }

    public String loginUser(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + user.getEmail()));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password"); // can also create a custom InvalidCredentialsException
        }

        return hashUtil.generateToken(existingUser);
    }

    public User updateUser(User userUpdates, Integer id) {
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
        return updatedUser;
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }
}
