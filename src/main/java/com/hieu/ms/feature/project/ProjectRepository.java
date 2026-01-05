package com.hieu.ms.feature.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hieu.ms.feature.user.User;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByOwner(User user);

    List<Project> findByNameContainingAndTeamsContaining(String partialName, User user);

    //    @Query("SELECT P from Project p join p.teams t where t=:user")
    //    List <Project> findProjectByTeams(@Param("user") User user);

    List<Project> findByTeamsContainingOrOwner(User user, User owner);
}
