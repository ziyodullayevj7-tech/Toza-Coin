package teamwork.util;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.view.general.home.LoginView;
import teamwork.view.user.UserDashboard;

/** Layout navigation checks evaluated against the current request's authenticated user. */
public final class NavigationGuard {
    private NavigationGuard() {
    }

    public static boolean requireLogin(BeforeEnterEvent event) {
        if (event.getNavigationTarget().isAnnotationPresent(AnonymousAllowed.class)) {
            return true;
        }
        if (SecurityUtils.getCurrentUser().isEmpty()) {
            event.forwardTo(LoginView.class);
            return false;
        }
        return true;
    }

    public static boolean requireAdmin(BeforeEnterEvent event) {
        var user = SecurityUtils.getCurrentUser();
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return false;
        }
        var authorities = user.get().getAuthorities();
        boolean admin = authorities != null && authorities.stream().anyMatch(authority -> authority != null
                && ("ROLE_ADMIN".equals(authority.getAuthority()) || "ADMIN".equals(authority.getAuthority())));
        if (!admin) {
            event.forwardTo(UserDashboard.class);
            return false;
        }
        return true;
    }
}
