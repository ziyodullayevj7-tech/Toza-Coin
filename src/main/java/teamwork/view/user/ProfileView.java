package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.entity.ProfileEntity;
import teamwork.service.AuthService;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/profile", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class ProfileView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {
    private final AuthService authService;
    private final TextField firstName = I18n.localize(new TextField("Ism"));
    private final TextField lastName = I18n.localize(new TextField("Familiya"));
    private final EmailField email = I18n.localize(new EmailField("Email"));
    private final TextField phone = I18n.localize(new TextField("Telefon raqam"));
    private final TextField district = I18n.localize(new TextField("Tuman"));
    private final TextField mahalla = I18n.localize(new TextField("Mahalla"));
    private final Span balance = I18n.localize(new Span());
    private final H2 fullName = I18n.localize(new H2());

    public ProfileView(AuthService authService) {
        this.authService = authService;
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("eco-profile-page");
        Div heading = I18n.localize(new Div(I18n.localize(new H1("Profil")), I18n.localize(new Paragraph("Shaxsiy ma'lumotlaringiz va hisob sozlamalari."))));
        heading.addClassName("eco-page-heading");
        Div summary = I18n.localize(new Div(fullName, balance));
        summary.addClassName("eco-profile-summary");
        firstName.setRequiredIndicatorVisible(true);
        lastName.setRequiredIndicatorVisible(true);
        email.setRequiredIndicatorVisible(true);
        firstName.setMaxLength(100);
        lastName.setMaxLength(100);
        phone.setMaxLength(30);
        Div personal = I18n.localize(new Div(firstName, lastName, phone, district, mahalla));
        personal.addClassName("eco-form-grid");
        Div settings = I18n.localize(new Div(email));
        settings.addClassName("eco-form-grid");
        Button save = I18n.localize(new Button("O'zgarishlarni saqlash", e -> save()));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = I18n.localize(new Button("Bekor qilish", e -> loadProfile()));
        Div actions = I18n.localize(new Div(save, cancel));
        actions.addClassName("eco-form-actions");
        Div card = I18n.localize(new Div(summary, I18n.localize(new H3("Shaxsiy ma'lumotlar")), personal, I18n.localize(new H3("Sozlamalar")),
                I18n.localize(new Paragraph("Email manzilingiz hisobga kirish uchun ishlatiladi.")), settings, actions));
        card.addClassName("eco-profile-form");
        add(heading, card);
        loadProfile();
    }

    private void loadProfile() {
        ProfileEntity p = authService.getCurrentProfileEntity();
        if (p == null) return;
        firstName.setInvalid(false);
        lastName.setInvalid(false);
        email.setInvalid(false);
        firstName.setValue(text(p.getName()));
        lastName.setValue(text(p.getSurname()));
        email.setValue(text(p.getUsername()));
        phone.setValue(text(p.getPhoneNumber()));
        district.setValue(text(p.getDistrict()));
        mahalla.setValue(text(p.getMahalla()));
        I18n.dataText(fullName, (text(p.getName()) + " " + text(p.getSurname())).trim());
        I18n.text(balance, (p.getCoinBalance() == null ? 0 : p.getCoinBalance()) + " TozaCoin");
    }

    private void save() {
        firstName.setInvalid(firstName.isEmpty());
        lastName.setInvalid(lastName.isEmpty());
        email.setInvalid(email.isEmpty() || email.isInvalid());
        if (firstName.isInvalid() || lastName.isInvalid() || email.isInvalid()) {
            I18n.notify("Ism, familiya va to'g'ri email manzilini kiriting.");
            return;
        }
        try {
            authService.updateCurrentProfile(firstName.getValue(), lastName.getValue(), email.getValue(), phone.getValue(), district.getValue(), mahalla.getValue());
            loadProfile();
            I18n.notify("Ma'lumotlar saqlandi.", 3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            I18n.notify(e.getMessage() == null ? "Saqlab bo'lmadi. Qayta urinib ko'ring." : e.getMessage(), 4000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Profil va sozlamalar | TozaCoin");
    }
}
