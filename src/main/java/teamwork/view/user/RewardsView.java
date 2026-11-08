package teamwork.view.user;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teamwork.dto.reward.*;
import teamwork.exceptions.BaseException;
import teamwork.i18n.I18n;
import teamwork.service.AuthService;
import teamwork.service.RewardService;
import teamwork.util.SecurityUtils;
import teamwork.view.layout.user.UserLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route(value = "user/rewards", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/rewards.css")
public class RewardsView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {
    private static final Logger log = LoggerFactory.getLogger(RewardsView.class);
    private final RewardService service;
    private final AuthService auth;
    private final Div cards = new Div();
    private final Div state = new Div();
    private final Span balanceLabel = new Span();
    private final Span summary = new Span();
    private final Div history = new Div();
    private final H2 sectionTitle = new H2();
    private final Span sectionCount = new Span();
    private final Div success = new Div();
    private final Button catalogTab = I18n.localize(new Button("Katalog"));
    private final Button historyTab = I18n.localize(new Button("Mening mukofotlarim"));
    private final Button previous = I18n.localize(new Button("Oldingi"));
    private final Button next = I18n.localize(new Button("Keyingi"));
    private int page;
    private Integer balance;
    private boolean showHistory;

    public RewardsView(RewardService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("rewards-user-page");
        Div title = new Div(I18n.localize(new H1("Mukofotlar do'koni")),
                I18n.localize(new Paragraph("To'plagan tangalaringizni foydali xizmat va chegirmalarga almashtiring.")));
        Div balanceCard = new Div(VaadinIcon.COIN_PILES.create(),
                I18n.localize(new Span("Sizning balansingiz")), balanceLabel);
        balanceCard.addClassName("reward-balance");
        balanceLabel.getElement().setAttribute("aria-live", "polite");
        Div heading = new Div(title, balanceCard);
        heading.addClassName("rewards-heading");
        catalogTab.setId("rewards-catalog-tab");
        historyTab.setId("rewards-history-tab");
        catalogTab.addClickListener(e -> selectTab(false));
        historyTab.addClickListener(e -> selectTab(true));
        Div tabs = new Div(catalogTab, historyTab);
        tabs.addClassName("rewards-tabs");
        Button refresh = I18n.localize(new Button("Yangilash", VaadinIcon.REFRESH.create(), e -> load()));
        Div toolbar = new Div(tabs, refresh);
        toolbar.addClassName("rewards-toolbar");
        cards.setId("rewards-catalog");
        cards.addClassName("reward-catalog");
        state.addClassName("rewards-state");
        state.getElement().setAttribute("role", "status");
        history.setId("rewards-history");
        history.addClassName("reward-owned-list");
        success.setId("reward-purchase-success");
        success.addClassName("reward-success-banner");
        success.getElement().setAttribute("role", "status");
        success.setVisible(false);
        Div sectionHeading = new Div(sectionTitle, sectionCount);
        sectionHeading.addClassName("reward-section-heading");
        previous.setId("rewards-previous");
        next.setId("rewards-next");
        previous.addClickListener(e -> { if (page > 0) { page--; load(); } });
        next.addClickListener(e -> { page++; load(); });
        Div footer = new Div(previous, summary, next);
        footer.addClassName("rewards-pagination");
        Div workspace = new Div(toolbar, success, sectionHeading, state, cards, history, footer);
        workspace.addClassName("rewards-workspace");
        add(heading, workspace);
        selectTab(false);
    }

    private void selectTab(boolean historySelected) {
        success.setVisible(false);
        showHistory = historySelected;
        page = 0;
        catalogTab.setClassName("active", !showHistory);
        historyTab.setClassName("active", showHistory);
        catalogTab.getElement().setAttribute("aria-pressed", String.valueOf(!showHistory));
        historyTab.getElement().setAttribute("aria-pressed", String.valueOf(showHistory));
        I18n.text(sectionTitle, showHistory ? "Siz olgan mukofotlar" : "O'zingizga mos mukofotni tanlang");
        load();
    }

    private void load() {
        cards.removeAll();
        history.removeAll();
        history.setVisible(false);
        cards.setVisible(!showHistory);
        state.setVisible(false);
        state.removeClassName("is-error");
        try {
            balance = auth.getBalanceByUserId(SecurityUtils.getCurrentUserId());
            I18n.text(balanceLabel, (balance == null ? 0 : balance) + " tanga");
            if (showHistory) {
                var result = service.getMyRewards(page, 9);
                if (page > 0 && result.isEmpty()) { page--; load(); return; }
                result.forEach(item -> history.add(ownedCard(item)));
                history.setVisible(!result.isEmpty());
                updatePagination(result);
                if (result.isEmpty()) showState("Hali xaridlar yo'q. Katalogdan o'zingizga mos mukofotni tanlang.");
            } else {
                var result = service.getRewardsPagForUsers(page, 9);
                if (page > 0 && result.isEmpty()) { page--; load(); return; }
                result.forEach(reward -> cards.add(card(reward)));
                updatePagination(result);
                if (result.isEmpty()) showState("Hozircha mukofotlar mavjud emas. Keyinroq yana tekshiring.");
            }
        } catch (RuntimeException exception) {
            log.error("Could not load user rewards", exception);
            balance = null;
            balanceLabel.setText("\u2014");
            cards.removeAll();
            history.setVisible(false);
            state.addClassName("is-error");
            showState("Mukofotlarni yuklab bo'lmadi. Yangilash tugmasini bosing.");
            previous.setEnabled(false);
            next.setEnabled(false);
            summary.setText("");
            sectionCount.setText("");
        }
    }

    private Div card(RewardDto reward) {
        Div visual = rewardPhoto(reward);
        Span provider = new Span(reward.getProviderName());
        provider.addClassName("reward-provider");
        H2 title = new H2(reward.getTitle());
        Paragraph description = new Paragraph(reward.getDescription());
        Span price = I18n.localize(new Span(reward.getCoinPrice() + " tanga"));
        price.addClassName("reward-price");
        Span stock = I18n.localize(new Span("Mavjud soni: " + reward.getQuantity()));
        stock.addClassName("reward-stock");
        Button buy = I18n.localize(new Button("Tangaga olish", e -> openPurchase(reward)));
        buy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        boolean affordable = balance != null && reward.getCoinPrice() != null && reward.getCoinPrice() > 0
                && balance >= reward.getCoinPrice();
        buy.setEnabled(affordable);
        if (!affordable) I18n.text(buy, "Tangalar yetarli emas");
        Button details = I18n.localize(new Button("Ko'rish", e -> openRewardDetails(reward)));
        details.addClassName("reward-card-details");
        Div actions = new Div(details, buy);
        actions.addClassName("reward-card-actions");
        Div body = new Div(provider, title, description, new Div(price, stock), actions);
        body.addClassName("reward-card-body");
        Div card = new Div(visual, body);
        card.addClassName("reward-card");
        return card;
    }

    private Div rewardPhoto(RewardDto reward) {
        Div visual = new Div(VaadinIcon.GIFT.create());
        visual.addClassName("reward-card-image");
        if (reward.getOptionalRewardImage() != null && !reward.getOptionalRewardImage().isBlank()) {
            Image image = new Image(reward.getOptionalRewardImage(), value(reward.getTitle()));
            image.getElement().setAttribute("loading", "lazy");
            image.getElement().addEventListener("error", e -> image.setVisible(false));
            visual.add(image);
        }
        return visual;
    }

    private Dialog openRewardDetails(RewardDto reward) {
        Dialog dialog = new Dialog();
        dialog.addClassNames("reward-dialog", "reward-user-detail-dialog");
        I18n.headerTitle(dialog, "Mukofot tafsilotlari");
        dialog.setWidth("560px");
        Div content = new Div(rewardPhoto(reward), new Span(value(reward.getProviderName())),
                new H2(value(reward.getTitle())), new Paragraph(value(reward.getDescription())),
                I18n.localize(new Span(reward.getCoinPrice() + " tanga")),
                I18n.localize(new Span("Mavjud soni: " + reward.getQuantity())));
        content.addClassName("reward-user-detail-content");
        Button close = I18n.localize(new Button("Yopish", e -> dialog.close()));
        Button buy = I18n.localize(new Button("Tangaga olish", e -> { dialog.close(); openPurchase(reward); }));
        buy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buy.setEnabled(balance != null && reward.getCoinPrice() != null && reward.getCoinPrice() > 0
                && balance >= reward.getCoinPrice() && reward.getQuantity() != null && reward.getQuantity() > 0);
        dialog.add(content);
        dialog.getFooter().add(close, buy);
        dialog.open();
        return dialog;
    }

    private Div ownedCard(MyRewardDto item) {
        Div icon = new Div(VaadinIcon.GIFT.create());
        icon.addClassName("reward-owned-icon");
        Div identity = new Div(new Span(value(item.getRewardProviderName())), new H3(value(item.getRewardTitle())),
                new Span(date(item.getPurchasedDateTime())));
        identity.addClassName("reward-owned-identity");
        Div amounts = new Div(I18n.localize(new Span("Soni: " + item.getPurchasedQuantity())),
                I18n.localize(new Span(item.getCoinsSpent() + " tanga")));
        amounts.addClassName("reward-owned-amounts");
        Button receipt = I18n.localize(new Button("Xarid tafsilotlari", e -> openReceipt(item)));
        Div card = new Div(icon, identity, amounts, receipt);
        card.addClassName("reward-owned-card");
        return card;
    }

    private Dialog openReceipt(MyRewardDto item) {
        Dialog dialog = new Dialog();
        dialog.addClassNames("reward-dialog", "reward-receipt-dialog");
        I18n.headerTitle(dialog, "Xarid tafsilotlari");
        dialog.setWidth("480px");
        Div content = new Div(VaadinIcon.CHECK_CIRCLE.create(), new H2(value(item.getRewardTitle())),
                new Paragraph(value(item.getRewardProviderName())),
                receiptValue("Soni", value(item.getPurchasedQuantity())),
                receiptValue("Sarflangan tangalar", value(item.getCoinsSpent())),
                receiptValue("Xarid sanasi", date(item.getPurchasedDateTime())),
                receiptValue("Xarid raqami", value(item.getPurchaseId())));
        content.addClassName("reward-receipt-content");
        dialog.add(content);
        dialog.getFooter().add(I18n.localize(new Button("Yopish", e -> dialog.close())));
        dialog.open();
        return dialog;
    }

    private Div receiptValue(String label, String text) {
        Div row = new Div(I18n.localize(new Span(label)), new Span(text));
        row.addClassName("reward-receipt-row");
        return row;
    }

    private void showPurchaseSuccess(RewardDto reward, int quantity) {
        success.removeAll();
        Div text = new Div(I18n.localize(new Span("Xarid muvaffaqiyatli yakunlandi")),
                new Span(value(reward.getTitle()) + " \u00d7 " + quantity));
        success.add(VaadinIcon.CHECK_CIRCLE.create(), text,
                I18n.localize(new Button("Mening mukofotlarim", e -> selectTab(true))));
        success.setVisible(true);
    }

    private static String value(Object text) { return text == null ? "\u2014" : text.toString(); }

    private Dialog openPurchase(RewardDto reward) {
        Dialog dialog = new Dialog();
        dialog.addClassNames("reward-dialog", "reward-checkout-dialog");
        I18n.headerTitle(dialog, "Mukofotni olish");
        dialog.setWidth("440px");
        IntegerField quantity = I18n.localize(new IntegerField("Soni"));
        quantity.setMin(1);
        quantity.setStepButtonsVisible(true);
        int max = Math.min(reward.getQuantity(), (balance == null ? 0 : balance) / reward.getCoinPrice());
        quantity.setMax(max);
        quantity.setValue(1);
        Span total = new Span();
        total.addClassName("reward-purchase-total");
        Span remaining = new Span();
        remaining.addClassName("reward-remaining-balance");
        remaining.getElement().setAttribute("aria-live", "polite");
        Div error = new Div();
        error.addClassName("reward-form-error");
        error.getElement().setAttribute("role", "alert");
        error.setVisible(false);
        Button cancel = I18n.localize(new Button("Bekor qilish", e -> dialog.close()));
        Button confirm = I18n.localize(new Button("Xaridni tasdiqlash"));
        confirm.setId("reward-purchase-confirm");
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.setDisableOnClick(true);
        Runnable updateTotal = () -> {
            Integer count = quantity.getValue();
            boolean valid = count != null && count >= 1 && count <= max;
            quantity.setInvalid(!valid);
            confirm.setEnabled(valid);
            I18n.text(total, valid ? "Jami: " + ((long) count * reward.getCoinPrice()) + " tanga" : "To'g'ri sonni kiriting.");
            I18n.text(remaining, valid ? "Xariddan keyingi balans: " + (balance - (long) count * reward.getCoinPrice()) + " tanga" : "");
        };
        quantity.addValueChangeListener(e -> updateTotal.run());
        updateTotal.run();
        boolean[] submitted = {false};
        confirm.addClickListener(e -> {
            if (submitted[0]) return;
            Integer count = quantity.getValue();
            if (count == null || count < 1 || count > max) { updateTotal.run(); return; }
            submitted[0] = true;
            quantity.setReadOnly(true);
            try {
                if (!Boolean.TRUE.equals(service.rewardSellForCoin(reward.getId(), count))) throw new IllegalStateException();
                dialog.close();
                I18n.notify("Mukofot olindi. Xaridni Mening mukofotlarim bo'limida ko'rishingiz mumkin.", 4500, Position.TOP_END);
                load();
                showPurchaseSuccess(reward, count);
            } catch (BaseException exception) {
                I18n.text(error, exception.getMessage());
                error.setVisible(true);
                // Stock or balance may have changed since this dialog was opened.
                confirm.setEnabled(false);
                load();
            } catch (RuntimeException exception) {
                log.error("Could not purchase reward {}", reward.getId(), exception);
                I18n.text(error, "Xarid yakunlanmadi. Oynani yoping va balans hamda xaridlar tarixini yangilang.");
                error.setVisible(true);
                confirm.setEnabled(false);
            }
        });
        Div checkoutSummary = new Div(rewardPhoto(reward), new Div(new H2(reward.getTitle()), new Paragraph(reward.getProviderName()),
                I18n.localize(new Span(reward.getCoinPrice() + " tanga"))));
        checkoutSummary.addClassName("reward-checkout-summary");
        dialog.add(checkoutSummary, quantity, total, remaining,
                I18n.localize(new Paragraph("Tasdiqlangandan so'ng tangalar balansingizdan yechiladi.")), error);
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
        return dialog;
    }

    private void updatePagination(org.springframework.data.domain.Page<?> result) {
        previous.setEnabled(result.hasPrevious());
        next.setEnabled(result.hasNext());
        I18n.text(summary, (result.isEmpty() ? 0 : page + 1) + " / " + result.getTotalPages());
        I18n.text(sectionCount, "Jami: " + result.getTotalElements());
    }
    private void showState(String message) {
        state.removeAll();
        state.add(VaadinIcon.GIFT.create(), I18n.localize(new Paragraph(message)));
        if (showHistory && !state.hasClassName("is-error")) {
            state.add(I18n.localize(new Button("Katalogga o'tish", e -> selectTab(false))));
        }
        state.setVisible(true);
    }
    private static String date(String value) {
        try { return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")); }
        catch (RuntimeException exception) { return value == null ? "\u2014" : value; }
    }
    @Override public String getPageTitle() { return I18n.t("Mukofotlar do'koni | TozaCoin"); }
}
