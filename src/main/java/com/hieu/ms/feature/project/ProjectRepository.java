package com.hieu.ms.feature.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hieu.ms.feature.user.User;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByOwner(User user);

    List<Project> findByNameContainingAndTeamsContaining(String partialName, User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.teams t WHERE t = :user OR p.owner = :owner")
    List<Project> findByTeamsContainingOrOwner(@Param("user") User user, @Param("owner") User owner);

    @Query(
            "SELECT DISTINCT p FROM Project p LEFT JOIN p.teams t WHERE (t = :user OR p.owner = :user) AND (:category IS NULL OR p.category = :category)")
    List<Project> findByTeamMemberOrOwnerAndCategory(@Param("user") User user, @Param("category") String category);
}
