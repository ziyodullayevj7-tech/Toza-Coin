package teamwork.view.layout.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.user.*;

import java.util.HashMap;
import java.util.Map;

@CssImport("./themes/tozacoin/user-layout.css")
public class UserLayout extends AppLayout implements BeforeEnterObserver {

    private final UserHeader header = new UserHeader();
    private final UserFooter footer = new UserFooter();
    private final Map<Class<? extends Component>, Div> navButtonsMap = new HashMap<>();
    private final Map<Class<? extends Component>, String> navTitlesMap = new HashMap<>();
    private final Button btnToggleCollapse = new Button(VaadinIcon.CHEVRON_LEFT.create());
    
    private Div sidebarContainer;
    private boolean isCollapsed = false;

    public UserLayout() {
        addClassName("user-layout-container");

        // Set Top Header
        addToNavbar(header);

        // Set Left Drawer Sidebar
        sidebarContainer = buildSidebar();
        addToDrawer(sidebarContainer);

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<?> target = event.getNavigationTarget();
        for (Map.Entry<Class<? extends Component>, Div> entry : navButtonsMap.entrySet()) {
            if (entry.getKey().equals(target)) {
                entry.getValue().addClassName("active");
                String title = navTitlesMap.get(target);
                if (title != null) {
                    header.setPageTitle(title);
                }
            } else {
                entry.getValue().removeClassName("active");
            }
        }
    }

    private Div buildSidebar() {
        Div sidebar = new Div();
        sidebar.addClassName("user-sidebar");

        // 1. Top Fixed Logo Block with Interactive Collapse Toggle Button
        Div logoHeader = new Div();
        logoHeader.addClassName("sidebar-header");

        RouterLink logoLink = new RouterLink();
        logoLink.addClassName("sidebar-logo-link");
        logoLink.setRoute(teamwork.view.general.LandingView.class);

        Div logoBadge = new Div(new Span("TC"));
        logoBadge.addClassName("sidebar-logo-badge");

        Span logoText = new Span();
        logoText.addClassName("sidebar-logo-text");

        Span textToza = new Span("Toza");
        textToza.addClassName("text-toza");

        Span textCoin = new Span("Coin");
        textCoin.addClassName("text-coin");

        logoText.add(textToza, textCoin);
        logoLink.add(logoBadge, logoText);

        // Collapse / Expand Toggle Button (< / >)
        btnToggleCollapse.addClassName("sidebar-collapse-btn");
        btnToggleCollapse.getElement().setAttribute("title", "Sidebar yig'ish / ochish");
        btnToggleCollapse.addClickListener(e -> toggleSidebarCollapse());

        logoHeader.add(logoLink, btnToggleCollapse);

        // 2. Middle Scrollable Nav Area (Scrolls Up & Down for all 12 items)
        Div scrollNav = new Div();
        scrollNav.addClassName("sidebar-scroll-area");

        scrollNav.add(
                createNavItem("Boshqaruv paneli", "🏡", UserDashboard.class, null),
                createNavItem("Ifloslik xabar", "📍", ReportWasteView.class, null),
                createNavItem("Mening xabarlarim", "📋", MyReportsView.class, null),
                createNavItem("Mening tozalashlarim", "🧹", MyCleanupsView.class, null),
                createNavItem("Xarita", "🗺️", MapView.class, null),
                createNavItem("Kampaniyalar", "🏕️", CampaignsView.class, null),
                createNavItem("Reyting", "🏆", LeaderboardView.class, null),
                createNavItem("Tanga hamyon", "🪙", WalletView.class, null),
                createNavItem("Mukofotlar do'koni", "🎁", RewardsView.class, null),
                createNavItem("Bildirishnomalar", "🔔", NotificationsView.class, "2"),
                createNavItem("Profil", "👤", ProfileView.class, null),
                createNavItem("Sozlamalar", "⚙️", SettingsView.class, null)
        );

        // 3. Bottom Fixed Profile & Chiqish Footer (Stays Fixed)
        sidebar.add(logoHeader, scrollNav, footer);
        return sidebar;
    }

    private void toggleSidebarCollapse() {
        isCollapsed = !isCollapsed;
        if (isCollapsed) {
            sidebarContainer.addClassName("collapsed");
            addClassName("sidebar-collapsed");
            btnToggleCollapse.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
        } else {
            sidebarContainer.removeClassName("collapsed");
            removeClassName("sidebar-collapsed");
            btnToggleCollapse.setIcon(VaadinIcon.CHEVRON_LEFT.create());
        }
    }

    private Div createNavItem(String title, String iconEmoji, Class<? extends Component> viewClass, String badgeText) {
        Div itemBtn = new Div();
        itemBtn.addClassName("nav-item-btn");
        itemBtn.getElement().setAttribute("title", title);

        Div leftGroup = new Div();
        leftGroup.addClassName("nav-item-left");

        Span iconSpan = new Span(iconEmoji);
        iconSpan.addClassName("nav-item-emoji");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("nav-item-title");

        leftGroup.add(iconSpan, titleSpan);
        itemBtn.add(leftGroup);

        if (badgeText != null && !badgeText.isBlank()) {
            Span badge = new Span(badgeText);
            badge.addClassName("nav-badge-pill");
            itemBtn.add(badge);
        }

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
