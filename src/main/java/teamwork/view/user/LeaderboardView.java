package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.service.LeaderBoardService;
import teamwork.util.SecurityUtils;
import teamwork.view.layout.user.UserLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Route(value = "user/leaderboard", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/user-layout.css")
public class LeaderboardView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {

    private static final int PAGE_SIZE = 10;
    private final LeaderBoardService leaderBoardService;
    private final String currentProfileId;
    private final Div podium = I18n.localize(new Div());
    private final Div entries = I18n.localize(new Div());
    private final Div results = I18n.localize(new Div());
    private final Span resultCount = I18n.localize(new Span());
    private final Span pageLabel = I18n.localize(new Span());
    private final Button previousButton = I18n.localize(new Button("← Oldingi"));
    private final Button nextButton = I18n.localize(new Button("Keyingi →"));

    private Page<LeaderBoardDto> currentPage;
    private int pageNumber;

    @Autowired
    public LeaderboardView(LeaderBoardService leaderBoardService) {
        this.leaderBoardService = leaderBoardService;
        this.currentProfileId = SecurityUtils.getCurrentUserId();

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("user-leaderboard-page");

        add(buildHeader(), buildBoard());
        loadPage();
    }

    private Div buildHeader() {
        Div header = I18n.localize(new Div());
        header.addClassName("user-leaderboard-header");

        Span icon = I18n.localize(new Span("🏆"));
        icon.addClassName("user-leaderboard-icon");

        H1 title = I18n.localize(new H1("Eng faol foydalanuvchilar"));
        title.addClassName("user-leaderboard-title");

        Paragraph subtitle = I18n.localize(new Paragraph("TozaCoin hamjamiyatida eng ko'p tanga jamg'arganlar"));
        subtitle.addClassName("user-leaderboard-subtitle");

        header.add(icon, title, subtitle);
        return header;
    }

    private Div buildBoard() {
        Div board = I18n.localize(new Div());
        board.addClassName("user-leaderboard-board");

        Div boardHeader = I18n.localize(new Div());
        boardHeader.addClassName("user-leaderboard-board-header");

        Div heading = I18n.localize(new Div());
        heading.addClassName("user-leaderboard-heading");
        H2 sectionTitle = I18n.localize(new H2("Foydalanuvchilar reytingi"));
        sectionTitle.addClassName("user-leaderboard-section-title");
        Paragraph scoreDescription = I18n.localize(new Paragraph("Umumiy ishlab topilgan tangalar bo'yicha"));
        scoreDescription.addClassName("user-leaderboard-score-description");
        heading.add(sectionTitle, scoreDescription);

        Button refreshButton = I18n.localize(new Button("⟳ Yangilash", event -> loadPage()));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClassName("user-leaderboard-refresh");
        boardHeader.add(heading, refreshButton);

        podium.addClassName("user-leaderboard-podium");

        Div listHeader = I18n.localize(new Div());
        listHeader.addClassName("user-leaderboard-list-header");
        Span listTitle = I18n.localize(new Span("Barcha ishtirokchilar"));
        listTitle.addClassName("user-leaderboard-list-title");
        resultCount.addClassName("user-leaderboard-count");
        listHeader.add(listTitle, resultCount);
        entries.addClassName("user-leaderboard-entries");

        HorizontalLayout pagination = new HorizontalLayout();
        pagination.addClassName("user-leaderboard-pagination");
        pagination.setWidthFull();
        pagination.setAlignItems(Alignment.CENTER);
        pagination.setJustifyContentMode(JustifyContentMode.BETWEEN);

        previousButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previousButton.addClickListener(event -> {
            if (currentPage != null && currentPage.hasPrevious()) {
                pageNumber--;
                loadPage();
            }
        });
        nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextButton.addClickListener(event -> {
            if (currentPage != null && currentPage.hasNext()) {
                pageNumber++;
                loadPage();
            }
        });
        pageLabel.addClassName("user-leaderboard-page-label");
        pagination.add(previousButton, pageLabel, nextButton);

        results.addClassName("user-leaderboard-results");
        board.add(boardHeader, podium, listHeader, entries, results, pagination);
        return board;
    }

    private void loadPage() {
        try {
            currentPage = leaderBoardService.getLeaderBoard(pageNumber, PAGE_SIZE);
            renderPage();
        } catch (RuntimeException exception) {
            results.removeAll();
            Paragraph message = I18n.localize(new Paragraph("Reytingni yuklab bo'lmadi. Qayta urinib ko'ring."));
            message.addClassName("user-leaderboard-error");
            Button retry = I18n.localize(new Button("Qayta urinish", event -> loadPage()));
            retry.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            results.add(message, retry);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            I18n.text(pageLabel, "");
        }
    }

    private void renderPage() {
        List<LeaderBoardDto> users = currentPage.getContent();
        List<LeaderBoardDto> topUsers = pageNumber == 0 ? users.stream().limit(3).toList() : List.of();

        podium.removeAll();
        if (!topUsers.isEmpty()) {
            for (int rank : new int[]{2, 1, 3}) {
                if (rank <= topUsers.size()) {
                    podium.add(buildPodiumCard(topUsers.get(rank - 1), rank));
                }
            }
            podium.setVisible(true);
        } else {
            podium.setVisible(false);
        }

        entries.removeAll();
        List<LeaderBoardDto> listedUsers = pageNumber == 0 ? users.stream().skip(3).toList() : users;
        if (users.isEmpty()) {
            Paragraph empty = I18n.localize(new Paragraph("Hozircha reytingda foydalanuvchilar yo'q."));
            empty.addClassName("user-leaderboard-empty");
            entries.add(empty);
        } else if (listedUsers.isEmpty()) {
            Paragraph topOnly = I18n.localize(new Paragraph("Hozircha barcha ishtirokchilar kuchli uchlikda!"));
            topOnly.addClassName("user-leaderboard-empty");
            entries.add(topOnly);
        } else {
            int firstRank = pageNumber * PAGE_SIZE + (pageNumber == 0 ? 4 : 1);
            for (int index = 0; index < listedUsers.size(); index++) {
                entries.add(buildUserRow(listedUsers.get(index), firstRank + index));
            }
        }

        I18n.text(resultCount, currentPage.getTotalElements() + " ishtirokchi");
        I18n.text(pageLabel, (pageNumber + 1) + " / " + Math.max(currentPage.getTotalPages(), 1));
        previousButton.setEnabled(currentPage.hasPrevious());
        nextButton.setEnabled(currentPage.hasNext());
        results.removeAll();
    }

    private Div buildPodiumCard(LeaderBoardDto user, int rank) {
        Div card = I18n.localize(new Div());
        card.addClassNames("user-leaderboard-podium-card", "rank-" + rank);

        Span medal = I18n.localize(new Span(rank == 1 ? "🥇" : rank == 2 ? "🥈" : "🥉"));
        medal.addClassName("user-leaderboard-medal");

        Span avatar = I18n.localize(new Span(initials(user.getFullName())));
        avatar.addClassName("user-leaderboard-podium-avatar");

        Span name = I18n.userText(new Span(), user.getFullName(), "Foydalanuvchi");
        name.addClassName("user-leaderboard-podium-name");

        Span place = I18n.localize(new Span(rank + "-o'rin"));
        place.addClassName("user-leaderboard-podium-place");

        Span score = I18n.localize(new Span("🪙 " + formatCoins(user.getTotalEarnedCoins())));
        score.addClassName("user-leaderboard-podium-score");
        card.add(medal, avatar, name, place, score);
        return card;
    }

    private Div buildUserRow(LeaderBoardDto user, int rank) {
        Div row = I18n.localize(new Div());
        row.addClassName("user-leaderboard-row");
        boolean isCurrentUser = currentProfileId != null && Objects.equals(currentProfileId, user.getProfileId());
        if (isCurrentUser) {
            row.addClassName("is-current-user");
        }

        Span rankNumber = I18n.localize(new Span(String.valueOf(rank)));
        rankNumber.addClassName("user-leaderboard-rank");

        Span avatar = I18n.localize(new Span(initials(user.getFullName())));
        avatar.addClassName("user-leaderboard-avatar");

        Div identity = I18n.localize(new Div());
        identity.addClassName("user-leaderboard-identity");
        Span name = I18n.userText(new Span(), user.getFullName(), "Foydalanuvchi");
        name.addClassName("user-leaderboard-name");
        String district = user.getDistrict() == null || user.getDistrict().isBlank()
                ? "Foydalanuvchi"
                : user.getDistrict();
        Span location = I18n.userText(new Span(), user.getDistrict(), "Foydalanuvchi");
        location.addClassName("user-leaderboard-location");
        identity.add(name, location);

        Div person = I18n.localize(new Div());
        person.addClassName("user-leaderboard-person");
        person.add(rankNumber, avatar, identity);
        if (isCurrentUser) {
            Span you = I18n.localize(new Span("Siz"));
            you.addClassName("user-leaderboard-you");
            person.add(you);
        }

        Span score = I18n.localize(new Span("🪙 " + formatCoins(user.getTotalEarnedCoins())));
        score.addClassName("user-leaderboard-row-score");
        row.add(person, score);
        return row;
    }

    private static String formatCoins(Integer coins) {
        return NumberFormat.getIntegerInstance(Locale.US).format(coins == null ? 0 : coins);
    }

    private static String displayName(LeaderBoardDto user) {
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            return "Foydalanuvchi";
        }
        return user.getFullName().trim();
    }

    private static String initials(String fullName) {
        String name = displayName(fullName);
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.ROOT);
    }

    private static String displayName(String fullName) {
        return fullName == null || fullName.isBlank() ? "Foydalanuvchi" : fullName.trim();
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Reyting | TozaCoin");
    }
}
