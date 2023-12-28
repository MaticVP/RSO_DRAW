package com.example.Drawer.Service.Repository;

import com.example.Drawer.Service.Entity.Project;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "project", path = "project")
public interface ProjectRepository extends CrudRepository<Project, Integer> {
    Project[] findByProjectName(@Param("projectName") String project_name);
    Project[] findByUserId(@Param("userId") Long user_id);
    void removeByProjectName(@Param("projectName") String project_name);
}