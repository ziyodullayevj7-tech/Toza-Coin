package teamwork.view.admin;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.i18n.I18n;
import teamwork.view.layout.admin.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.service.AdminOverviewService;

@Route(value = "admin/suspiciosActivity", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminSuspiciousActivityView extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    public AdminSuspiciousActivityView(AdminOverviewService service) {
        super("Shubhali holatlar", "Review uncertain evidence carefully before making a decision.");
        Div review = panel("Manual review", "An incomplete AI review is a reason to inspect the evidence, not proof of misconduct.");
        try {
            var summary = service.load();
            Span count = new Span(Long.toString(summary.failedReviews()));
            count.addClassName("admin-policy-value");
            review.add(I18n.localize(new Paragraph("Reports with an unsuccessful AI review")), count);
        } catch (RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Could not load review priorities", exception);
            Paragraph error = I18n.localize(new Paragraph("Overview unavailable. Please try again."));
            error.getElement().setAttribute("role", "alert");
            review.add(error);
        }
        add(review, action("Moderatsiya navbati", "Open reports to compare photos, AI findings and the submitted details.",
                VaadinIcon.SEARCH, AdminModerationView.class));
    }

    @Override
    public String getPageTitle() { return I18n.t("Shubhali holatlar | TozaCoin"); }
}
