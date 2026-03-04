package com.hieu.ms.feature.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hieu.ms.feature.role.Role;
import com.hieu.ms.feature.role.RoleService;
import com.hieu.ms.feature.subscription.SubscriptionService;
import com.hieu.ms.feature.user.dto.UserResponse;
import com.hieu.ms.feature.user.dto.UserSearchRequest;
import com.hieu.ms.feature.user.dto.UserUpdateRequest;
import com.hieu.ms.shared.dto.response.PageResponse;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;
import com.querydsl.core.types.Predicate;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminMethodsTest {

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

    @Test
    @DisplayName("deleteUser: calls deleteById with correct userId")
    void deleteUser_CallsDeleteById() {
        // Act
        userService.deleteUser("user-1");

        // Assert
        verify(userRepository).deleteById("user-1");
    }

    @Test
    @DisplayName("getUser: found -> maps and returns UserResponse")
    void getUser_Found_ReturnsUserResponse() {
        // Arrange
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUser("user-1");

        // Assert
        assertThat(result).isEqualTo(userResponse);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    @DisplayName("getUser: not found -> throws USER_NOT_EXISTED")
    void getUser_NotFound_ThrowsUserNotExisted() {
        // Arrange
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUser("missing"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    @DisplayName("getUsers: keyword null -> returns paginated result without filtering")
    void getUsers_KeywordNull_ReturnsPaginatedResult() {
        // Arrange
        UserSearchRequest request = new UserSearchRequest();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        PageResponse<UserResponse> result = userService.getUsers(request);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(userResponse);
        verify(userRepository).findAll(any(Predicate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getUsers: keyword provided -> applies predicate and returns result")
    void getUsers_KeywordProvided_AppliesPredicate() {
        // Arrange
        UserSearchRequest request = new UserSearchRequest();
        request.setKeyword("john");
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        PageResponse<UserResponse> result = userService.getUsers(request);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAll(any(Predicate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getUsers: no results -> returns empty page")
    void getUsers_NoResults_ReturnsEmptyPage() {
        // Arrange
        UserSearchRequest request = new UserSearchRequest();
        Page<User> emptyPage = Page.empty();

        when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PageResponse<UserResponse> result = userService.getUsers(request);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateUser: happy path -> encodes password, sets roles and saves")
    void updateUser_HappyPath_UpdatesUser() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .password("newpass123")
                .firstName("Jane")
                .roles(List.of("USER"))
                .build();

        Role role = Role.builder().name("USER").build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded-pass");
        when(roleService.findRolesByIds(List.of("USER"))).thenReturn(List.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.updateUser("user-1", request);

        // Assert
        assertThat(result).isEqualTo(userResponse);
        verify(passwordEncoder).encode("newpass123");
        assertThat(user.getPassword()).isEqualTo("encoded-pass");
        assertThat(user.getRoles()).containsExactly(role);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: user not found -> throws USER_NOT_EXISTED")
    void updateUser_NotFound_ThrowsUserNotExisted() {
        // Arrange
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser("missing", new UserUpdateRequest()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser: roles list null -> sets empty roles")
    void updateUser_RolesNull_SetsEmptyRoles() {
        // Arrange
        UserUpdateRequest request =
                UserUpdateRequest.builder().password("pass").roles(null).build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(roleService.findRolesByIds(null)).thenReturn(List.of());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        userService.updateUser("user-1", request);

        // Assert
        assertThat(user.getRoles()).isEmpty();
    }
}
