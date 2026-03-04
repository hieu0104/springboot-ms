package com.hieu.ms.feature.issue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface IssueRepository extends JpaRepository<Issue, String> {
    List<Issue> findByProjectId(String id);
}
