package com.hieu.ms.feature.user;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityExistsException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hieu.ms.feature.role.Role;
import com.hieu.ms.feature.role.RoleRepository;
import com.hieu.ms.feature.subscription.SubscriptionService;
import com.hieu.ms.feature.user.dto.UserCreationRequest;
import com.hieu.ms.feature.user.dto.UserRegisteredEvent;
import com.hieu.ms.feature.user.dto.UserResponse;
import com.hieu.ms.feature.user.dto.UserSearchRequest;
import com.hieu.ms.feature.user.dto.UserUpdateRequest;
import com.hieu.ms.shared.constant.PredefinedRole;
import com.hieu.ms.shared.dto.response.PageResponse;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;
import com.querydsl.core.BooleanBuilder;

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

    ApplicationEventPublisher eventPublisher;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);

        //  Validate email is not null or empty
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        //  Check email duplication
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        log.info("🔍 Creating user with email: {} and username: {}", request.getEmail(), request.getUsername());

        User user = userMapper.toUser(request);

        // ✅ Ensure email is set even if mapper fails
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim().toLowerCase());
            log.warn("⚠️ Email was null after mapping, manually set to: {}", user.getEmail());
        }

        log.info("🔍 After mapping - User email: {}, username: {}", user.getEmail(), user.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        subscriptionService.createSubscription(user);

        User savedUser = userRepository.save(user);
        log.info("👤 User created successfully: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        // ✅ Publish event for auto-accepting pending invitations
        log.info("📡 Publishing UserRegisteredEvent for email: {}", savedUser.getEmail());
        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser.getEmail()));
        log.info("📡 UserRegisteredEvent published successfully for: {}", savedUser.getEmail());

        return userMapper.toUserResponse(savedUser);
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

    /**
     * Get users with pagination and search using QueryDSL
     *
     * @param request Search criteria and pagination info
     * @return PageResponse containing users
     */
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> getUsers(UserSearchRequest request) {
        // Build predicate
        QUser qUser = QUser.user;
        BooleanBuilder predicate = new BooleanBuilder();

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            predicate.and(qUser.username
                    .containsIgnoreCase(keyword)
                    .or(qUser.email.containsIgnoreCase(keyword))
                    .or(qUser.firstName.containsIgnoreCase(keyword))
                    .or(qUser.lastName.containsIgnoreCase(keyword)));
        }

        // Get pageable from request
        Pageable pageable = request.getPageable(Sort.by("username").ascending());

        // Execute query
        Page<UserResponse> userPage =
                userRepository.findAll(predicate, pageable).map(userMapper::toUserResponse);

        return PageResponse.of(userPage);
    }

    /**
     * Legacy method - Get all users without pagination
     * @deprecated Use getUsers(UserSearchRequest) instead for better performance
     */
    @Deprecated
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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUserProjectSize(User user, int size) {
        user.setProjectSize(user.getProjectSize() + size);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public User findUserById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new EntityExistsException("user not found with ID" + userId);
        return user.get();
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
