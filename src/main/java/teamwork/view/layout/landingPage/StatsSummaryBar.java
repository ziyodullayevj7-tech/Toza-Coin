package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/stats-summary.css")
public class StatsSummaryBar extends Div {

    private final Span numActiveUsers = I18n.localize(new Span("12,847"));
    private final Span numSubmittedReports = I18n.localize(new Span("89,320"));
    private final Span numCleanedWasteKg = I18n.localize(new Span("247,500 kg"));
    private final Span numCompletedCampaigns = I18n.localize(new Span("1,240"));
    private final Span numDistributedCoins = I18n.localize(new Span("5.2M"));

    public StatsSummaryBar() {
        addClassName("stats-summary-wrapper");

        Div container = I18n.localize(new Div());
        container.addClassName("stats-summary-container");

        container.add(
                createStatItem("👥", numActiveUsers, "Faol ko'ngillilar"),
                createStatItem("📍", numSubmittedReports, "Yuborilgan xabarlar"),
                createStatItem("♻️", numCleanedWasteKg, "Tozalangan chiqindi"),
                createStatItem("🏆", numCompletedCampaigns, "Yakunlangan aksiyalar"),
                createStatItem("🪙", numDistributedCoins, "Tarqatilgan tangalar")
        );

        add(container);
    }

    private Div createStatItem(String iconEmoji, Span numberSpan, String labelText) {
        Div item = I18n.localize(new Div());
        item.addClassName("stats-summary-item");

        Span iconSpan = I18n.localize(new Span(iconEmoji));
        iconSpan.addClassName("stats-summary-icon");

        numberSpan.addClassName("stats-summary-number");

        Span labelSpan = I18n.localize(new Span(labelText));
        labelSpan.addClassName("stats-summary-label");

        item.add(iconSpan, numberSpan, labelSpan);
        return item;
    }

    // Dynamic Setters for Backend Service Integration
    public void setActiveUsers(String count) {
        I18n.text(numActiveUsers, count);
    }

    public void setSubmittedReports(String count) {
        I18n.text(numSubmittedReports, count);
    }

    public void setCleanedWasteKg(String count) {
        I18n.text(numCleanedWasteKg, count);
    }

    public void setCompletedCampaigns(String count) {
        I18n.text(numCompletedCampaigns, count);
    }

    public void setDistributedCoins(String count) {
        I18n.text(numDistributedCoins, count);
    }

    public void setStats(String users, String reports, String wasteKg, String campaigns, String coins) {
        I18n.text(numActiveUsers, users);
        I18n.text(numSubmittedReports, reports);
        I18n.text(numCleanedWasteKg, wasteKg);
        I18n.text(numCompletedCampaigns, campaigns);
        I18n.text(numDistributedCoins, coins);
    }
}
