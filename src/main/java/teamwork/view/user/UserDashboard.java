package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import teamwork.util.SecurityUtils;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/dashboard", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class UserDashboard extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {
    public UserDashboard() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("eco-dashboard");
        String name = SecurityUtils.getCurrentUser().map(u -> u.getName()).filter(n -> n != null && !n.isBlank()).orElse("do'stim");
        Span label = I18n.localize(new Span("BIRGALIKDA TOZAROQ"));
        label.addClassName("eco-eyebrow");
        Div welcome = I18n.localize(new Div(label, I18n.localize(new H1("Salom, " + name + "!")),
                I18n.localize(new Paragraph("Bugun atrofingizdagi muhit uchun bir yaxshi ish qiling."))));
        welcome.addClassName("eco-welcome");
        Div actions = I18n.localize(new Div(
                action(VaadinIcon.CAMERA, "Report", "Chiqindi bor joyni suratga oling va xabar bering.", ReportWasteView.class, "eco-action-primary"),
                action(VaadinIcon.MAP_MARKER, "Xaritani ochish", "Tozalash uchun berilgan joylarni toping.", MapView.class, ""),
                action(VaadinIcon.CLIPBOARD_CHECK, "Mening ishlarim", "Xabarlaringiz va tozalashlaringizni bir joyda kuzating.", MyReportsView.class, "")));
        actions.addClassName("eco-dashboard-actions");
        Div note = I18n.localize(new Div(I18n.localize(new H2("Birinchi marta foydalanayapsizmi?")),
                I18n.localize(new Paragraph("Report tugmasini bosing, suratga oling va joylashuvni belgilang. Keyin o'zingiz tozalashni yoki boshqalarga yuborishni tanlang."))));
        note.addClassName("eco-dashboard-note");
        add(welcome, actions, note);
    }

    private RouterLink action(VaadinIcon icon, String title, String text,
                              Class<? extends com.vaadin.flow.component.Component> target, String extraClass) {
        RouterLink link = I18n.localize(new RouterLink());
        link.setRoute(target);
        link.addClassName("eco-action-card");
        if (!extraClass.isEmpty()) link.addClassName(extraClass);
        Div symbol = I18n.localize(new Div(icon.create()));
        symbol.addClassName("eco-action-icon");
        Span arrow = I18n.localize(new Span("↗"));
        arrow.addClassName("eco-action-arrow");
        link.add(symbol, I18n.localize(new H2(title)), I18n.localize(new Paragraph(text)), arrow);
        return link;
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Boshqaruv paneli | TozaCoin");
    }
}
