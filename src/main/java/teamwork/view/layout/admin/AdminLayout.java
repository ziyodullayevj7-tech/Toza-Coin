package teamwork.view.layout.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
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

import java.util.LinkedHashMap;
import java.util.Map;

@CssImport("./themes/tozacoin/user-layout.css")
public class AdminLayout extends AppLayout implements BeforeEnterObserver {

    private final AdminHeader header = new AdminHeader();
    private final Map<Class<? extends Component>, Div> navigationItems = new LinkedHashMap<>();
    private final Map<Class<? extends Component>, String> pageTitles = new LinkedHashMap<>();

    public AdminLayout() {
        addClassName("admin-layout-container");
        addToNavbar(header);
        addToDrawer(buildSidebar());
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<?> target = event.getNavigationTarget();
        navigationItems.forEach((view, item) -> {
            if (view.equals(target)) {
                item.addClassName("active");
                header.setPageTitle(pageTitles.get(view));
            } else {
                item.removeClassName("active");
            }
        });
    }

    private Div buildSidebar() {
        Div sidebar = new Div();
        sidebar.addClassName("admin-sidebar");

        Div brand = new Div();
        brand.addClassName("admin-brand");
        Div logo = new Div(new Span("TC"));
        logo.addClassName("admin-brand-logo");
        Div brandText = new Div();
        brandText.addClassName("admin-brand-text");
        Span name = new Span("TozaCoin");
        name.addClassName("admin-brand-name");
        Span role = new Span("Admin panel");
        role.addClassName("admin-brand-role");
        brandText.add(name, role);
        brand.add(logo, brandText);

        Div navigation = new Div();
        navigation.addClassName("admin-navigation");
        navigation.add(
                createNavigationItem("Boshqaruv paneli", "📊", AdminDashboard.class),
                createNavigationItem("Moderatsiya navbati", "⏳", AdminModerationView.class),
                createNavigationItem("Shubhali holatlar", "🚨", AdminSuspiciousActivityView.class),
                createNavigationItem("Foydalanuvchilar", "👥", AdminUsersView.class),
                createNavigationItem("Mukofotlar", "🎁", AdminRewardsView.class),
                createNavigationItem("Statistika", "📈", AdminStatisticsView.class),
                createNavigationItem("Sozlamalar", "⚙️", AdminSettingsView.class)
        );

        sidebar.add(brand, navigation, new AdminFooter());
        return sidebar;
    }

    private Div createNavigationItem(String title, String icon, Class<? extends Component> view) {
        Div item = new Div();
        item.addClassName("admin-nav-item");
        item.setTitle(title);
        item.getElement().setAttribute("role", "link");
        item.getElement().setAttribute("tabindex", "0");
        item.getElement().setAttribute("aria-label", title);

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("admin-nav-icon");
        Span label = new Span(title);
        label.addClassName("admin-nav-label");
        item.add(iconSpan, label);

        item.addClickListener(event -> UI.getCurrent().navigate(view));
        item.getElement().addEventListener("keydown", event -> {
            String key = event.getEventData().get("event.key").asText();
            if ("Enter".equals(key) || " ".equals(key)) {
                UI.getCurrent().navigate(view);
            }
        }).addEventData("event.key");

        navigationItems.put(view, item);
        pageTitles.put(view, title);
        return item;
    }
}
