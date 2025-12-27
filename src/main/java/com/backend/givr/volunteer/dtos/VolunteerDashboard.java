package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.ProjectApplicationDto;
import com.backend.givr.organization.entity.ProjectApplication;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.RequiredArgsConstructor;

import java.util.List;

public record VolunteerDashboard(
        String firstname,
        List<ProjectApplicationDto> projectApplications
) {

}
