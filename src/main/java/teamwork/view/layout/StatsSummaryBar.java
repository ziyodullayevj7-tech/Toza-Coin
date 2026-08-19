package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/stats-summary.css")
public class StatsSummaryBar extends Div {

    private final Span numActiveUsers = new Span("12,847");
    private final Span numSubmittedReports = new Span("89,320");
    private final Span numCleanedWasteKg = new Span("247,500 kg");
    private final Span numCompletedCampaigns = new Span("1,240");
    private final Span numDistributedCoins = new Span("5.2M");

    public StatsSummaryBar() {
        addClassName("stats-summary-wrapper");

        Div container = new Div();
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
        Div item = new Div();
        item.addClassName("stats-summary-item");

        Span iconSpan = new Span(iconEmoji);
        iconSpan.addClassName("stats-summary-icon");

        numberSpan.addClassName("stats-summary-number");

        Span labelSpan = new Span(labelText);
        labelSpan.addClassName("stats-summary-label");

        item.add(iconSpan, numberSpan, labelSpan);
        return item;
    }

    // Dynamic Setters for Backend Service Integration
    public void setActiveUsers(String count) {
        numActiveUsers.setText(count);
    }

    public void setSubmittedReports(String count) {
        numSubmittedReports.setText(count);
    }

    public void setCleanedWasteKg(String count) {
        numCleanedWasteKg.setText(count);
    }

    public void setCompletedCampaigns(String count) {
        numCompletedCampaigns.setText(count);
    }

    public void setDistributedCoins(String count) {
        numDistributedCoins.setText(count);
    }

    public void setStats(String users, String reports, String wasteKg, String campaigns, String coins) {
        numActiveUsers.setText(users);
        numSubmittedReports.setText(reports);
        numCleanedWasteKg.setText(wasteKg);
        numCompletedCampaigns.setText(campaigns);
        numDistributedCoins.setText(coins);
    }
}
