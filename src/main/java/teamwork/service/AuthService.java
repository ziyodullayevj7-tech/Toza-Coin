package teamwork.service;

import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import teamwork.config.CustomUserDetails;
import teamwork.config.PasswordEncoderConfig;
import teamwork.dto.LoginDto;
import teamwork.dto.ProfileDto;
import teamwork.entity.ProfileEntity;
import teamwork.entity.ProfileRoleEntity;
import teamwork.enums.ProfileRole;
import teamwork.enums.ProfileStatus;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.ProfileRepository;
import teamwork.repository.ProfileRoleRepository;

import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private PasswordEncoderConfig passwordEncoder;

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
        profile.setPassword(passwordEncoder.bCryptPasswordEncoder().encode(dto.getPassword()));
        profile.setCoinBalance(0);
        profile.setStreakDays(0);
        profile.setCreatedDate(LocalDateTime.now());
        profile.setStatus(ProfileStatus.ACTIVE);

        profileRepository.save(profile);

        //PROFILE ROLE ENTITY SET
        ProfileRoleEntity profileRole = new ProfileRoleEntity();
        profileRole.setProfileId(profile.getId());
        profileRole.setRoles(ProfileRole.ROLE_USER);
        profileRole.setCreatedDate(LocalDateTime.now());
        profileRoleRepository.save(profileRole);

        log.info("New user registered: username={}, name={} {}", username, profile.getName(), profile.getSurname());
    }

    public ProfileDto login(LoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails user) {
                SecurityContextHolder.getContext().setAuthentication(authentication);

                VaadinServletRequest request = (VaadinServletRequest) VaadinServletRequest.getCurrent();

                if (request != null) {
                    HttpSession session = request.getHttpServletRequest().getSession();
                    session.setAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                            SecurityContextHolder.getContext()
                    );
                }
                // SET RESPONSE DTO
                ProfileDto response = new ProfileDto();
                response.setName(user.getName());
                response.setSurname(user.getSurname());
                response.setEmail(user.getUsername());
                response.setRoles(user.getRoles().stream()
                        .map(SimpleGrantedAuthority::getAuthority)
                        .toList()
                );
                return response;
            }
        } catch (BadCredentialsException e) {
            throw new AppBadRequestException("Incorrect username or password");
        } catch (DisabledException e) {
            throw new AppBadRequestException("This user is not active");
        }
        throw new AppBadRequestException("Incorrect username or password");
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
                    ProfileRoleEntity profileRole = new ProfileRoleEntity();
                    profileRole.setProfileId(profile.getId());
                    profileRole.setRoles(ProfileRole.ROLE_USER);
                    profileRole.setCreatedDate(profile.getCreatedDate());
                    profileRoleRepository.save(profileRole);

                    return profile;
                });

    }

    public boolean updateUserBalance(String currentProfileId, Integer amount) {
        Optional<ProfileEntity> optional = profileRepository.findById(currentProfileId);

        if (optional.isEmpty()) return false;

        ProfileEntity profile = optional.get();
        profile.setCoinBalance(profile.getCoinBalance() + amount);
        profileRepository.save(profile);
        return true;
    }

    public ProfileEntity getCurrentProfile(String id) {
        return profileRepository.getReferenceById(id);
    }
}
