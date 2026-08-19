package teamwork.view.layout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

@CssImport("./themes/tozacoin/leaderboard.css")
public class LeaderboardSection extends Div {

    private final Button btnFullLeaderboard = new Button("To'liq reytingni ko'rish →");
    private final Div leaderboardList = new Div();

    public LeaderboardSection() {
        addClassName("leaderboard-wrapper");

        Div container = new Div();
        container.addClassName("leaderboard-container");

        // Left Column
        Div leftCol = new Div();
        leftCol.addClassName("leaderboard-left");

        Span badge = new Span("REYTING");
        badge.addClassName("leaderboard-badge");

        H2 title = new H2("Eng faol ko'ngillilar");
        title.addClassName("leaderboard-title");

        Paragraph desc = new Paragraph("Haftalik va oylik reytingda o'z o'rningizni egallang. Top 10 ga kirganlar maxsus mukofotlar olishadi.");
        desc.addClassName("leaderboard-desc");

        btnFullLeaderboard.addClassName("btn-full-leaderboard");
        btnFullLeaderboard.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        leftCol.add(badge, title, desc, btnFullLeaderboard);

        // Right Column (Leaderboard Card)
        Div rightCard = new Div();
        rightCard.addClassName("leaderboard-card");

        Div cardHeader = new Div();
        cardHeader.addClassName("leaderboard-card-header");

        H3 cardTitle = new H3("🏆 Top ko'ngillilar");
        cardTitle.addClassName("leaderboard-card-title");

        Span timePill = new Span("Haftalik");
        timePill.addClassName("leaderboard-time-pill");

        cardHeader.add(cardTitle, timePill);

        leaderboardList.addClassName("leaderboard-list");
        rightCard.add(cardHeader, leaderboardList);

        container.add(leftCol, rightCard);
        add(container);

        // Load Default Leaderboard Data
        setLeaderboard(getDefaultSampleUsers());
    }

    // Public method for loading dynamic Leaderboard data from a Service
    public void setLeaderboard(List<LeaderboardUser> users) {
        leaderboardList.removeAll();
        if (users == null || users.isEmpty()) {
            return;
        }

        for (LeaderboardUser user : users) {
            leaderboardList.add(createUserRow(user));
        }
    }

    // ClickListener support for "To'liq reytingni ko'rish" button
    public void addFullLeaderboardClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnFullLeaderboard.addClickListener(listener);
    }

    public Button getFullLeaderboardButton() {
        return btnFullLeaderboard;
    }

    private Div createUserRow(LeaderboardUser user) {
        Div row = new Div();
        row.addClassName("leaderboard-user-row");

        Div leftGroup = new Div();
        leftGroup.addClassName("user-left-group");

        Span rankBadge = new Span(user.getRankText());
        rankBadge.addClassName("rank-badge");
        if (user.getRank() <= 3) {
            rankBadge.addClassName("top-rank");
        }

        Div avatar = new Div();
        avatar.setText(user.getInitials());
        avatar.addClassName("user-avatar-initials");

        Div userInfo = new Div();
        userInfo.addClassName("user-info");

        Span name = new Span(user.getName());
        name.addClassName("user-name");

        Span meta = new Span(user.getCity() + " · " + user.getCleanupCount() + " tozalash");
        meta.addClassName("user-meta");

        userInfo.add(name, meta);
        leftGroup.add(rankBadge, avatar, userInfo);

        Span coins = new Span();
        coins.addClassName("user-coins");
        coins.add(new Span("🪙"), new Span(user.getCoins()));

        row.add(leftGroup, coins);
        return row;
    }

    private List<LeaderboardUser> getDefaultSampleUsers() {
        List<LeaderboardUser> list = new ArrayList<>();
        list.add(new LeaderboardUser(1, "🥇", "JN", "Jasur Nazarov", "Toshkent", 47, "4,820"));
        list.add(new LeaderboardUser(2, "🥈", "MY", "Malika Yusupova", "Samarqand", 39, "4,210"));
        list.add(new LeaderboardUser(3, "🥉", "BK", "Bobur Karimov", "Buxoro", 35, "3,890"));
        list.add(new LeaderboardUser(4, "4", "NH", "Nodira Hasanova", "Namangan", 31, "3,540"));
        list.add(new LeaderboardUser(5, "5", "UT", "Ulugbek Toshev", "Andijon", 28, "3,120"));
        return list;
    }

    // Service DTO Model for Leaderboard Users
    public static class LeaderboardUser {
        private int rank;
        private String rankText;
        private String initials;
        private String name;
        private String city;
        private int cleanupCount;
        private String coins;

        public LeaderboardUser() {}

        public LeaderboardUser(int rank, String rankText, String initials, String name, String city, int cleanupCount, String coins) {
            this.rank = rank;
            this.rankText = rankText;
            this.initials = initials;
            this.name = name;
            this.city = city;
            this.cleanupCount = cleanupCount;
            this.coins = coins;
        }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public String getRankText() { return rankText; }
        public void setRankText(String rankText) { this.rankText = rankText; }

        public String getInitials() { return initials; }
        public void setInitials(String initials) { this.initials = initials; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public int getCleanupCount() { return cleanupCount; }
        public void setCleanupCount(int cleanupCount) { this.cleanupCount = cleanupCount; }

        public String getCoins() { return coins; }
        public void setCoins(String coins) { this.coins = coins; }
    }
}
