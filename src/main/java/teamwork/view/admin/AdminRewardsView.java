package teamwork.view.admin;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teamwork.dto.reward.RewardDto;
import teamwork.exceptions.BaseException;
import teamwork.i18n.I18n;
import teamwork.service.RewardService;
import teamwork.service.AttachService;
import teamwork.view.layout.admin.*;

@Route(value = "admin/rewards", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
@CssImport("./themes/tozacoin/rewards.css")
public class AdminRewardsView extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    private static final Logger log = LoggerFactory.getLogger(AdminRewardsView.class);
    private final RewardService service;
    private final AttachService attachments;
    private final Grid<RewardDto> grid = new Grid<>();
    private final Div state = new Div();
    private final Span summary = new Span();
    private final Button previous = I18n.localize(new Button("Oldingi"));
    private final Button next = I18n.localize(new Button("Keyingi"));
    private int page;

    public AdminRewardsView(RewardService service, AttachService attachments) {
        super("Mukofotlar", "Tangalar evaziga olinadigan xizmat va chegirmalarni boshqaring.");
        this.service = service;
        this.attachments = attachments;
        addClassName("rewards-admin-page");
        Button create = I18n.localize(new Button("Mukofot qo'shish", VaadinIcon.PLUS.create(), e -> openEditor(null)));
        create.setId("reward-create");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = I18n.localize(new Button("Yangilash", VaadinIcon.REFRESH.create(), e -> load()));
        Div toolbar = new Div(I18n.localize(new H2("Mukofotlar katalogi")), new Div(refresh, create));
        toolbar.addClassName("rewards-toolbar");
        grid.setId("admin-rewards-grid");
        grid.addClassNames("admin-data-grid", "rewards-admin-grid");
        grid.setAllRowsVisible(true);
        grid.addComponentColumn(reward -> {
            Div identity = new Div(new Span(reward.getTitle()), new Span(reward.getProviderName()));
            identity.addClassName("reward-identity");
            Div cell = new Div(buildPhoto(reward, false), identity);
            cell.addClassName("reward-admin-identity");
            return cell;
        }).setHeader(I18n.t("Mukofot")).setWidth("300px").setFlexGrow(1);
        grid.addColumn(RewardDto::getCoinPrice).setHeader(I18n.t("Narxi (tanga)"))
                .setTextAlign(ColumnTextAlign.END).setWidth("140px").setFlexGrow(0);
        grid.addColumn(RewardDto::getQuantity).setHeader(I18n.t("Mavjud soni"))
                .setTextAlign(ColumnTextAlign.END).setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(reward -> {
            Span badge = I18n.localize(new Span(Boolean.TRUE.equals(reward.getIsActive()) ? "Faol" : "Nofaol"));
            badge.addClassNames("reward-status", Boolean.TRUE.equals(reward.getIsActive()) ? "is-active" : "is-inactive");
            return badge;
        }).setHeader(I18n.t("Holat")).setWidth("110px").setFlexGrow(0);
        grid.addComponentColumn(reward -> {
            Button view = I18n.localize(new Button("Ko'rish", e -> openDetails(reward)));
            view.addClassName("reward-view-button");
            Button edit = I18n.localize(new Button("Tahrirlash", e -> openEditor(reward)));
            Button toggle = I18n.localize(new Button(Boolean.TRUE.equals(reward.getIsActive()) ? "To'xtatish" : "Faollashtirish",
                    e -> confirmStatus(reward)));
            Div actions = new Div(view, edit, toggle);
            actions.addClassName("reward-row-actions");
            return actions;
        }).setHeader(I18n.t("Amallar")).setWidth("340px").setFlexGrow(0);
        state.addClassName("rewards-state");
        state.getElement().setAttribute("role", "status");
        previous.setId("admin-rewards-previous");
        next.setId("admin-rewards-next");
        previous.addClickListener(e -> { if (page > 0) { page--; load(); } });
        next.addClickListener(e -> { page++; load(); });
        Div footer = new Div(previous, summary, next);
        footer.addClassName("rewards-pagination");
        Div panel = new Div(toolbar, state, grid, footer);
        panel.addClassNames("admin-data-panel", "rewards-workspace");
        add(panel);
        load();
    }

    private void load() {
        try {
            var result = service.getRewardsPagForAdmin(page, 10);
            if (page > 0 && result.isEmpty()) { page--; load(); return; }
            grid.setItems(result.getContent());
            grid.setVisible(!result.isEmpty());
            state.setVisible(result.isEmpty());
            state.removeClassName("is-error");
            I18n.text(state, "Hali mukofotlar yo'q. Birinchi mukofotni qo'shing.");
            I18n.text(summary, (result.isEmpty() ? 0 : page + 1) + " / " + result.getTotalPages());
            previous.setEnabled(result.hasPrevious());
            next.setEnabled(result.hasNext());
        } catch (RuntimeException exception) {
            log.error("Could not load reward management", exception);
            grid.setVisible(false);
            state.setVisible(true);
            state.addClassName("is-error");
            I18n.text(state, "Mukofotlarni yuklab bo'lmadi. Yangilash tugmasini bosing.");
            previous.setEnabled(false);
            next.setEnabled(false);
            summary.setText("");
        }
    }

    private Div buildPhoto(RewardDto reward, boolean detail) {
        Div placeholder = new Div(VaadinIcon.GIFT.create());
        placeholder.addClassName("reward-admin-photo-placeholder");
        placeholder.getElement().setAttribute("aria-hidden", String.valueOf(!detail));
        if (detail) placeholder.add(I18n.localize(new Span("Rasm mavjud emas")));
        Div frame = new Div(placeholder);
        frame.addClassName(detail ? "reward-detail-photo" : "reward-admin-thumbnail");
        String path = reward.getOptionalRewardImage();
        if (path != null && !path.isBlank()) {
            Image image = new Image(path, detail ? value(reward.getTitle()) : "");
            image.getElement().setAttribute("loading", "lazy");
            image.getElement().addEventListener("error", e -> image.setVisible(false));
            frame.add(image);
        }
        return frame;
    }

    private Dialog openDetails(RewardDto reward) {
        Dialog dialog = new Dialog();
        dialog.setId("reward-details-dialog");
        dialog.addClassNames("admin-theme-dialog", "reward-dialog", "reward-details-dialog");
        I18n.headerTitle(dialog, "Mukofot tafsilotlari");
        dialog.setWidth("720px");
        H2 title = I18n.userText(new H2(), reward.getTitle(), "Mukofot");
        title.addClassName("reward-detail-title");
        Span provider = I18n.userText(new Span(), reward.getProviderName(), "Ko'rsatilmagan");
        provider.addClassName("reward-detail-provider");
        Div metadata = new Div(
                detailValue("Narxi (tanga)", reward.getCoinPrice() == null ? "?" : reward.getCoinPrice().toString()),
                detailValue("Mavjud soni", reward.getQuantity() == null ? "?" : reward.getQuantity().toString()),
                detailValue("Holat", I18n.t(Boolean.TRUE.equals(reward.getIsActive()) ? "Faol" : "Nofaol")));
        metadata.addClassName("reward-detail-metadata");
        H3 descriptionTitle = I18n.localize(new H3("Tavsif"));
        Paragraph description = I18n.userText(new Paragraph(), reward.getDescription(), "Tavsif kiritilmagan.");
        description.addClassName("reward-detail-description");
        Div content = new Div(buildPhoto(reward, true), new Div(provider, title), metadata, descriptionTitle, description);
        content.addClassName("reward-detail-content");
        Button close = I18n.localize(new Button("Yopish", e -> dialog.close()));
        Button edit = I18n.localize(new Button("Tahrirlash", e -> {
            dialog.close();
            openEditor(reward);
        }));
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(content);
        dialog.getFooter().add(close, edit);
        dialog.open();
        return dialog;
    }

    private Div detailValue(String label, String value) {
        Span caption = I18n.localize(new Span(label));
        Span text = new Span(value);
        Div item = new Div(caption, text);
        item.addClassName("reward-detail-value");
        return item;
    }

    private Dialog openEditor(RewardDto reward) {
        Dialog dialog = new Dialog();
        dialog.addClassNames("admin-theme-dialog", "reward-dialog");
        I18n.headerTitle(dialog, reward == null ? "Mukofot qo'shish" : "Mukofotni tahrirlash");
        dialog.setWidth("620px");
        TextField title = I18n.localize(new TextField("Nomi"));
        TextField provider = I18n.localize(new TextField("Xizmat ko'rsatuvchi"));
        TextArea description = I18n.localize(new TextArea("Tavsif"));
        IntegerField price = I18n.localize(new IntegerField("Narxi (tanga)"));
        IntegerField quantity = I18n.localize(new IntegerField("Mavjud soni"));
        RewardImageField image = new RewardImageField(attachments, reward == null ? null : reward.getOptionalRewardImage());
        dialog.addOpenedChangeListener(e -> { if (!e.isOpened()) image.dispose(); });
        title.setMaxLength(255);
        provider.setMaxLength(255);
        description.setMaxLength(255);
        price.setMin(1);
        quantity.setMin(0);
        title.setRequiredIndicatorVisible(true);
        provider.setRequiredIndicatorVisible(true);
        description.setRequiredIndicatorVisible(true);
        price.setRequiredIndicatorVisible(true);
        quantity.setRequiredIndicatorVisible(true);
        quantity.setValue(0);
        if (reward != null) {
            title.setValue(value(reward.getTitle()));
            provider.setValue(value(reward.getProviderName()));
            description.setValue(value(reward.getDescription()));
            price.setValue(reward.getCoinPrice());
            quantity.setValue(reward.getQuantity());
        }
        FormLayout form = new FormLayout(title, provider, description, price, quantity, image);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("480px", 2));
        form.setColspan(description, 2);
        form.setColspan(image, 2);
        Div error = new Div();
        error.addClassName("reward-form-error");
        error.getElement().setAttribute("role", "alert");
        error.setVisible(false);
        Button cancel = I18n.localize(new Button("Bekor qilish", e -> dialog.close()));
        Button save = I18n.localize(new Button("Saqlash"));
        save.setId("reward-save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setDisableOnClick(true);
        save.addClickListener(e -> {
            try {
                title.setInvalid(title.getValue().isBlank());
                provider.setInvalid(provider.getValue().isBlank());
                description.setInvalid(description.getValue().isBlank());
                price.setInvalid(price.getValue() == null || price.getValue() < 1);
                quantity.setInvalid(quantity.getValue() == null || quantity.getValue() < 0);
                if (title.isInvalid() || provider.isInvalid() || description.isInvalid()
                        || price.isInvalid() || quantity.isInvalid()) {
                    I18n.text(error, "Maydonlarni tekshiring. Narx musbat, son esa nol yoki undan katta bo'lishi kerak.");
                    error.setVisible(true);
                    return;
                }
                RewardDto dto = new RewardDto();
                dto.setTitle(title.getValue().trim());
                dto.setProviderName(provider.getValue().trim());
                dto.setDescription(description.getValue().trim());
                dto.setCoinPrice(price.getValue());
                dto.setQuantity(quantity.getValue());
                dto.setOptionalRewardImage(image.imagePathForSave());
                boolean success = reward == null ? Boolean.TRUE.equals(service.createReward(dto))
                        : Boolean.TRUE.equals(service.updateReward(reward.getId(), dto));
                if (!success) throw new IllegalStateException("Reward save returned false");
                image.markSaved();
                dialog.close();
                I18n.notify("Mukofot saqlandi.", 3000, Position.TOP_END);
                load();
            } catch (BaseException exception) {
                I18n.text(error, exception.getMessage());
                error.setVisible(true);
            } catch (RuntimeException exception) {
                log.error("Could not save reward", exception);
                I18n.text(error, "Saqlab bo'lmadi. Qayta urinib ko'ring.");
                error.setVisible(true);
            } finally { save.setEnabled(true); }
        });
        dialog.add(form, error);
        dialog.getFooter().add(cancel, save);
        dialog.open();
        return dialog;
    }

    private void confirmStatus(RewardDto reward) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.addClassName("admin-theme-dialog");
        dialog.setHeader(I18n.t("Mukofot holatini o'zgartirish"));
        I18n.text(dialog, Boolean.TRUE.equals(reward.getIsActive())
                ? "Mukofot katalogdan yashiriladi. Avvalgi xaridlar saqlanadi."
                : "Mukofot mavjud bo'lsa foydalanuvchilar katalogida ko'rinadi.");
        dialog.setCancelable(true);
        I18n.property(dialog, "cancelText", "Bekor qilish");
        I18n.property(dialog, "confirmText", "Tasdiqlash");
        dialog.addConfirmListener(e -> {
            try {
                if (!Boolean.TRUE.equals(service.changeRewardStatus(reward.getId()))) throw new IllegalStateException();
                I18n.notify("Mukofot holati yangilandi.", 3000, Position.TOP_END);
            } catch (RuntimeException exception) {
                log.error("Could not change reward status {}", reward.getId(), exception);
                I18n.notify("Holatni yangilab bo'lmadi. Qayta urinib ko'ring.", 4000, Position.TOP_END);
            }
            load();
        });
        dialog.open();
    }

    private static String value(String text) { return text == null ? "" : text; }
    @Override public String getPageTitle() { return I18n.t("Mukofotlar | TozaCoin"); }
}
