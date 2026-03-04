package com.hieu.ms.feature.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import jakarta.persistence.EntityExistsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hieu.ms.feature.role.RoleService;
import com.hieu.ms.feature.subscription.SubscriptionService;
import com.hieu.ms.feature.user.dto.UserCreationRequest;
import com.hieu.ms.feature.user.dto.UserResponse;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleService roleService;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    SubscriptionService subscriptionService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    UserService userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().username("john").email("john@example.com").build();
        user.setId("user-1");

        userResponse = UserResponse.builder().id("user-1").username("john").build();
    }

    // -------- createUser --------

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        private UserCreationRequest request;

        @BeforeEach
        void setUpRequest() {
            request = UserCreationRequest.builder()
                    .username("john")
                    .password("secret123")
                    .email("john@example.com")
                    .build();
        }

        @Test
        @DisplayName("success: encodes password, saves and publishes event")
        void success_savesAndPublishesEvent() {
            when(userRepository.existsByUsername("john")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userMapper.toUser(request)).thenReturn(user);
            when(passwordEncoder.encode("secret123")).thenReturn("encoded");
            when(roleService.findRoleById(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.createUser(request);

            assertThat(result).isEqualTo(userResponse);
            verify(passwordEncoder).encode("secret123");
            verify(userRepository).save(user);
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("username exists: throws USER_EXISTED")
        void usernameExists_throwsUserExisted() {
            when(userRepository.existsByUsername("john")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED));
        }

        @Test
        @DisplayName("blank email: throws INVALID_REQUEST")
        void blankEmail_throwsInvalidRequest() {
            request = UserCreationRequest.builder()
                    .username("john")
                    .password("secret123")
                    .email("")
                    .build();
            when(userRepository.existsByUsername("john")).thenReturn(false);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
        }

        @Test
        @DisplayName("email exists: throws USER_EXISTED")
        void emailExists_throwsUserExisted() {
            when(userRepository.existsByUsername("john")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED));
        }
    }

    // -------- findUserById --------

    @Nested
    @DisplayName("findUserById")
    class FindUserByIdTests {

        @Test
        @DisplayName("found: returns user entity")
        void found_returnsUser() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

            User result = userService.findUserById("user-1");

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("not found: throws EntityExistsException")
        void notFound_throwsEntityExistsException() {
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserById("missing")).isInstanceOf(EntityExistsException.class);
        }
    }

    // -------- findUserByUsername --------

    @Nested
    @DisplayName("findUserByUsername")
    class FindUserByUsernameTests {

        @Test
        @DisplayName("found: returns user entity")
        void found_returnsUser() {
            when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

            User result = userService.findUserByUsername("john");

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("not found: throws USER_NOT_EXISTED")
        void notFound_throwsUserNotExisted() {
            when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserByUsername("nobody"))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED));
        }
    }

    // -------- findUserByEmail --------

    @Nested
    @DisplayName("findUserByEmail")
    class FindUserByEmailTests {

        @Test
        @DisplayName("found: returns UserResponse")
        void found_returnsUserResponse() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.findUserByEmail("john@example.com");

            assertThat(result).isEqualTo(userResponse);
        }

        @Test
        @DisplayName("not found: throws USER_NOT_EXISTED")
        void notFound_throwsUserNotExisted() {
            when(userRepository.findByEmail("no@one.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserByEmail("no@one.com"))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED));
        }
    }

    // -------- updateUserProjectSize --------

    @Test
    @DisplayName("updateUserProjectSize: increments field, saves, returns response")
    void updateUserProjectSize_incrementsAndSaves() {
        user.setProjectSize(2);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUserProjectSize(user, 1);

        assertThat(user.getProjectSize()).isEqualTo(3);
        assertThat(result).isEqualTo(userResponse);
        verify(userRepository).save(user);
    }
}
