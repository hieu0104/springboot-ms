package com.hieu.ms.repository;

import com.hieu.ms.entity.Invitation;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation,String> {
Invitation findByToken(String token);
Invitation findByEmail(String userEmail);
}
