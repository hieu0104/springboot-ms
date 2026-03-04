package com.hieu.ms.feature.attachment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByIssueId(String issueId);

    List<Attachment> findByProjectId(String projectId);

    List<Attachment> findByUploadedById(String userId);

    @Query("SELECT a FROM Attachment a WHERE a.issue.id = :issueId ORDER BY a.createdAt DESC")
    List<Attachment> findByIssueIdOrderByCreatedAtDesc(@Param("issueId") String issueId);

    @Query("SELECT a FROM Attachment a WHERE a.project.id = :projectId ORDER BY a.createdAt DESC")
    List<Attachment> findByProjectIdOrderByCreatedAtDesc(@Param("projectId") String projectId);

    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.uploadedBy.id = :userId")
    Long getTotalFileSizeByUser(@Param("userId") String userId);

    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.project.id = :projectId")
    Long getTotalFileSizeByProject(@Param("projectId") String projectId);

    long countByIssueId(String issueId);

    long countByProjectId(String projectId);
}
