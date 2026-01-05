package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.otp.OtpDto;
import com.backend.givr.shared.service.LogoutService;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.UpdateVolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerDashboard;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/volunteer")
public class VolunteerController {
    @Autowired
    private VolunteerService service;
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private ProjectService projectService;
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Validated CreateVolunteerRequestDto payload){
        var createdVolunteer = service.createAccount(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<VolunteerDashboard> getVolunteerDashboard(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        return ResponseEntity.ok(service.getVolunteerDashboard(volunteerDetails.getId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<VolunteerProfile> getVolunteerProfile(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        var profile = service.getVolunteerProfile(volunteerDetails.getId());
        profile.setEmail(volunteerDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/profile")
    public ResponseEntity<VolunteerProfile> updateVolunteerProfile(@AuthenticationPrincipal SecurityDetails details, @RequestBody UpdateVolunteerDto profile){
        return ResponseEntity.ok(service.updateProfile(details.getId(), profile, details));
    }

    @PostMapping("/projects/apply")
    public ResponseEntity<Void> applyForProject(@AuthenticationPrincipal VolunteerDetails volunteerDetails, @RequestBody @Valid ProjectApplicationForm applicationForm){
        service.apply(volunteerDetails.getId(), applicationForm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getAvailableProjects(){
        return ResponseEntity.ok(projectService.getAllProjectsForVolunteer());
    }

    @GetMapping("/projects/recommended")
    public ResponseEntity<List<ProjectResponseDto>> getRecommendedProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getRecommendedProjects(details));
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationDto>> getOrganizationDtoResponseEntity(){
        return ResponseEntity.ok(organizationService.getOrganizations());
    }

    @GetMapping("/volunteering")
    public ResponseEntity<List<ParticipationDto>> getMyVolunteering(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getMyVolunteering(details));
    }

    @DeleteMapping("/volunteering/{participationId}")
    public ResponseEntity<Void> rejectParticipation(@PathVariable("participationId") Long participationId, @AuthenticationPrincipal SecurityDetails details){
        service.rejectParticipation(participationId, details.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify/email/otp")
    public ResponseEntity<Void> verifyEmail(@AuthenticationPrincipal SecurityDetails details){
        service.requestOtp( details.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/verify/email")
    public ResponseEntity<Void> confirmEmail(@RequestBody @Valid OtpDto otpDto, @AuthenticationPrincipal SecurityDetails details){
        service.confirmEmail(details, otpDto.otp());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal SecurityDetails authUser) {
        return ResponseEntity.ok().headers(logoutService.logout(authUser)).build();
    }
}
