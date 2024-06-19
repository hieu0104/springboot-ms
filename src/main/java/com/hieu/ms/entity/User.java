package com.hieu.ms.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
    String email;
    @ManyToMany
    Set<Role> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "assignee",cascade = CascadeType.ALL)
    List<Issue> assignedIssues= new ArrayList<>();

    int projectSize;

    @OneToOne
    Subscription subscription;

}
