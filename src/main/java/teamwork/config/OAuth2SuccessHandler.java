package teamwork.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import teamwork.entity.ProfileEntity;
import teamwork.service.AuthService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // ATTRIBUTES THAT COME FROM GOOGLE
        String phone =  oAuth2User.getAttribute("phone");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String surname = oAuth2User.getAttribute("surname");

        if (email == null || email.isBlank()) {
            log.error("Google OAuth2: email attribute came null or blank");
            response.sendRedirect("/login?error=oauth2");
            return;
        }

        log.info("Google OAuth2 login: email={}, name={}, surname={}", email, name, surname);

        //NEW OR EXISTING FROM DB
        ProfileEntity profile = authService.findOrCreateGoogleUser(phone, email, name, surname);

        //BRING ROLES
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // VAADIN SESSION
        request.getSession().setAttribute("GOOGLE_EMAIL", profile.getUsername());
        request.getSession().setAttribute("GOOGLE_NAME", profile.getName());
        request.getSession().setAttribute("GOOGLE_SURNAME", profile.getSurname());
        request.getSession().setAttribute("GOOGLE_ROLE", roles);

        response.sendRedirect("/dashboard");
    }
}
