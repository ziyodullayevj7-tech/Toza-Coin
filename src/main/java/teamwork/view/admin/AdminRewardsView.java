package teamwork.view.admin;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.i18n.I18n;
import teamwork.view.layout.admin.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.service.AdminOverviewService;

@Route(value = "admin/rewards", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminRewardsView extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    public AdminRewardsView() {
        super("Mukofotlar", "Recognize verified reports and completed cleanups.");
        Div policies = new Div(policy("Xabar berish", "15", "A report photo is verified before the reward is granted.", VaadinIcon.MAP_MARKER),
                policy("Tozalash", "50", "Before and after photos are reviewed to verify a cleanup.", VaadinIcon.RECYCLE));
        policies.addClassName("admin-policy-grid");
        add(policies, panel("AI tasdiqlasa beriladi", "Rewards are credited after AI approval. These are the current reward rules.",
                action("Moderatsiya navbati", "Review submitted evidence and report status.", VaadinIcon.CHECK_CIRCLE, AdminModerationView.class)));
    }

    private Div policy(String title, String amount, String description, VaadinIcon icon) {
        Span value = I18n.localize(new Span(amount + " tanga"));
        value.addClassName("admin-policy-value");
        Div card = panel(title, description, value);
        card.addComponentAsFirst(icon.create());
        card.addClassName("admin-policy-card");
        return card;
    }

    @Override
    public String getPageTitle() { return I18n.t("Mukofotlar | TozaCoin"); }
}
