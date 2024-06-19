package com.hieu.ms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String token;
    String email;
    @Column(name = "project_id")
    String projectId;
}
