package com.hieu.ms.repository;

import com.hieu.ms.entity.Comment;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,String> {
    List<Comment> findByIssueId(String issueId);

}
