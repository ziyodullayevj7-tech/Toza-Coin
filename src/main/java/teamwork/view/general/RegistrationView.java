package teamwork.view.general;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.dto.ProfileDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.service.AuthService;
import teamwork.view.general.home.LoginView;
import teamwork.view.layout.AuthPage;

import java.util.Locale;
import java.util.Map;

@Route("register")
@AnonymousAllowed
public class RegistrationView extends AuthPage implements com.vaadin.flow.router.HasDynamicTitle {
    private final AuthService authService;
    private final Binder<ProfileDto> binder = new Binder<>(ProfileDto.class);
    private final TextField firstName = I18n.localize(new TextField("Ism"));
    private final TextField lastName = I18n.localize(new TextField("Familiya"));
    private final EmailField email = I18n.localize(new EmailField("Email"));
    private final TextField phone = I18n.localize(new TextField("Telefon raqam"));
    private final PasswordField password = I18n.localize(new PasswordField("Parol"));
    private final PasswordField confirm = I18n.localize(new PasswordField("Parolni tasdiqlash"));
    private final Checkbox terms = I18n.localize(new Checkbox("Foydalanish shartlari va maxfiylik siyosatiga roziman."));
    private final Button submit = I18n.localize(new Button("Hisob yaratish", VaadinIcon.ARROW_RIGHT.create()));

    public RegistrationView(AuthService authService) {
        super(true, "Yashil kelajakka", "qo'shiling.", "Hisob yarating. Toza muhit sari birinchi qadamni tashlang.");
        this.authService = authService;
        I18n.placeholder(firstName, "Ismingiz");
        firstName.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.GIVEN_NAME);
        firstName.setMaxLength(100);
        I18n.placeholder(lastName, "Familiyangiz");
        lastName.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.FAMILY_NAME);
        lastName.setMaxLength(100);
        I18n.placeholder(email, "siz@example.com");
        email.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.EMAIL);
        email.setMaxLength(254);
        I18n.placeholder(phone, "+998 90 123 45 67");
        phone.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.TEL);
        phone.setMaxLength(25);
        phone.getElement().setAttribute("inputmode", "tel");
        I18n.placeholder(password, "Kamida 8 ta belgi");
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        I18n.placeholder(confirm, "Parolni qayta kiriting");
        confirm.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        firstName.addClassName("auth-field");
        firstName.setRequiredIndicatorVisible(true);
        lastName.addClassName("auth-field");
        lastName.setRequiredIndicatorVisible(true);
        email.addClassName("auth-field");
        email.setRequiredIndicatorVisible(true);
        phone.addClassName("auth-field");
        phone.setRequiredIndicatorVisible(true);
        password.addClassName("auth-field");
        password.setRequiredIndicatorVisible(true);
        confirm.addClassName("auth-field");
        confirm.setRequiredIndicatorVisible(true);
        binder.forField(firstName).asRequired(ctx -> I18n.t("Ismingizni kiriting."))
                .withConverter(String::trim, value -> value).withValidator(value -> !value.isBlank(), ctx -> I18n.t("Ismingizni kiriting."))
                .bind(ProfileDto::getName, ProfileDto::setName);
        binder.forField(lastName).asRequired(ctx -> I18n.t("Familiyangizni kiriting."))
                .withConverter(String::trim, value -> value).withValidator(value -> !value.isBlank(), ctx -> I18n.t("Familiyangizni kiriting."))
                .bind(ProfileDto::getSurname, ProfileDto::setSurname);
        binder.forField(email).asRequired(ctx -> I18n.t("Email manzilingizni kiriting."))
                .withConverter(value -> value.trim().toLowerCase(Locale.ROOT), value -> value)
                .withValidator((value, ctx) -> new EmailValidator(I18n.t("To'g'ri email manzilini kiriting.")).apply(value, ctx))
                .bind(ProfileDto::getEmail, ProfileDto::setEmail);
        binder.forField(phone).asRequired(ctx -> I18n.t("Telefon raqamingizni kiriting."))
                .withValidator(value -> !normalizePhone(value).isEmpty(), ctx -> I18n.t("O'zbekiston telefon raqamini kiriting."))
                .withConverter(RegistrationView::normalizePhone, value -> value)
                .bind(ProfileDto::getPhone, ProfileDto::setPhone);
        binder.forField(password).asRequired(ctx -> I18n.t("Parol yarating."))
                .withValidator(value -> value.length() >= 8, ctx -> I18n.t("Parol kamida 8 ta belgidan iborat bo'lsin."))
                .bind(ProfileDto::getPassword, ProfileDto::setPassword);
        var confirmation = binder.forField(confirm).asRequired(ctx -> I18n.t("Parolni tasdiqlang."))
                .withValidator(value -> value.equals(password.getValue()), ctx -> I18n.t("Parollar bir-biriga mos kelmadi."))
                .bind(dto -> "", (dto, value) -> {
                });
        password.addValueChangeListener(event -> {
            if (!confirm.isEmpty()) confirmation.validate();
        });
        terms.addClassName("auth-terms");
        binder.forField(terms).withValidator(Boolean.TRUE::equals, ctx -> I18n.t("Davom etish uchun roziligingiz kerak."))
                .bind(dto -> false, (dto, value) -> {
                });
        Div names = row(firstName, lastName);
        Div contact = row(email, phone);
        Div passwords = row(password, confirm);
        Div fields = I18n.localize(new Div(names, contact, passwords));
        fields.addClassName("auth-fields");
        submit.addClassName("auth-submit");
        submit.setDisableOnClick(true);
        submit.addClickListener(event -> register());
        submit.addClickShortcut(Key.ENTER).listenOn(this);
        Paragraph switchPage = I18n.localize(new Paragraph(I18n.localize(new Span("Hisobingiz bormi?")), I18n.localize(new RouterLink("Kirish", LoginView.class))));
        switchPage.addClassName("auth-switch");
        form.add(fields, terms, submit, switchPage);
    }

    private Div row(com.vaadin.flow.component.Component... fields) {
        Div row = I18n.localize(new Div(fields));
        row.addClassName("auth-field-row");
        return row;
    }

    private void register() {
        clearFeedback();
        try {
            ProfileDto dto = new ProfileDto();
            if (!binder.writeBeanIfValid(dto)) return;
            authService.register(dto);
            UI.getCurrent().navigate(LoginView.class, QueryParameters.simple(Map.of("registered", "1")));
        } catch (AppBadRequestException e) {
            showFeedback("Bu email bilan hisob mavjud yoki ma'lumotlar qabul qilinmadi. Tekshiring yoki hisobingizga kiring.", false);
        } catch (Exception e) {
            showFeedback("Hisobni yaratib bo'lmadi. Birozdan so'ng qayta urinib ko'ring.", false);
        } finally {
            submit.setEnabled(true);
        }
    }

    private static String normalizePhone(String value) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() == 9) return "+998" + digits;
        if (digits.length() == 12 && digits.startsWith("998")) return "+" + digits;
        return "";
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Ro'yxatdan o'tish | TozaCoin");
    }
}
