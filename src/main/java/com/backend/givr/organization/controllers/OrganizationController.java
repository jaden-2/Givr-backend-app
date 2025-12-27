package com.backend.givr.organization.controllers;

import com.backend.givr.organization.dtos.CreateOrganizationDto;
import com.backend.givr.organization.dtos.OrganizationDashboard;
import com.backend.givr.organization.dtos.ProjectRequestDto;

import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.VolunteerApplicationDto;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/organization")
public class OrganizationController {

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganizationService service;

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Valid CreateOrganizationDto createOrganizationDto){
        service.createOrganization(createOrganizationDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<OrganizationDashboard> getOrganizationDashboard(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getOrganizationDashboard(details));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getOrganizationProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(mapper.toDtos(service.getProjects(details)));
    }

    @PostMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> createProject(@RequestBody @Valid ProjectRequestDto projectRequestDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.createProject(projectRequestDto, details));
    }

    @PatchMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable("projectId") Long projectId, @RequestBody ProjectRequestDto projectRequestDto){
        return ResponseEntity.accepted().body(service.updateProject(projectId, projectRequestDto));
    }
    @GetMapping("/projects/applicants")
    public ResponseEntity<List<VolunteerApplicationDto>> getProjectApplication(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectApplications(details));
    }

    @PatchMapping("/projects/application/{id}/accept")
    public ResponseEntity<Void> acceptApplication(@AuthenticationPrincipal SecurityDetails details, @PathVariable("id") Long id){
        applicationService.changeApplicationStatus(id, ApplicationStatus.APPROVED);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/application/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Long id){
        applicationService.changeApplicationStatus(id, ApplicationStatus.REJECTED);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/{projectId}/publish")
    public ResponseEntity<Void> publicProject(@PathVariable("projectId") Long id){
        service.publishProject(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal SecurityDetails details){
        service.deleteProject(projectId, details.getId());
        return ResponseEntity.noContent().build();
    }
}
