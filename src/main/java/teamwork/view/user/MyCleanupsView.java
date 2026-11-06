package teamwork.view.user;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.user.UserLayout;

@Route(value = "dashboard/my-cleanups", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class MyCleanupsView extends Div implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo("user/my-reports", new QueryParameters(java.util.Map.of("filter", java.util.List.of("cleaned"))));
    }
}
