package com.backend.givr.volunteer.mappings;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.organization.dtos.ProjectApplicationDto;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.UpdateVolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.entity.Volunteer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SkillMapper.class})
public interface VolunteerMapper {

    @Mapping(source = "phone", target = "phoneNumber")
    Volunteer toVolunteer(CreateVolunteerRequestDto dto);
    void updateVolunteer(UpdateVolunteerDto updatedVolunteer, @MappingTarget Volunteer volunteer);

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "id", source = "volunteerId")
    VolunteerProfile toProfile(Volunteer volunteer);

    @AfterMapping
    default  void updateProfile(Volunteer volunteer, @MappingTarget VolunteerProfile profile){
        profile.setSkills(volunteer.getSkills().stream().map(Skill::getName).toList());
    }

    VolunteerDto toVolunteerDto (Volunteer volunteer);
    Location toLocation (LocationDto locationDto);

}
