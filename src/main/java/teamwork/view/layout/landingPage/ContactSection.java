package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "contact", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/contact.css")
public class ContactSection extends Div {

    private final TextField txtName = new TextField("Ism familiya");
    private final EmailField txtEmail = new EmailField("Email");
    private final Select<String> selectSubject = new Select<>();
    private final TextArea txtMessage = new TextArea("Xabar");
    private final Button btnSubmit = new Button("📩 Xabar yuborish");

    public ContactSection() {
        addClassName("contact-wrapper");

        Div container = new Div();
        container.addClassName("contact-container");

        // Left Column (Info Block)
        Div leftCol = new Div();
        leftCol.addClassName("contact-left");

        Span badge = new Span("ALOQA");
        badge.addClassName("contact-badge");

        H2 title = new H2("Biz bilan bog'laning");
        title.addClassName("contact-title");

        Paragraph subtitle = new Paragraph("Savol, taklif yoki hamkorlik takliflari bo'lsa — yozing. 1–2 ish kuni ichida javob beramiz.");
        subtitle.addClassName("contact-subtitle");

        Div infoList = new Div();
        infoList.addClassName("contact-info-list");

        infoList.add(
            createContactInfoItem("✉️", "blue", "Email", "info@tozacoin.uz"),
            createContactInfoItem("📱", "green", "Telegram", "@tozacoin_uz"),
            createContactInfoItem("📍", "red", "Manzil", "Toshkent sh., Yunusobod tumani, Amir Temur ko'chasi 107B"),
            createContactInfoItem("⏰", "yellow", "Ish vaqti", "Dushanba–Juma: 09:00–18:00")
        );

        leftCol.add(badge, title, subtitle, infoList);

        // Right Column (Form Card)
        Div formCard = new Div();
        formCard.addClassName("contact-form-card");

        H3 formTitle = new H3("Xabar yuborish");
        formTitle.addClassName("contact-form-title");

        txtName.setPlaceholder("Jasur Nazarov");
        txtEmail.setPlaceholder("jasur@example.com");

        Div row1 = new Div();
        row1.addClassName("contact-form-row");
        row1.add(txtName, txtEmail);

        selectSubject.setLabel("Mavzu");
        selectSubject.setPlaceholder("Tanlang...");
        selectSubject.setItems("Umumiy savol", "Taklif", "Hamkorlik", "Muammo haqida xabar");

        txtMessage.setPlaceholder("Savolingiz yoki taklifingizni yozing...");
        txtMessage.setHeight("130px");

        btnSubmit.addClassName("btn-contact-submit");
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Form Submit Listener & Validation
        btnSubmit.addClickListener(event -> handleFormSubmit());

        formCard.add(formTitle, row1, selectSubject, txtMessage, btnSubmit);

        container.add(leftCol, formCard);
        add(container);
    }

    private void handleFormSubmit() {
        String name = txtName.getValue() != null ? txtName.getValue().trim() : "";
        String email = txtEmail.getValue() != null ? txtEmail.getValue().trim() : "";
        String subject = selectSubject.getValue() != null ? selectSubject.getValue().trim() : "";
        String message = txtMessage.getValue() != null ? txtMessage.getValue().trim() : "";

        if (name.isEmpty()) {
            showErrorNotification("Iltimos, ism familiyangizni kiriting!");
            txtName.focus();
            return;
        }

        if (email.isEmpty()) {
            showErrorNotification("Iltimos, email manzilingizni kiriting!");
            txtEmail.focus();
            return;
        }

        if (subject.isEmpty()) {
            showErrorNotification("Iltimos, mavzuni tanlang!");
            selectSubject.focus();
            return;
        }

        if (message.isEmpty()) {
            showErrorNotification("Iltimos, xabar matnini kiriting!");
            txtMessage.focus();
            return;
        }

        // All fields valid -> show success notification & clear form
        Notification successNotif = Notification.show("Xabaringiz muvaffaqiyatli yuborildi!", 4000, Notification.Position.BOTTOM_END);
        successNotif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Reset form inputs
        txtName.clear();
        txtEmail.clear();
        selectSubject.clear();
        txtMessage.clear();
    }

    private void showErrorNotification(String text) {
        Notification notif = Notification.show(text, 3500, Notification.Position.BOTTOM_END);
        notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private Div createContactInfoItem(String iconEmoji, String badgeColor, String labelText, String valueText) {
        Div item = new Div();
        item.addClassName("contact-info-item");

        Span iconBadge = new Span(iconEmoji);
        iconBadge.addClassName("contact-info-badge");
        iconBadge.addClassName(badgeColor);

        Div details = new Div();
        details.addClassName("contact-info-details");

        Span label = new Span(labelText);
        label.addClassName("contact-info-label");

        Span value = new Span(valueText);
        value.addClassName("contact-info-value");

        details.add(label, value);
        item.add(iconBadge, details);
        return item;
    }
}
