package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.organization.repo.OrganizationRepo;
import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.VolunteerApplicationDto;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationMapper mapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private OrganizationRepo repo;
    @Autowired
    private OrganizationDetailsService service;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private LocationService locationService;

    @Autowired
    private EntityManager em;

    @Transactional
    public Organization createOrganization(CreateOrganizationDto organizationDto){
        if(organizationDto == null){
            throw new IllegalArgumentException("Organization DTO cannot be null");
        }
        Organization organization = mapper.toOrganization(organizationDto);
        organization.setLocation(locationService.createLocation(organization.getLocation()));
        organization.setStatus(VerificationStatus.UNVERIFIED);
        try{
            var savedOrganization = repo.save(organization);
            OrganizationDetails details = new OrganizationDetails(organizationDto.getEmail(), encoder.encode(organizationDto.getPassword()), savedOrganization);
            service.save(details);
            return savedOrganization;
        }catch (DataIntegrityViolationException ignored){
            throw new DuplicateAccountException("An organization with the same cac registration number or email already exists ");
        }
    }

    @Transactional
    public List<ProjectResponseDto> createProject(ProjectRequestDto projectRequestDto, SecurityDetails details){
        if(projectRequestDto == null)
            throw new IllegalArgumentException("Null project DTO null accepted");

        var organization = repo.findById(details.getId());
        if(organization.isEmpty())
            throw new EntityNotFoundException("Failed to fetch organization");
        Organization org = organization.get();

        Project project = projectService.createProject(projectRequestDto, org);

        //        org.addProject(project);
        //        repo.save(org);

        return projectMapper.toDtos(projectService.getOrganizationProjects(org));
    }

    public void approveApplication(Long applicationId){
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.APPROVED);
    }

    public void rejectApplication(Long applicationId){
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.REJECTED);
    }

    public List<VolunteerApplicationDto> getProjectApplications (SecurityDetails details){
        Organization organization = repo.findById(details.getId()).orElseThrow();
        return applicationService.getProjectsApplications(organization);
    }

    public void publishProject(Long projectId){
        Project project = projectService.findProjectById(projectId);
        project.setStatus(ProjectStatus.OPEN);
        projectService.save(project);
    }

    public OrganizationDashboard getOrganizationDashboard(SecurityDetails details){
        var organization = repo.findById(details.getId()).orElseThrow(()-> new EntityNotFoundException(String.format("Organization withID %s does not exist", details.getId())));

        Map<String, List<ProjectResponseDto>> projectDtoMap = new HashMap<>();

        projectDtoMap.put("draftProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.DRAFT)
                        .toList()
        ));

        projectDtoMap.put("openProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.OPEN)
                        .sorted(Comparator.comparing(Project::getCreatedAt))
                        .toList()
        ));


        projectDtoMap.put("ongoingProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.ONGOING)
                        .sorted(Comparator.comparing(Project::getCreatedAt))
                        .toList()
        ));

        projectDtoMap.put("completedProjects", projectMapper.toDtos(organization.getProjects()
                .stream()
                .filter(project -> project.getStatus() == ProjectStatus.COMPLETED)
                .sorted(Comparator.comparing(Project::getCreatedAt))
                .toList()));

        ApplicationStats stats = applicationService.getVolunteerStats(organization);
        return new OrganizationDashboard(organization.getOrganizationName(), projectDtoMap, 5.0, stats);
    }

    public List<Project> getProjects(SecurityDetails details){
        return projectService.getOrganizationProjects(em.getReference(Organization.class, details.getId()));
    }

    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto projectRequestDto) {
        return projectMapper.toProjectDto(projectService.updateProject(projectId, projectRequestDto));
    }
    public List<OrganizationDto> getOrganizations() {
        return mapper.toOrganizationDtoList(repo.findAll());
    }

    public void deleteProject(Long projectId, String organizationId) {
        Organization organization = em.getReference(Organization.class, organizationId);
        projectService.deleteProject(projectId, organization);
    }
}
