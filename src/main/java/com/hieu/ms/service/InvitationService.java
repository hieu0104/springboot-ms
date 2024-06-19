package com.hieu.ms.service;

import com.hieu.ms.dto.request.InviteRequest;

import com.hieu.ms.dto.response.UserResponse;
import com.hieu.ms.entity.Invitation;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import com.hieu.ms.mapper.UserMapper;
import com.hieu.ms.repository.InvitationRepository;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InvitationService {
    EmailService emailService;
    InvitationRepository invitationRepository;
    AuthenticationService authenticationService;
    UserService userService;
    UserMapper userMapper;
    ProjectService projectService;

    public void sendInvitation(Authentication authenticationUser,
                               InviteRequest inviteRequest) throws MessagingException {
        User user = authenticationService.getAuthenticatedUser(authenticationUser);
        String invitationToken = UUID.randomUUID().toString();
        Invitation invitation = Invitation.builder()
                .email(inviteRequest.getEmail())
                .projectId(inviteRequest.getProjectId())
                .token(invitationToken)
                .build();
        invitationRepository.save(invitation);

        String invitationLink = "http//localhost:8080/accept_invitation?token" + invitationToken;
        log.info("invitation token: {}",invitationToken);
        emailService.sendMailWithToken(inviteRequest.getEmail(), invitationLink);
    }
    public Invitation acceptInvitation(String token, Authentication connectedUser) throws Exception {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        Invitation invitation= invitationRepository.findByToken(token);
        if (invitation==null){
            throw new Exception("Invalid invitation token");

        }
        // Optional<User> exitstingUser = userRepository.findByEmail(invitation.getEmail());

        UserResponse existingUser = userService.findUserByEmail(invitation.getEmail());
        //userMapper.toUser(existingUser);
        Project project = projectService.getProjectById(invitation.getProjectId());
        if (existingUser != null && project != null) {
            projectService.addUserToProject(user.getId(),project.getId());
//            Project project1 = Project.builder()
//
//                    .build();
        }
//        if (!userProjectRepository.existsByUserAndProject(user, project)) {
//            // Create association between user and project
//            UserProject userProject = new UserProject();
//            userProject.setUser(user);
//            userProject.setProject(project);
//            userProjectRepository.save(userProject);
//        }


        return invitation;
    }
    public String getTokenByUserMail(String userEmail){
        Invitation invitation = invitationRepository.findByEmail(userEmail);
        return invitation.getToken();
    }

    public void deleteToken(String token){
        Invitation invitation = invitationRepository.findByToken(token);

        invitationRepository.delete(invitation);
    }


}
