package com.hieu.ms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String title;
    String description;
//    @Enumerated(EnumType.STRING)
//    IssueStatus status;
    String status;
    String projectID;
    String priority;

    @Column(name = "due_date")
    LocalDateTime dueDate;

    @ElementCollection
    List<String> tags;

    @ManyToOne
    User assignee;

    @ManyToOne
    Project project;

    @JsonIgnore
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL,orphanRemoval = true)
    List<Comment> comments= new ArrayList<>();


}
