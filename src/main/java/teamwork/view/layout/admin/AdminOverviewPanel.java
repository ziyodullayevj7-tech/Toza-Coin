package teamwork.view.layout.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.slf4j.LoggerFactory;
import teamwork.i18n.I18n;
import teamwork.service.AdminOverviewService;

/** Live counts with explicit load failure and an accessible status distribution. */
public class AdminOverviewPanel extends Div {
    public AdminOverviewPanel(AdminOverviewService service) {
        addClassName("admin-overview");
        render(service);
    }

    private void render(AdminOverviewService service) {
        removeAll();
        Button refresh = I18n.localize(new Button("Yangilash", VaadinIcon.REFRESH.create(),
                event -> render(service)));
        refresh.setDisableOnClick(true);
        refresh.addClassName("admin-overview-refresh");
        try {
            var summary = service.load();
            Div metrics = new Div(metric("Foydalanuvchilar", summary.accounts(), VaadinIcon.USERS),
                    metric("Xabarlar", summary.totalReports(), VaadinIcon.FILE_TEXT),
                    metric("Pending reports", summary.pending(), VaadinIcon.CLOCK),
                    metric("Resolved reports", summary.resolved(), VaadinIcon.CHECK_CIRCLE));
            metrics.addClassName("admin-metric-grid");
            Div distribution = new Div();
            distribution.addClassName("admin-status-distribution");
            distribution.add(status("Pending reports", summary.pending(), summary.totalReports(), "pending"),
                    status("Reports in progress", summary.inProgress(), summary.totalReports(), "progress"),
                    status("Resolved reports", summary.resolved(), summary.totalReports(), "resolved"),
                    status("Rejected reports", summary.rejected(), summary.totalReports(), "rejected"));
            add(metrics, AdminPage.panel("Report overview", "Current report status across the platform.", distribution), refresh);
        } catch (RuntimeException exception) {
            LoggerFactory.getLogger(getClass()).error("Could not load admin overview", exception);
            Div error = new Div(I18n.localize(new Paragraph("Overview unavailable. Please try again.")), refresh);
            error.addClassName("admin-load-error");
            error.getElement().setAttribute("role", "alert");
            add(error);
        }
    }

    private Div metric(String label, long value, VaadinIcon icon) {
        Div badge = new Div(icon.create());
        badge.addClassName("admin-metric-icon");
        badge.getElement().setAttribute("aria-hidden", "true");
        Span number = new Span(Long.toString(value));
        number.addClassName("admin-metric-number");
        Div metric = new Div(badge, I18n.localize(new Span(label)), number);
        metric.addClassName("admin-metric-card");
        return metric;
    }

    private Div status(String label, long count, long total, String state) {
        Div heading = new Div(I18n.localize(new Span(label)), new Span(Long.toString(count)));
        heading.addClassName("admin-status-heading");
        ProgressBar progress = new ProgressBar(0, 1, total == 0 ? 0 : (double) count / total);
        I18n.attribute(progress, "aria-label", label);
        Div row = new Div(heading, progress);
        row.addClassNames("admin-status-row", "admin-status-" + state);
        return row;
    }
}
