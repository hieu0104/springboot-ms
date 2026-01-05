package com.hieu.ms.feature.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.issue.Issue;
import com.hieu.ms.feature.role.Role;
import com.hieu.ms.feature.subscription.Subscription;
import com.hieu.ms.shared.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        indexes = {
            @Index(name = "idx_user_username", columnList = "username"),
            @Index(name = "idx_user_email", columnList = "email")
        })
public class User extends BaseEntity {
    // ID field removed as it is inherited from BaseEntity

    String username;
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
    String email;

    @ManyToMany
    Set<Role> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL)
    List<Issue> assignedIssues = new ArrayList<>();

    int projectSize;

    @JsonIgnore
    @OneToOne
    Subscription subscription;
}
