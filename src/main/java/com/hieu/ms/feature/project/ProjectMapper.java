package com.hieu.ms.feature.project;

import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hieu.ms.feature.user.UserMapper;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class})
public interface ProjectMapper {
    Project toProject(ProjectRequest request);

    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "teams", target = "teams")
    ProjectResponse toProjectResponse(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "issues", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateProject(ProjectRequest request, @MappingTarget Project entity);
}
