package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {
    List<Project> findAllByOrganizationOrderByCreatedAtAsc(Organization organization);

    Optional<Project> findByProjectIdAndOrganization(Long projectId, Organization organization);

    List<Project> findAllByStatus(ProjectStatus projectStatus);

}
