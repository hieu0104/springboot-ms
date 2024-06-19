package com.hieu.ms.mapper;

import com.hieu.ms.dto.request.ProjectRequest;
import com.hieu.ms.dto.request.ProjectUpdateRequest;
import com.hieu.ms.dto.request.UserCreationRequest;
import com.hieu.ms.dto.request.UserUpdateRequest;
import com.hieu.ms.dto.response.ProjectResponse;
import com.hieu.ms.dto.response.UserResponse;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toProject(ProjectRequest request);

    ProjectResponse toProjectResponse(Project project);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "issues", ignore = true)
    @Mapping(target = "teams", ignore = true)
    void updateProject(ProjectRequest request, @MappingTarget Project entity);
}
