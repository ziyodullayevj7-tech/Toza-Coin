package teamwork.view.layout.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.user.*;
import teamwork.util.NavigationGuard;

import java.util.HashMap;
import java.util.Map;

@CssImport("./themes/tozacoin/user-layout.css")
@CssImport("./themes/tozacoin/trashout.css")
@CssImport("./themes/tozacoin/user-jungle.css")
public class UserLayout extends AppLayout implements BeforeEnterObserver {

    private final UserHeader header = new UserHeader();
    private final UserFooter footer = new UserFooter();
    private final Map<Class<? extends Component>, Div> navButtonsMap = new HashMap<>();
    private final Map<Class<? extends Component>, String> navTitlesMap = new HashMap<>();

    public UserLayout() {
        addClassNames("user-layout-container", "user-jungle-layout");

        // Set Top Header
        addToNavbar(header);

        // Set Left Drawer Sidebar
        addToDrawer(buildSidebar());

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!NavigationGuard.requireLogin(event)) {
            return;
        }
        Class<?> target = event.getNavigationTarget();
        for (Map.Entry<Class<? extends Component>, Div> entry : navButtonsMap.entrySet()) {
            if (entry.getKey().equals(target)) {
                entry.getValue().addClassName("active");
                entry.getValue().getElement().setAttribute("aria-current", "page");
                String title = navTitlesMap.get(target);
                if (title != null) {
                    header.setPageTitle(title);
                }
            } else {
                entry.getValue().removeClassName("active");
                entry.getValue().getElement().removeAttribute("aria-current");
            }
        }
    }

    private Div buildSidebar() {
        Div sidebar = I18n.localize(new Div());
        sidebar.addClassName("user-sidebar");

        // Brand
        Div logoHeader = I18n.localize(new Div());
        logoHeader.addClassName("sidebar-header");

        RouterLink logoLink = I18n.localize(new RouterLink());
        logoLink.addClassName("sidebar-logo-link");
        logoLink.setRoute(UserDashboard.class);

        Div logoBadge = I18n.localize(new Div(I18n.localize(new Span("TC"))));
        logoBadge.addClassName("sidebar-logo-badge");

        Span logoText = I18n.localize(new Span());
        logoText.addClassName("sidebar-logo-text");

        Span textToza = I18n.localize(new Span("Toza"));
        textToza.addClassName("text-toza");

        Span textCoin = I18n.localize(new Span("Coin"));
        textCoin.addClassName("text-coin");

        logoText.add(textToza, textCoin);
        logoLink.add(logoBadge, logoText);

        logoHeader.add(logoLink);

        // Five primary destinations
        Div scrollNav = I18n.localize(new Div());
        scrollNav.addClassName("sidebar-scroll-area");

        scrollNav.add(
                createNavItem("Boshqaruv paneli", "dashboard", UserDashboard.class),
                createNavItem("Report", "camera", ReportWasteView.class),
                createNavItem("Mening ishlarim", "reports", MyReportsView.class),
                createNavItem("Xarita", "map", MapView.class),
                createNavItem("Profil", "profile", ProfileView.class)
        );

        // Account and logout
        sidebar.add(logoHeader, scrollNav, new JungleBackground(), footer);
        return sidebar;
    }

    private Div createNavItem(String title, String icon, Class<? extends Component> viewClass) {
        Div itemBtn = I18n.localize(new Div());
        itemBtn.getElement().setAttribute("role", "link");
        itemBtn.getElement().setAttribute("tabindex", "0");
        itemBtn.getElement().addEventListener("keydown", e -> UI.getCurrent().navigate(viewClass))
                .setFilter("event.key === 'Enter' || event.key === ' '");
        itemBtn.addClassName("nav-item-btn");
        I18n.attribute(itemBtn, "title", title);

        Div leftGroup = I18n.localize(new Div());
        leftGroup.addClassName("nav-item-left");

        Image iconSpan = new Image("icons/user/" + icon + ".svg", "");
        iconSpan.addClassName("user-nav-icon");
        iconSpan.getElement().setAttribute("aria-hidden", "true");

        Span titleSpan = I18n.localize(new Span(title));
        titleSpan.addClassName("nav-item-title");

        leftGroup.add(iconSpan, titleSpan);
        itemBtn.add(leftGroup);

        if (viewClass != null) {
            navButtonsMap.put(viewClass, itemBtn);
            navTitlesMap.put(viewClass, title);
            itemBtn.addClickListener(event -> UI.getCurrent().navigate(viewClass));
        }

        return itemBtn;
    }

    public UserHeader getHeader() {
        return header;
    }

    public UserFooter getFooter() {
        return footer;
    }
}
