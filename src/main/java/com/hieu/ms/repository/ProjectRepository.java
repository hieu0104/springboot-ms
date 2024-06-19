package com.hieu.ms.repository;

import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ProjectRepository extends JpaRepository<Project,String> {
    List<Project> findByOwner(User user);
    List <Project> findByNameContainingAndTeamsContaining(String partialName,User user);

//    @Query("SELECT P from Project p join p.teams t where t=:user")
//    List <Project> findProjectByTeams(@Param("user") User user);

    List <Project> findByTeamsContainingOrOwner(User user, User owner);
}
