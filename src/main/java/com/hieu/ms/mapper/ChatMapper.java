//package com.hieu.ms.mapper;
//
//import com.hieu.ms.dto.request.ChatRequest;
//import com.hieu.ms.dto.request.ProjectRequest;
//import com.hieu.ms.dto.response.ChatResponse;
//import com.hieu.ms.dto.response.ProjectResponse;
//import com.hieu.ms.entity.Chat;
//import com.hieu.ms.entity.Project;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//
//@Mapper(componentModel = "spring")
//public interface ChatMapper {
//
//
//
//        @Mapping(source = "project.id", target = "projectId")
//        @Mapping(source = "users", target = "userIds")
//        ChatResponse toChatResponse (Chat chat);
//
//        @Mapping(source = "name", target = "name")
//        @Mapping(source = "projectId", target = "project.id")
//        Chat toChat(ChatRequest chatRequest);
//
//        void updateChatFromDTO(ChatRequest chatRequest, @MappingTarget Chat chat);
//    }
