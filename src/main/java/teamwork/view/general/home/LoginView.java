package teamwork.view.general.home;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;



@Route("login")
@PageTitle("Login | Toza Coin")
@CssImport("./themes/tozacoin/login-view.css")
public class LoginView extends HorizontalLayout {

    public LoginView() {
        addClassName("login-root");
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        add(buildBrandPanel(), buildFormPanel());
    }

    // CHAP PANEL - Brend / statistika
    private Div buildBrandPanel() {
        Div panel = new Div();
        panel.addClassName("brand-panel");

        // Logotip
        HorizontalLayout logoRow = new HorizontalLayout();
        logoRow.addClassName("logo-row");
        logoRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Div logoBadge = new Div(new Span("TC"));
        logoBadge.addClassName("logo-badge");

        Span logoText = new Span("TozaCoin");
        logoText.addClassName("logo-text");

        logoRow.add(logoBadge, logoText);

        // Barg ikonasi (o'zimizning SVG, VaadinIcon to'plamiga bog'liq emas)
        Html leafIcon = new Html(
                "<svg viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
                        "<path d='M6 3C6 3 3 8 3 13C3 17.4183 6.58172 21 11 21C15 21 21 17 21 9C21 5 18 3 18 3" +
                        "C18 3 16 6 12 6C8 6 6 3 6 3Z' fill='#a9e08f'/>" +
                        "<path d='M11 21C11 21 11 13 6 8' stroke='#1f5c33' stroke-width='1.2' stroke-linecap='round'/>" +
                        "</svg>");
        leafIcon.addClassName("leaf-icon");

        // Sarlavha va tavsif
        H1 heading = new H1("Ekologik harakatga qo'shiling");
        heading.addClassName("brand-heading");

        Paragraph subtitle = new Paragraph(
                "Tabiatni tozalash orqali tanga ishlang va O'zbekistonni yanada yashil qiling.");
        subtitle.addClassName("brand-subtitle");

        // Statistika kartochkalari (2x2 grid)
        Div statsGrid = new Div();
        statsGrid.addClassName("stats-grid");
        statsGrid.add(
                statCard("12,847", "Faol ko'ngillilar"),
                statCard("247T kg", "Tozalangan"),
                statCard("5.2M", "Tanga tarqatildi"),
                statCard("1,240", "Kampaniyalar")
        );

        // Footer
        Span footer = new Span("© 2026 TozaCoin");
        footer.addClassName("brand-footer");

        VerticalLayout content = new VerticalLayout(logoRow, leafIcon, heading, subtitle, statsGrid, footer);
        content.addClassName("brand-content");
        content.setSpacing(false);
        content.setPadding(false);
        content.setSizeFull();

        panel.add(content);
        return panel;
    }

    private Div statCard(String value, String label) {
        Div card = new Div();
        card.addClassName("stat-card");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");

        card.add(valueSpan, labelSpan);
        return card;
    }


    // O'NG PANEL - Kirish formasi
    private Div buildFormPanel() {
        Div panel = new Div();
        panel.addClassName("form-panel");

        VerticalLayout formBox = new VerticalLayout();
        formBox.addClassName("form-box");
        formBox.setSpacing(false);
        formBox.setPadding(false);

        H2 title = new H2("Kirish");
        title.addClassName("form-title");

        Paragraph subtitle = new Paragraph("Hisobingizga kiring va ekologik harakatni davom ettiring.");
        subtitle.addClassName("form-subtitle");

        // Google orqali kirish tugmasi
        Button googleButton = new Button("Google orqali kirish", VaadinIcon.GLOBE.create());
        googleButton.addClassName("google-button");
        googleButton.addClickListener(e ->
                Notification.show("Google orqali kirish bosildi (integratsiya keyinroq ulanadi)"));

        // Ajratuvchi chiziq "yoki"
        Div divider = buildDivider();

        // Email/telefon maydoni
        TextField emailField = new TextField();
        emailField.setLabel("Email yoki telefon");
        emailField.setPlaceholder("email@example.com");
        emailField.addClassName("form-field");
        emailField.setWidthFull();

        // Parol maydoni + "Parolni unutdingizmi?" havolasi
        HorizontalLayout passwordLabelRow = new HorizontalLayout();
        passwordLabelRow.addClassName("password-label-row");
        passwordLabelRow.setWidthFull();
        passwordLabelRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span passwordLabel = new Span("Parol");
        passwordLabel.addClassName("field-label");

        Anchor forgotPassword = new Anchor("#", "Parolni unutdingizmi?");
        forgotPassword.addClassName("forgot-link");

        passwordLabelRow.add(passwordLabel, forgotPassword);

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("••••••••");
        passwordField.addClassName("form-field");
        passwordField.setWidthFull();
        passwordField.setRevealButtonVisible(true);

        // Eslab qolish checkbox
        Checkbox rememberMe = new Checkbox("Eslab qolish");
        rememberMe.addClassName("remember-checkbox");
// Kirish tugmasi
        Button loginButton = new Button("Kirish");
        loginButton.addClassName("login-button");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

// Faqat login logikasini chaqirish
        loginButton.addClickListener(e -> handleLogin(emailField, passwordField));

        // Ro'yxatdan o'tish havolasi
        HorizontalLayout signupRow = new HorizontalLayout();
        signupRow.addClassName("signup-row");
        signupRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        Span signupText = new Span("Hisobingiz yo'qmi? ");
        Anchor signupLink = new Anchor("register", "Ro'yxatdan o'tish");
        signupLink.addClassName("signup-link");
        signupRow.add(signupText, signupLink);

        formBox.add(title, subtitle, googleButton, divider, emailField,
                passwordLabelRow, passwordField, rememberMe, loginButton, signupRow);

        panel.add(formBox);
        return panel;
    }

    private Div buildDivider() {
        Div wrapper = new Div();
        wrapper.addClassName("divider-wrapper");

        Hr line1 = new Hr();
        line1.addClassName("divider-line");
        Span text = new Span("yoki");
        text.addClassName("divider-text");
        Hr line2 = new Hr();
        line2.addClassName("divider-line");

        wrapper.add(line1, text, line2);
        return wrapper;
    }

    private void handleLogin(TextField emailField, PasswordField passwordField) {
        String identifier = emailField.getValue();
        String password = passwordField.getValue();

        if (identifier.isBlank() || password.isBlank()) {
            showError("Iltimos, barcha maydonlarni to'ldiring");
            return;
        }

        // TODO: hozircha bazaga tegmayapmiz - AuthService tayyor bo'lgach shu yerga ulanadi
        // masalan: LoginResult result = authService.login(identifier, password); ...
        Notification.show("Kirish so'rovi yuborildi: " + identifier)
                .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message)
                .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
    }
}