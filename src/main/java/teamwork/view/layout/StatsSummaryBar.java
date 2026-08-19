package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/stats-summary.css")
public class StatsSummaryBar extends Div {

    private final Span activeUsersNum = new Span("12,847");
    private final Span submittedReportsNum = new Span("89,320");
    private final Span cleanedWasteNum = new Span("247,500");
    private final Span completedCampaignsNum = new Span("1,240");
    private final Span distributedCoinsNum = new Span("5.2M");

    public StatsSummaryBar() {
        addClassName("stats-summary-wrapper");

        Div container = new Div();
        container.addClassName("stats-summary-container");

        container.add(
            createStatItem("👥", activeUsersNum, "Faol foydalanuvchilar"),
            createStatItem("📍", submittedReportsNum, "Yuborilgan xabarlar"),
            createStatItem("♻️", cleanedWasteNum, "Tozalangan chiqindi (kg)"),
            createStatItem("🏆", completedCampaignsNum, "Yakunlangan kampaniyalar"),
            createStatItem("🪙", distributedCoinsNum, "Tarqatilgan tanga")
        );

        add(container);
    }

    // Constructor to pass initial dynamic values directly from a Service/DTO
    public StatsSummaryBar(String activeUsers, String submittedReports, String cleanedWasteKg, String completedCampaigns, String distributedCoins) {
        this();
        setStats(activeUsers, submittedReports, cleanedWasteKg, completedCampaigns, distributedCoins);
    }

    // Dynamic Setters for future Service integration
    public void setActiveUsers(String count) {
        this.activeUsersNum.setText(count != null ? count : "0");
    }

    public void setSubmittedReports(String count) {
        this.submittedReportsNum.setText(count != null ? count : "0");
    }

    public void setCleanedWasteKg(String count) {
        this.cleanedWasteNum.setText(count != null ? count : "0");
    }

    public void setCompletedCampaigns(String count) {
        this.completedCampaignsNum.setText(count != null ? count : "0");
    }

    public void setDistributedCoins(String count) {
        this.distributedCoinsNum.setText(count != null ? count : "0");
    }

    public void setStats(String activeUsers, String submittedReports, String cleanedWasteKg, String completedCampaigns, String distributedCoins) {
        setActiveUsers(activeUsers);
        setSubmittedReports(submittedReports);
        setCleanedWasteKg(cleanedWasteKg);
        setCompletedCampaigns(completedCampaigns);
        setDistributedCoins(distributedCoins);
    }

    private Div createStatItem(String icon, Span numberSpan, String label) {
        Div item = new Div();
        item.addClassName("stats-summary-item");

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("stats-summary-icon");

        numberSpan.addClassName("stats-summary-number");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stats-summary-label");

        item.add(iconSpan, numberSpan, labelSpan);
        return item;
    }
}
