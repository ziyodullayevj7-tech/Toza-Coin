package teamwork.view.general;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.dto.ProfileDto;
import teamwork.service.AuthService;

@Route("register")
@PageTitle("Ro'yxatdan o'tish - TozaCoin")
@AnonymousAllowed
@CssImport("./themes/tozacoin/register.css")
public class RegistrationView extends Div {

    private final AuthService authService;

    private final TextField txtFirstName = new TextField("Ism");
    private final TextField txtLastName = new TextField("Familiya");
    private final EmailField txtEmail = new EmailField("Email");
    private final TextField txtPhone = new TextField("Telefon raqam");
    private final PasswordField txtPassword = new PasswordField("Parol");
    private final PasswordField txtConfirmPassword = new PasswordField("Parolni tasdiqlash");
    private final Checkbox chkTerms = new Checkbox("Foydalanish shartlari va Maxfiylik siyosati bilan roziman");
    private final Button btnSubmit = new Button("Ro'yxatdan o'tish");

    public RegistrationView(AuthService authService) {
        this.authService = authService;
        addClassName("register-page-wrapper");

        // Top Brand Logo
        RouterLink logoLink = new RouterLink("", LandingView.class);
        logoLink.addClassName("register-brand-logo");

        Span logoBadge = new Span("TC");
        logoBadge.addClassName("logo-badge");

        Span logoText = new Span();
        logoText.addClassName("logo-text");

        Span spanToza = new Span("Toza");
        spanToza.addClassName("text-toza");

        Span spanCoin = new Span("Coin");
        spanCoin.addClassName("text-coin");

        logoText.add(spanToza, spanCoin);
        logoLink.add(logoBadge, logoText);

        // Form Card Container
        Div card = new Div();
        card.addClassName("register-card");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("register-card-header");

        H2 title = new H2("Ro'yxatdan o'tish");
        title.addClassName("register-card-title");

        Paragraph subtitle = new Paragraph("Ma'lumotlaringizni kiriting");
        subtitle.addClassName("register-card-subtitle");

        headerBlock.add(title, subtitle);

        // Fields Setup
        txtFirstName.setPlaceholder("Jasur");
        txtLastName.setPlaceholder("Nazarov");

        Div nameRow = new Div();
        nameRow.addClassName("register-form-row");
        nameRow.add(txtFirstName, txtLastName);

        txtEmail.setPlaceholder("jasur@example.com");
        txtPhone.setPlaceholder("+998 90 123 45 67");

        txtPassword.setPlaceholder("Kamida 8 ta belgi");
        txtConfirmPassword.setPlaceholder("Parolni qayta kiriting");

        Div passwordRow = new Div();
        passwordRow.addClassName("register-form-row");
        passwordRow.add(txtPassword, txtConfirmPassword);

        chkTerms.addClassName("register-terms-checkbox");

        btnSubmit.addClassName("btn-register-submit");
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmit.addClickListener(event -> handleRegisterSubmit());

        card.add(
            headerBlock,
            nameRow,
            txtEmail,
            txtPhone,
            passwordRow,
            chkTerms,
            btnSubmit
        );

        // Bottom Login Link
        Paragraph footerText = new Paragraph();
        footerText.addClassName("register-footer-text");
        footerText.add(new Span("Hisobingiz bormi? "));

        // RouterLink to login (or '#' if LoginView page created in next step)
        AnchorLink loginLink = new AnchorLink("Kirish");
        loginLink.addClassName("register-login-link");
        footerText.add(loginLink);

        add(logoLink, card, footerText);
    }

    private void handleRegisterSubmit() {
        String firstName = txtFirstName.getValue() != null ? txtFirstName.getValue().trim() : "";
        String lastName = txtLastName.getValue() != null ? txtLastName.getValue().trim() : "";
        String email = txtEmail.getValue() != null ? txtEmail.getValue().trim() : "";
        String phone = txtPhone.getValue() != null ? txtPhone.getValue().trim() : "";
        String password = txtPassword.getValue() != null ? txtPassword.getValue() : "";
        String confirmPassword = txtConfirmPassword.getValue() != null ? txtConfirmPassword.getValue() : "";

        if (firstName.isEmpty()) {
            showErrorNotification("Iltimos, ismingizni kiriting!");
            txtFirstName.focus();
            return;
        }

        if (lastName.isEmpty()) {
            showErrorNotification("Iltimos, familiyangizni kiriting!");
            txtLastName.focus();
            return;
        }

        if (email.isEmpty()) {
            showErrorNotification("Iltimos, email manzilingizni kiriting!");
            txtEmail.focus();
            return;
        }

        if (phone.isEmpty()) {
            showErrorNotification("Iltimos, telefon raqamingizni kiriting!");
            txtPhone.focus();
            return;
        }

        if (!phone.isBlank()) {
            String digits = phone.replaceAll("\\D", "");

            if (digits.length() == 9) {
                phone = "+998" + digits;
            } else if (digits.length() == 12 && digits.startsWith("998")) {
                phone = "+" + digits;
            } else {
                showErrorNotification("Please enter a valid phone number!");
                txtPhone.focus();
                return;
            }
        }

        if (password.isEmpty()) {
            showErrorNotification("Iltimos, parolni kiriting!");
            txtPassword.focus();
            return;
        }

        if (password.length() < 8) {
            showErrorNotification("Parol kamida 8 ta belgidan iborat bo'lishi kerak!");
            txtPassword.focus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorNotification("Kiritilgan parollar bir-biriga mos kelmadi!");
            txtConfirmPassword.focus();
            return;
        }

        if (!chkTerms.getValue()) {
            showErrorNotification("Ro'yxatdan o'tish uchun foydalanish shartlariga rozi bo'lishingiz kerak!");
            chkTerms.focus();
            return;
        }

        ProfileDto dto = new ProfileDto();
        dto.setName(firstName);
        dto.setSurname(lastName);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setPassword(password);

        authService.register(dto);

        // All fields valid -> show success notification & redirect to home
        Notification successNotif = Notification.show("Ro'yxatdan o'tish muvaffaqiyatli yakunlandi!", 4000, Notification.Position.BOTTOM_END);
        successNotif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        getUI().ifPresent(ui -> ui.navigate(""));
    }

    private void showErrorNotification(String text) {
        Notification notif = Notification.show(text, 3500, Notification.Position.BOTTOM_END);
        notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Helper anchor link for login
    private static class AnchorLink extends com.vaadin.flow.component.html.Anchor {
        public AnchorLink(String text) {
            super("login", text);
        }
    }
}
