package teamwork.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import teamwork.dto.ProfileDto;
import teamwork.entity.ProfileEntity;
import teamwork.entity.ProfileRoleEntity;
import teamwork.enums.ProfileRole;
import teamwork.enums.ProfileStatus;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.ProfileRepository;
import teamwork.repository.ProfileRoleRepository;

import java.time.LocalDateTime;


@Slf4j
@Service
public class AuthService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void register(ProfileDto dto) {
        // VALIDATION
        validateNotBlank(dto.getName(), "Name is required");
        validateNotBlank(dto.getSurname(), "Surname is required");
        validateNotBlank(dto.getEmail(), "Email is required");
        validateNotBlank(dto.getPhone(), "Phone is required");
        validateNotBlank(dto.getPassword(), "Password is required");

        String username = dto.getEmail().toLowerCase();
        if (profileRepository.existsByUsername(username)) {
            throw new AppBadRequestException("Username is already in use");
        }

        //PROFILE ENTITY SET
        ProfileEntity profile = new ProfileEntity();
        profile.setName(dto.getName());
        profile.setSurname(dto.getSurname());
        profile.setPhoneNumber(String.valueOf(dto.getPhone()));
        profile.setUsername(username);
        profile.setPassword(new BCryptPasswordEncoder().encode(dto.getPassword()));
        profile.setCreatedDate(LocalDateTime.now());
        profile.setStatus(ProfileStatus.ACTIVE);

        profileRepository.save(profile);

        //PROFILE ROLE ENTITY SET
        ProfileRoleEntity  profileRole = new ProfileRoleEntity();
        profileRole.setProfileId(profile.getId());
        profileRole.setRoles(ProfileRole.ROLE_USER);
        profileRole.setCreatedDate(LocalDateTime.now());
        profileRoleRepository.save(profileRole);

        log.info("New user registered: username={}, name={} {}", username, profile.getName(), profile.getSurname());
    }

    private void validateNotBlank(String value, String errorMessage) {
        if (value == null || value.trim().length() == 0) {
            throw new AppBadRequestException(errorMessage);
        }
    }

    public ProfileEntity findOrCreateGoogleUser(String phoneNumber, String email, String name, String surname) {
        String emailLowerCase = email.toLowerCase();

        return profileRepository.findByUsername(emailLowerCase)
                .orElseGet(() -> {
                    log.info("Creating new user from Google OAuth2: email={}", email);

                    ProfileEntity profile = new ProfileEntity();
                    profile.setName(name);
                    profile.setSurname(surname);
                    profile.setPhoneNumber(phoneNumber);
                    profile.setUsername(emailLowerCase);
                    profile.setPassword("");
                    profile.setCreatedDate(LocalDateTime.now());
                    profile.setStatus(ProfileStatus.ACTIVE);

                    profileRepository.save(profile);

                    //PROFILE ROLE ENTITY SET
                    ProfileRoleEntity  profileRole = new ProfileRoleEntity();
                    profileRole.setProfileId(profile.getId());
                    profileRole.setRoles(ProfileRole.ROLE_USER);
                    profileRole.setCreatedDate(profile.getCreatedDate());
                    profileRoleRepository.save(profileRole);

                    return  profile;
                });

    }
}
