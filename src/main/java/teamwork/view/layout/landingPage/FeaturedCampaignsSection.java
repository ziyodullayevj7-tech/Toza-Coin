package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "featured-campaigns", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/featured-campaigns.css")
public class FeaturedCampaignsSection extends Div {

    private final Button btnViewAll = I18n.localize(new Button("Barchasini ko'rish →"));
    private final Div grid = I18n.localize(new Div());
    private final Map<String, Button> joinButtonsMap = new HashMap<>();

    public FeaturedCampaignsSection() {
        addClassName("featured-campaigns-wrapper");

        Div container = I18n.localize(new Div());
        container.addClassName("featured-campaigns-container");

        // Header Row
        Div header = I18n.localize(new Div());
        header.addClassName("featured-campaigns-header");

        Div titleGroup = I18n.localize(new Div());
        titleGroup.addClassName("campaigns-title-group");

        Span badge = I18n.localize(new Span("KAMPANIYALAR"));
        badge.addClassName("campaigns-badge");

        H2 title = I18n.localize(new H2("Dolzarb kampaniyalar"));
        title.addClassName("campaigns-title");

        titleGroup.add(badge, title);

        btnViewAll.addClassName("btn-view-all");
        btnViewAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        header.add(titleGroup, btnViewAll);

        // Grid Container
        grid.addClassName("campaigns-grid");

        container.add(header, grid);
        add(container);

        // Load Default Sample Campaigns
        setCampaigns(getDefaultSampleCampaigns());
    }

    // Public method to load dynamic campaigns from a Service in the future
    public void setCampaigns(List<CampaignItem> campaigns) {
        grid.removeAll();
        joinButtonsMap.clear();

        if (campaigns == null || campaigns.isEmpty()) {
            return;
        }

        for (CampaignItem campaign : campaigns) {
            grid.add(createCampaignCard(campaign));
        }
    }

    // ClickListener support for "Barchasini ko'rish" button
    public void addViewAllClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnViewAll.addClickListener(listener);
    }

    public Button getViewAllButton() {
        return btnViewAll;
    }

    // ClickListener support for individual campaign "Qo'shilish" buttons
    public void addJoinClickListener(String campaignId, ComponentEventListener<ClickEvent<Button>> listener) {
        Button btn = joinButtonsMap.get(campaignId);
        if (btn != null) {
            btn.addClickListener(listener);
        }
    }

    private Div createCampaignCard(CampaignItem item) {
        Div card = I18n.localize(new Div());
        card.addClassName("campaign-card");

        // Top Media Container
        Div media = I18n.localize(new Div());
        media.addClassName("campaign-card-media");

        Image img = I18n.localize(new Image(item.getImageUrl(), item.getTitle()));
        img.addClassName("campaign-card-img");

        Span rewardBadge = I18n.localize(new Span());
        rewardBadge.addClassName("campaign-reward-badge");
        rewardBadge.add(I18n.localize(new Span("🪙")), I18n.localize(new Span(item.getRewardCoins() + " tanga")));

        media.add(img, rewardBadge);

        // Card Body
        Div body = I18n.localize(new Div());
        body.addClassName("campaign-card-body");

        H3 cardTitle = I18n.localize(new H3(item.getTitle()));
        cardTitle.addClassName("campaign-card-title");

        // Meta Row (Location + Date)
        Div metaRow = I18n.localize(new Div());
        metaRow.addClassName("campaign-meta-row");

        Span locItem = I18n.localize(new Span());
        locItem.addClassName("campaign-meta-item");
        locItem.add(I18n.localize(new Span("📍")), I18n.localize(new Span(item.getLocation())));

        Span dateItem = I18n.localize(new Span());
        dateItem.addClassName("campaign-meta-item");
        dateItem.add(I18n.localize(new Span("📅")), I18n.localize(new Span(item.getDate())));

        metaRow.add(locItem, dateItem);

        // Progress Bar Block
        Div progressBlock = I18n.localize(new Div());
        progressBlock.addClassName("campaign-progress-block");

        Div progressInfo = I18n.localize(new Div());
        progressInfo.addClassName("campaign-progress-info");
        progressInfo.add(
                I18n.localize(new Span(item.getParticipantsCount() + " ishtirokchi")),
                I18n.localize(new Span(item.getProgressPercentage() + "%"))
        );

        Div progressBar = I18n.localize(new Div());
        progressBar.addClassName("campaign-progress-bar");

        Div progressFill = I18n.localize(new Div());
        progressFill.addClassName("campaign-progress-fill");
        progressFill.getStyle().set("width", item.getProgressPercentage() + "%");

        progressBar.add(progressFill);
        progressBlock.add(progressInfo, progressBar);

        // Join Button
        Button btnJoin = I18n.localize(new Button("Qo'shilish"));
        btnJoin.addClassName("btn-campaign-join");
        btnJoin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnJoin.getStyle().set("background-color", "#2D7D3E").set("color", "#ffffff").set("width", "100%");
        joinButtonsMap.put(item.getId(), btnJoin);

        body.add(cardTitle, metaRow, progressBlock, btnJoin);
        card.add(media, body);

        return card;
    }

    private List<CampaignItem> getDefaultSampleCampaigns() {
        List<CampaignItem> list = new ArrayList<>();
        list.add(new CampaignItem("1", "Chirchiq daryosi tozalash", "Toshkent viloyati", "15 Avgust 2026", 200, 84, 68, "images/campaign-1.jpg"));
        list.add(new CampaignItem("2", "Parklar tozalash haftaligi", "Samarqand shahri", "22 Avgust 2026", 150, 120, 45, "images/campaign-2.jpg"));
        list.add(new CampaignItem("3", "Amudaryo qirg'og'i", "Xorazm viloyati", "30 Avgust 2026", 300, 56, 82, "images/campaign-3.jpg"));
        return list;
    }

    // Service DTO Model for Campaign Items
    public static class CampaignItem {
        private String id;
        private String title;
        private String location;
        private String date;
        private int rewardCoins;
        private int participantsCount;
        private int progressPercentage;
        private String imageUrl;

        public CampaignItem() {
        }

        public CampaignItem(String id, String title, String location, String date, int rewardCoins, int participantsCount, int progressPercentage, String imageUrl) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.date = date;
            this.rewardCoins = rewardCoins;
            this.participantsCount = participantsCount;
            this.progressPercentage = progressPercentage;
            this.imageUrl = imageUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getRewardCoins() {
            return rewardCoins;
        }

        public void setRewardCoins(int rewardCoins) {
            this.rewardCoins = rewardCoins;
        }

        public int getParticipantsCount() {
            return participantsCount;
        }

        public void setParticipantsCount(int participantsCount) {
            this.participantsCount = participantsCount;
        }

        public int getProgressPercentage() {
            return progressPercentage;
        }

        public void setProgressPercentage(int progressPercentage) {
            this.progressPercentage = progressPercentage;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
