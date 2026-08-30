package teamwork.view.layout.user;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.user.*;

import java.util.ArrayList;
import java.util.List;

@CssImport("./themes/tozacoin/user-layout.css")
public class UserLayout extends AppLayout {

    private final UserHeader header = new UserHeader();
    private final UserFooter footer = new UserFooter();
    private final List<Div> navButtons = new ArrayList<>();
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
                createNavItem("Boshqaruv paneli", "🏡", true, null, e -> navigateTo(UserDashboard.class)),
                createNavItem("Ifloslik xabar", "📍", false, null, e -> navigateTo(ReportWasteView.class)),
                createNavItem("Mening xabarlarim", "📋", false, null, e -> navigateTo(MyReportsView.class)),
                createNavItem("Mening tozalashlarim", "🧹", false, null, e -> navigateTo(MyCleanupsView.class)),
                createNavItem("Xarita", "🗺️", false, null, e -> navigateTo(MapView.class)),
                createNavItem("Kampaniyalar", "🏕️", false, null, e -> navigateTo(CampaignsView.class)),
                createNavItem("Reyting", "🏆", false, null, e -> navigateTo(LeaderboardView.class)),
                createNavItem("Tanga hamyon", "🪙", false, null, e -> navigateTo(WalletView.class)),
                createNavItem("Mukofotlar do'koni", "🎁", false, null, e -> navigateTo(RewardsView.class)),
                createNavItem("Bildirishnomalar", "🔔", false, "2", e -> navigateTo(NotificationsView.class)),
                createNavItem("Profil", "👤", false, null, e -> navigateTo(ProfileView.class)),
                createNavItem("Sozlamalar", "⚙️", false, null, e -> navigateTo(SettingsView.class))
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

    private Div createNavItem(String title, String iconEmoji, boolean isActive, String badgeText,
                              ComponentEventListener<ClickEvent<Div>> listener) {
        Div itemBtn = new Div();
        itemBtn.addClassName("nav-item-btn");
        itemBtn.getElement().setAttribute("title", title);
        
        if (isActive) {
            itemBtn.addClassName("active");
        }

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

        itemBtn.addClickListener(event -> {
            setActiveItem(itemBtn);
            header.setPageTitle(title);
            if (listener != null) {
                listener.onComponentEvent(event);
            }
        });

        navButtons.add(itemBtn);
        return itemBtn;
    }

    private void setActiveItem(Div activeBtn) {
        for (Div btn : navButtons) {
            btn.removeClassName("active");
        }
        activeBtn.addClassName("active");
    }

    private void navigateTo(Class<? extends com.vaadin.flow.component.Component> targetView) {
        if (targetView != null) {
            UI.getCurrent().navigate(targetView);
        }
    }

    public UserHeader getHeader() {
        return header;
    }

    public UserFooter getFooter() {
        return footer;
    }
}
