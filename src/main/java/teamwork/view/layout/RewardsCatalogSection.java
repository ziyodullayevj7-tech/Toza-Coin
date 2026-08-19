package teamwork.view.layout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "rewards-catalog", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/rewards-catalog.css")
public class RewardsCatalogSection extends Div {

    private final Button btnViewAllRewards = new Button("Barcha mukofotlarni ko'rish →");
    private final Div grid = new Div();
    private final Map<String, Div> rewardCardsMap = new HashMap<>();

    public RewardsCatalogSection() {
        addClassName("rewards-catalog-wrapper");

        Div container = new Div();
        container.addClassName("rewards-catalog-container");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("rewards-catalog-header");

        Span badge = new Span("MUKOFOTLAR");
        badge.addClassName("rewards-catalog-badge");

        H2 title = new H2("Tangangizni haqiqiy narsalarga almashtiring");
        title.addClassName("rewards-catalog-title");

        Paragraph subtitle = new Paragraph("Ishlagan tangangizni Internet, transport, kino va boshqa mukofotlarga almashtiring.");
        subtitle.addClassName("rewards-catalog-subtitle");

        headerBlock.add(badge, title, subtitle);

        // 6 Rewards Grid Container
        grid.addClassName("rewards-catalog-grid");

        // Bottom Action Button
        btnViewAllRewards.addClassName("btn-view-all-rewards");
        btnViewAllRewards.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        container.add(headerBlock, grid, btnViewAllRewards);
        add(container);

        // Load Default Sample Rewards
        setRewards(getDefaultSampleRewards());
    }

    // Public method for loading dynamic Rewards data from a Service
    public void setRewards(List<RewardItem> rewards) {
        grid.removeAll();
        rewardCardsMap.clear();

        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        for (RewardItem reward : rewards) {
            grid.add(createRewardCard(reward));
        }
    }

    // ClickListener support for "Barcha mukofotlarni ko'rish" button
    public void addViewAllRewardsClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnViewAllRewards.addClickListener(listener);
    }

    public Button getViewAllRewardsButton() {
        return btnViewAllRewards;
    }

    // ClickListener support for individual Reward card clicks
    public void addRewardItemClickListener(String rewardId, ComponentEventListener<ClickEvent<Div>> listener) {
        Div card = rewardCardsMap.get(rewardId);
        if (card != null) {
            card.addClickListener(listener);
        }
    }

    private Div createRewardCard(RewardItem item) {
        Div card = new Div();
        card.addClassName("reward-card");

        Div leftGroup = new Div();
        leftGroup.addClassName("reward-card-left");

        Span iconBadge = new Span(item.getIconEmoji());
        iconBadge.addClassName("reward-icon-badge");
        iconBadge.addClassName(item.getBadgeColor());

        Div info = new Div();
        info.addClassName("reward-info");

        Span title = new Span(item.getTitle());
        title.addClassName("reward-title");

        Span category = new Span(item.getCategoryLabel());
        category.addClassName("reward-category");
        category.addClassName(item.getCategoryTextColor());

        info.add(title, category);
        leftGroup.add(iconBadge, info);

        Span priceBadge = new Span();
        priceBadge.addClassName("reward-price-badge");
        priceBadge.add(new Span("🪙"), new Span(item.getCoinCost() + " tanga"));

        card.add(leftGroup, priceBadge);
        rewardCardsMap.put(item.getId(), card);

        return card;
    }

    private List<RewardItem> getDefaultSampleRewards() {
        List<RewardItem> list = new ArrayList<>();
        list.add(new RewardItem("1", "Internet paket (1GB)", "Internet", "📊", "blue", "blue", 100));
        list.add(new RewardItem("2", "Telefon balansini to'ldirish", "To'lov", "📱", "purple", "purple", 250));
        list.add(new RewardItem("3", "Transport kartasi (ATTO)", "Transport", "🚌", "green", "green", 300));
        list.add(new RewardItem("4", "Kino chiptasi", "Ko'ngilochar", "🎬", "pink", "pink", 200));
        list.add(new RewardItem("5", "Supermarket sertifikati", "Oziq-ovqat", "🛒", "yellow", "amber", 500));
        list.add(new RewardItem("6", "Ko'chat — daraxt ekish", "Ekologiya", "🌱", "green", "green", 80));
        return list;
    }

    // Service DTO Model for Reward Items
    public static class RewardItem {
        private String id;
        private String title;
        private String categoryLabel;
        private String iconEmoji;
        private String badgeColor;
        private String categoryTextColor;
        private int coinCost;

        public RewardItem() {}

        public RewardItem(String id, String title, String categoryLabel, String iconEmoji, String badgeColor, String categoryTextColor, int coinCost) {
            this.id = id;
            this.title = title;
            this.categoryLabel = categoryLabel;
            this.iconEmoji = iconEmoji;
            this.badgeColor = badgeColor;
            this.categoryTextColor = categoryTextColor;
            this.coinCost = coinCost;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCategoryLabel() { return categoryLabel; }
        public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }

        public String getIconEmoji() { return iconEmoji; }
        public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

        public String getBadgeColor() { return badgeColor; }
        public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

        public String getCategoryTextColor() { return categoryTextColor; }
        public void setCategoryTextColor(String categoryTextColor) { this.categoryTextColor = categoryTextColor; }

        public int getCoinCost() { return coinCost; }
        public void setCoinCost(int coinCost) { this.coinCost = coinCost; }
    }
}
