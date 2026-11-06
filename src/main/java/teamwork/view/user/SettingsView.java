package teamwork.view.user;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/settings", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class SettingsView extends Div implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(ProfileView.class);
    }
}
