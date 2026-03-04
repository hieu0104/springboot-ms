package com.hieu.ms.feature.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByIssueId(String issueId);
}
