package com.backend.givr.organization.mappings;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import org.mapstruct.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {VolunteerMapper.class, SkillMapper.class})
public interface OrganizationMapper {

    Organization toOrganization(CreateOrganizationDto organizationDto);


    @AfterMapping
    default void updateVolunteerAndOrganization(Project project, @MappingTarget ProjectResponseDto projectDto){
        projectDto.setTotalApplicants(project.getVolunteerCount());
    }

    @Mapping(target = "id", source = "organizationId")
    @Mapping(target = "name", source = "organizationName")
    @Mapping(target = "category", source = "organizationType")
    OrganizationDto toOrganizationDto (Organization organization);

    @AfterMapping
    default void updateActiveProjectCount(Organization organization, @MappingTarget OrganizationDto organizationDto){
        organizationDto.setNumOfActiveProjects(organization.getNumOfActiveProjects());
    }

    List<OrganizationDto> toOrganizationDtoList (List<Organization> organizations);

    LocationDto toLocationDto (Location location);


    default Date toDate(String date)throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(date);
    }
    default Set<Skill> toSet(List<Skill> skills){
        return Set.copyOf(skills);
    }
}
