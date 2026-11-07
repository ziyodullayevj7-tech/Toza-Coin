package teamwork.TozaCoin;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import teamwork.config.CustomUserDetails;
import teamwork.entity.ProfileEntity;
import teamwork.util.NavigationGuard;
import teamwork.view.general.home.LoginView;
import teamwork.view.user.UserDashboard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NavigationGuardTests {
    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousUserIsForwardedToLogin() {
        BeforeEnterEvent event = userNavigation();
        assertFalse(NavigationGuard.requireLogin(event));
        verify(event).forwardTo(LoginView.class);
    }

    @Test
    void anonymousAdminVisitorIsForwardedToLogin() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        assertFalse(NavigationGuard.requireAdmin(event));
        verify(event).forwardTo(LoginView.class);
    }

    @Test
    void springAnonymousTokenIsNotTreatedAsALoggedInUser() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "test", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        BeforeEnterEvent event = userNavigation();
        assertFalse(NavigationGuard.requireLogin(event));
        verify(event).forwardTo(LoginView.class);
    }

    @Test
    void explicitlyPublicPageRemainsAccessible() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        doReturn(PublicPage.class).when(event).getNavigationTarget();
        assertTrue(NavigationGuard.requireLogin(event));
        verify(event, never()).forwardTo(LoginView.class);
    }

    @Test
    void authenticatedUserCanEnterUserPagesButNotAdminPages() {
        authenticate("ROLE_USER");
        assertTrue(NavigationGuard.requireLogin(userNavigation()));
        BeforeEnterEvent adminEvent = mock(BeforeEnterEvent.class);
        assertFalse(NavigationGuard.requireAdmin(adminEvent));
        verify(adminEvent).forwardTo(UserDashboard.class);
    }

    @Test
    void administratorCanEnterBothLayouts() {
        authenticate("ROLE_ADMIN");
        assertTrue(NavigationGuard.requireLogin(userNavigation()));
        BeforeEnterEvent adminEvent = mock(BeforeEnterEvent.class);
        assertTrue(NavigationGuard.requireAdmin(adminEvent));
        verify(adminEvent, never()).forwardTo(LoginView.class);
        verify(adminEvent, never()).forwardTo(UserDashboard.class);
    }

    @Test
    void unprefixedAdminAuthorityIsAlsoAccepted() {
        authenticate("ADMIN");
        assertTrue(NavigationGuard.requireAdmin(mock(BeforeEnterEvent.class)));
    }

    private BeforeEnterEvent userNavigation() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        doReturn(UserDashboard.class).when(event).getNavigationTarget();
        return event;
    }

    private void authenticate(String role) {
        ProfileEntity profile = new ProfileEntity();
        profile.setId("navigation-test");
        CustomUserDetails user = new CustomUserDetails(profile);
        user.setRoles(List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    @AnonymousAllowed
    private static class PublicPage extends Div {
    }
}
