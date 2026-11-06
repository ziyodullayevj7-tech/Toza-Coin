package teamwork.view.general.home;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.dto.LoginDto;
import teamwork.dto.ProfileDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.service.AuthService;
import teamwork.view.admin.AdminDashboard;
import teamwork.view.general.RegistrationView;
import teamwork.view.layout.AuthPage;
import teamwork.view.user.UserDashboard;

import java.util.Locale;

@Route("login")
@AnonymousAllowed
public class LoginView extends AuthPage implements BeforeEnterObserver, com.vaadin.flow.router.HasDynamicTitle {
    private final AuthService authService;
    private final Binder<LoginDto> binder = new Binder<>(LoginDto.class);
    private final TextField email = I18n.localize(new TextField("Email"));
    private final PasswordField password = I18n.localize(new PasswordField("Parol"));
    private final Button submit = I18n.localize(new Button("Kirish", VaadinIcon.ARROW_RIGHT.create()));

    public LoginView(AuthService authService) {
        super(false, "Xush kelibsiz.", "Yana birgalikda.", "Hisobingizga kiring va yaxshi harakatni davom ettiring.");
        this.authService = authService;
        email.addClassName("auth-field");
        I18n.placeholder(email, "siz@example.com");
        email.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.USERNAME);
        email.setRequiredIndicatorVisible(true);
        email.setMaxLength(254);
        password.addClassName("auth-field");
        I18n.placeholder(password, "Parolingizni kiriting");
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.CURRENT_PASSWORD);
        password.setRequiredIndicatorVisible(true);
        password.setRevealButtonVisible(true);
        binder.forField(email).asRequired(ctx -> I18n.t("Email manzilingizni kiriting."))
                .withConverter(value -> value.trim().toLowerCase(Locale.ROOT), value -> value)
                .bind(LoginDto::getUsername, LoginDto::setUsername);
        binder.forField(password).asRequired(ctx -> I18n.t("Parolingizni kiriting."))
                .bind(LoginDto::getPassword, LoginDto::setPassword);
        Div fields = I18n.localize(new Div(email, password));
        fields.addClassName("auth-fields");
        submit.addClassName("auth-submit");
        submit.setDisableOnClick(true);
        submit.addClickListener(event -> login());
        submit.addClickShortcut(Key.ENTER).listenOn(this);
        Div divider = I18n.localize(new Div(I18n.localize(new Span("yoki"))));
        divider.addClassName("auth-divider");
        Anchor google = I18n.localize(new Anchor("/oauth2/authorization/google", ""));
        google.addClassName("auth-google");
        Span googleMark = I18n.localize(new Span("G"));
        googleMark.addClassName("auth-google-mark");
        google.add(googleMark, I18n.localize(new Span("Google orqali davom etish")));
        Paragraph switchPage = I18n.localize(new Paragraph(I18n.localize(new Span("Hali hisobingiz yo'qmi?")),
                I18n.localize(new RouterLink("Ro'yxatdan o'tish", RegistrationView.class))));
        switchPage.addClassName("auth-switch");
        form.add(fields, submit, divider, google, switchPage);
    }

    private void login() {
        clearFeedback();
        try {
            LoginDto dto = new LoginDto();
            if (!binder.writeBeanIfValid(dto)) return;
            ProfileDto profile = authService.login(dto);
            boolean admin = profile.getRoles() != null && profile.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role));
            if (admin) UI.getCurrent().navigate(AdminDashboard.class);
            else UI.getCurrent().navigate(UserDashboard.class);
        } catch (AppBadRequestException e) {
            showFeedback("Email yoki parol noto'g'ri, yoki hisob faol emas. Ma'lumotlarni tekshirib qayta urinib ko'ring.", false);
        } catch (Exception e) {
            showFeedback("Hozir kirib bo'lmadi. Birozdan so'ng qayta urinib ko'ring.", false);
        } finally {
            submit.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var parameters = event.getLocation().getQueryParameters().getParameters();
        if (parameters.containsKey("registered")) {
            showFeedback("Hisobingiz yaratildi. Endi email va parolingiz bilan kiring.", true);
        } else if (parameters.containsKey("error")) {
            showFeedback("Kirish amalga oshmadi. Qayta urinib ko'ring.", false);
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Kirish | TozaCoin");
    }
}
