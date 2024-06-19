package com.hieu.ms.mapper;

import com.hieu.ms.dto.request.ProjectRequest;
import com.hieu.ms.dto.response.ProjectResponse;
import com.hieu.ms.entity.Project;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Oracle Corporation)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public Project toProject(ProjectRequest request) {
        if ( request == null ) {
            return null;
        }

        Project.ProjectBuilder project = Project.builder();

        project.name( request.getName() );
        project.description( request.getDescription() );
        project.category( request.getCategory() );
        List<String> list = request.getTags();
        if ( list != null ) {
            project.tags( new ArrayList<String>( list ) );
        }

        return project.build();
    }

    @Override
    public ProjectResponse toProjectResponse(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectResponse.ProjectResponseBuilder projectResponse = ProjectResponse.builder();

        projectResponse.id( project.getId() );
        projectResponse.name( project.getName() );
        projectResponse.description( project.getDescription() );
        projectResponse.category( project.getCategory() );
        List<String> list = project.getTags();
        if ( list != null ) {
            projectResponse.tags( new ArrayList<String>( list ) );
        }

        return projectResponse.build();
    }

    @Override
    public void updateProject(ProjectRequest request, Project entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setDescription( request.getDescription() );
        entity.setCategory( request.getCategory() );
        if ( entity.getTags() != null ) {
            List<String> list = request.getTags();
            if ( list != null ) {
                entity.getTags().clear();
                entity.getTags().addAll( list );
            }
            else {
                entity.setTags( null );
            }
        }
        else {
            List<String> list = request.getTags();
            if ( list != null ) {
                entity.setTags( new ArrayList<String>( list ) );
            }
        }
    }
}
