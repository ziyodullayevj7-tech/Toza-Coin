package teamwork.TozaCoin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminLoginTests {
    @Autowired AuthenticationManager authenticationManager;

    @Test
    void initialAdminsAuthenticateWithTheirConfiguredPasswordAndAdminRole() {
        for (String email : java.util.List.of("tzadmin@gmail.com", "tzadmin2711@gmail.com")) {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, "Project_Hackathon_23412_"));
            assertTrue(authentication.isAuthenticated());
            assertTrue(authentication.getAuthorities().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority())));
        }
    }
    @Test
    void testUsersAuthenticateWithoutAdminPrivileges() {
        for (int i = 1; i <= 5; i++) {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("user" + i + "@gmail.com", "Project_Hackathon_23412_"));
            assertTrue(authentication.isAuthenticated());
            assertTrue(authentication.getAuthorities().stream().anyMatch(role -> "ROLE_USER".equals(role.getAuthority())));
            assertFalse(authentication.getAuthorities().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority())));
        }
    }

}
