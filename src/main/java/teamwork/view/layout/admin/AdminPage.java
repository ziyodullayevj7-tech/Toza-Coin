package teamwork.view.layout.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import teamwork.i18n.I18n;

/** Shared page structure for admin destinations, using the user area's visual language. */
public class AdminPage extends VerticalLayout {
    protected AdminPage(String title, String description) {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("admin-content-page");
        Div heading = new Div(I18n.localize(new Span("Admin panel")),
                I18n.localize(new H1(title)), I18n.localize(new Paragraph(description)));
        heading.addClassName("admin-page-heading");
        add(heading);
    }

    public static Div panel(String title, String description, Component... children) {
        Div panel = new Div(I18n.localize(new H2(title)), I18n.localize(new Paragraph(description)));
        panel.addClassName("admin-glass-panel");
        panel.add(children);
        return panel;
    }

    public static RouterLink action(String title, String description, VaadinIcon icon,
                                    Class<? extends Component> destination) {
        RouterLink link = new RouterLink();
        link.setRoute(destination);
        link.addClassName("admin-action-card");
        Div badge = new Div(icon.create());
        badge.addClassName("admin-action-icon");
        badge.getElement().setAttribute("aria-hidden", "true");
        Span arrow = new Span("↗");
        arrow.addClassName("admin-action-arrow");
        arrow.getElement().setAttribute("aria-hidden", "true");
        link.add(badge, arrow, I18n.localize(new H2(title)), I18n.localize(new Paragraph(description)));
        return link;
    }
}
