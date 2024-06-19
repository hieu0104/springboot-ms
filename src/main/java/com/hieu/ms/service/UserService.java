package com.hieu.ms.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.hieu.ms.constant.PredefinedRole;
import com.hieu.ms.entity.Subscription;
import com.hieu.ms.exception.AppException;
import com.hieu.ms.exception.ErrorCode;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hieu.ms.dto.request.UserCreationRequest;
import com.hieu.ms.dto.request.UserUpdateRequest;
import com.hieu.ms.dto.response.UserResponse;
import com.hieu.ms.entity.Role;
import com.hieu.ms.entity.User;
import com.hieu.ms.mapper.UserMapper;
import com.hieu.ms.repository.RoleRepository;
import com.hieu.ms.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SubscriptionService subscriptionService;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        subscriptionService.createSubscription(user);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse findUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUserProjectSize(User user, int size) {
        user.setProjectSize(user.getProjectSize() + size);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public User findUserById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new EntityExistsException("user not found with ID" + userId);
        return user.get();
    }
}
