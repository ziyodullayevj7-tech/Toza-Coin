package teamwork.view.layout.admin;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.layout.user.JungleBackground;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import teamwork.view.admin.AdminDashboard;
import teamwork.view.admin.AdminModerationView;
import teamwork.view.admin.AdminRewardsView;
import teamwork.view.admin.AdminSettingsView;
import teamwork.view.admin.AdminStatisticsView;
import teamwork.view.admin.AdminSuspiciousActivityView;
import teamwork.view.admin.AdminUsersView;
import teamwork.util.NavigationGuard;

import java.util.LinkedHashMap;
import java.util.Map;

@CssImport("./themes/tozacoin/user-layout.css")
@CssImport("./themes/tozacoin/trashout.css")
@CssImport("./themes/tozacoin/user-jungle.css")
@CssImport("./themes/tozacoin/admin-jungle.css")
public class AdminLayout extends AppLayout implements BeforeEnterObserver {

    private final AdminHeader header = new AdminHeader();
    private final Map<Class<? extends Component>, RouterLink> navigationItems = new LinkedHashMap<>();
    private final Map<Class<? extends Component>, String> pageTitles = new LinkedHashMap<>();

    public AdminLayout() {
        addClassNames("admin-layout-container", "admin-jungle-layout");
        addToNavbar(header);
        addToDrawer(buildSidebar());
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!NavigationGuard.requireAdmin(event)) {
            return;
        }
        Class<?> target = event.getNavigationTarget();
        navigationItems.forEach((view, item) -> {
            if (view.equals(target)) {
                item.addClassName("active");
                item.getElement().setAttribute("aria-current", "page");
                header.setPageTitle(pageTitles.get(view));
            } else {
                item.removeClassName("active");
                item.getElement().removeAttribute("aria-current");
            }
        });
    }

    private Div buildSidebar() {
        Div sidebar = I18n.localize(new Div());
        sidebar.addClassName("admin-sidebar");

        Div brand = I18n.localize(new Div());
        brand.addClassName("admin-brand");
        Div logo = I18n.localize(new Div(I18n.localize(new Span("TC"))));
        logo.addClassName("admin-brand-logo");
        Div brandText = I18n.localize(new Div());
        brandText.addClassName("admin-brand-text");
        Span name = I18n.localize(new Span("TozaCoin"));
        name.addClassName("admin-brand-name");
        Span role = I18n.localize(new Span("Admin panel"));
        role.addClassName("admin-brand-role");
        brandText.add(name, role);
        brand.add(logo, brandText);

        Div navigation = I18n.localize(new Div());
        navigation.addClassName("admin-navigation");
        navigation.add(
                createNavigationItem("Boshqaruv paneli", VaadinIcon.DASHBOARD, AdminDashboard.class),
                createNavigationItem("Moderatsiya navbati", VaadinIcon.CLOCK, AdminModerationView.class),
                createNavigationItem("Shubhali holatlar", VaadinIcon.WARNING, AdminSuspiciousActivityView.class),
                createNavigationItem("Foydalanuvchilar", VaadinIcon.USERS, AdminUsersView.class),
                createNavigationItem("Mukofotlar", VaadinIcon.GIFT, AdminRewardsView.class),
                createNavigationItem("Statistika", VaadinIcon.CHART, AdminStatisticsView.class),
                createNavigationItem("Sozlamalar", VaadinIcon.COG, AdminSettingsView.class)
        );

        sidebar.add(brand, navigation, new JungleBackground(), new AdminFooter());
        return sidebar;
    }

    private RouterLink createNavigationItem(String title, VaadinIcon icon, Class<? extends Component> view) {
        RouterLink item = new RouterLink();
        item.setRoute(view);
        item.addClassName("admin-nav-item");
        I18n.title(item, title);
        var symbol = icon.create();
        symbol.addClassName("admin-nav-icon");
        symbol.getElement().setAttribute("aria-hidden", "true");
        Span label = I18n.localize(new Span(title));
        label.addClassName("admin-nav-label");
        item.add(symbol, label);
        navigationItems.put(view, item);
        pageTitles.put(view, title);
        return item;
    }
}
