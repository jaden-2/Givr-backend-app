package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.ApplicationStats;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.MaxApplicantsReachedException;
import com.backend.givr.shared.exceptions.ProjectDeadlinePastException;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ApplicationService {
    @Autowired
    private ProjectApplicationRepo repo;
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ParticipationService participationService;

    public ProjectApplication apply(Volunteer volunteer, ProjectApplicationForm applicationForm){
        Project project = em.getReference(Project.class, applicationForm.projectId());
        if(LocalDateTime.now().isAfter(project.getDeadline().atTime(23, 59, 59)))
            throw new ProjectDeadlinePastException("Cannot apply for a project past it's application period");

        var application = new ProjectApplication(project, volunteer);
        application.setApplicationReason(application.getApplicationReason());
        application.setAvailableDays(application.getAvailableDays());
        try{
            return repo.save(application);
        }catch (DataIntegrityViolationException ignored){
            throw new DataIntegrityViolationException("Cannot apply to a project more than once");
        }
    }

    public void cancelApplication(Long projectId, Volunteer volunteer){
        Project project = em.getReference(Project.class, projectId);
        repo.deleteByProjectAndVolunteer(project, volunteer);
    }

    public void changeApplicationStatus(Long projectId, Volunteer volunteer, ApplicationStatus status){
        Project project = em.getReference(Project.class, projectId);
        ProjectApplication application = repo.findByProjectAndVolunteer(project, volunteer).orElseThrow();
        if(status == ApplicationStatus.APPLIED)
            throw new IllegalOperationException("Cannot change status to applied");
        application.setStatus(status);
        repo.save(application);
    }

    @Transactional
    public void changeApplicationStatus(Long applicationId, ApplicationStatus status){
        if(applicationId==null)
            throw new IllegalArgumentException("Null values are not accepted");

        ProjectApplication application = repo.findById(applicationId).orElseThrow();
        Project project = application.getProject();

        if(project.getApprovedList().size() >= project.getMaxVolunteers())
            throw new MaxApplicantsReachedException("Maximum applicants reached");

        if(status == ApplicationStatus.APPLIED)
            throw new IllegalOperationException("Cannot change status to applied");

        if(status == ApplicationStatus.APPROVED)
            participationService.createParticipation(project, application.getVolunteer());

        application.setStatus(status);

        repo.save(application);
    }

    private void checkNull(Project project, Volunteer volunteer){
        if(Objects.isNull(volunteer) && Objects.isNull(project))
            throw new IllegalArgumentException("Null values are not accepted");
    }

    public List<ProjectApplication> getAppliedProjects(Volunteer volunteer){
        return repo.findAllByVolunteer(volunteer);
    }

    public List<VolunteerApplicationDto> getProjectsApplications(Organization organization){
        return repo.findAllByOrganizationAndStatus(organization, ApplicationStatus.APPLIED).stream().map(VolunteerApplicationDto::new).toList();
    }

    public ApplicationStats getVolunteerStats(Organization organization){
        int approved = repo.countByStatus(ApplicationStatus.APPLIED);
        int applied = repo.countByStatus(ApplicationStatus.APPLIED);
        int rejected = repo.countByStatus(ApplicationStatus.REJECTED);

        return new ApplicationStats(applied, approved, rejected);
    }
}
