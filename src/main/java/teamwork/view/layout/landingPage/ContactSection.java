package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
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

    private final TextField txtName = I18n.localize(new TextField("Ism familiya"));
    private final EmailField txtEmail = I18n.localize(new EmailField("Email"));
    private final Select<String> selectSubject = I18n.localize(new Select<>());
    private final TextArea txtMessage = I18n.localize(new TextArea("Xabar"));
    private final Button btnSubmit = I18n.localize(new Button("Xabar yuborish", VaadinIcon.PAPERPLANE.create()));

    public ContactSection() {
        addClassName("contact-wrapper");
        setId("contact");

        Div container = I18n.localize(new Div());
        container.addClassName("contact-container");

        // Left Column (Info Block)
        Div leftCol = I18n.localize(new Div());
        leftCol.addClassName("contact-left");

        Span badge = I18n.localize(new Span("ALOQA"));
        badge.addClassName("contact-badge");

        H2 title = I18n.localize(new H2("Biz bilan bog'laning"));
        title.addClassName("contact-title");

        Paragraph subtitle = I18n.localize(new Paragraph("Savol, taklif yoki hamkorlik takliflari bo'lsa — yozing. 1–2 ish kuni ichida javob beramiz."));
        subtitle.addClassName("contact-subtitle");

        Div infoList = I18n.localize(new Div());
        infoList.addClassName("contact-info-list");

        infoList.add(
                createContactInfoItem("email", "Email", "ziyodullayevj7@gmail.com",
                        "mailto:ziyodullayevj7@gmail.com"),
                createContactInfoItem("telegram", "Telegram", "@jahon_o7", "https://t.me/jahon_o7"),
                createContactInfoItem("location", "Manzil", "Toshkent shahar, Mirzo Ulugbek, Chust 17",
                        "https://www.google.com/maps/search/?api=1&query=Toshkent%20shahar%2C%20Mirzo%20Ulugbek%2C%20Chust%2017"),
                createContactInfoItem("instagram", "Instagram", "@ziyodullaev.j",
                        "https://www.instagram.com/ziyodullaev.j/")
        );

        leftCol.add(badge, title, subtitle, infoList);

        // Right Column (Form Card)
        Div formCard = I18n.localize(new Div());
        formCard.addClassName("contact-form-card");

        H3 formTitle = I18n.localize(new H3("Xabar yuborish"));
        formTitle.addClassName("contact-form-title");

        I18n.placeholder(txtName, "Jasur Nazarov");
        I18n.placeholder(txtEmail, "jasur@example.com");

        Div row1 = I18n.localize(new Div());
        row1.addClassName("contact-form-row");
        row1.add(txtName, txtEmail);

        I18n.label(selectSubject, "Mavzu");
        I18n.placeholder(selectSubject, "Tanlang...");
        selectSubject.setItemLabelGenerator(I18n::t);
        selectSubject.setItems("Umumiy savol", "Taklif", "Hamkorlik", "Muammo haqida xabar");

        I18n.placeholder(txtMessage, "Savolingiz yoki taklifingizni yozing...");
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
        Notification successNotif = I18n.notify("Xabaringiz muvaffaqiyatli yuborildi!", 4000, Notification.Position.BOTTOM_END);
        successNotif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Reset form inputs
        txtName.clear();
        txtEmail.clear();
        selectSubject.clear();
        txtMessage.clear();
    }

    private void showErrorNotification(String text) {
        Notification notif = I18n.notify(text, 3500, Notification.Position.BOTTOM_END);
        notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private Anchor createContactInfoItem(String iconName, String labelText, String valueText, String href) {
        Anchor item = new Anchor(href, "");
        item.addClassName("contact-info-item");
        // These are external destinations, never client-side application routes.
        item.getElement().setAttribute("router-ignore", "");
        if (href.startsWith("https://")) {
            item.setTarget("_blank");
            item.getElement().setAttribute("rel", "noopener noreferrer");
        }

        Image icon = new Image("icons/contact/" + iconName + ".svg", "");
        icon.setWidth("22px");
        icon.setHeight("22px");
        icon.getElement().setAttribute("aria-hidden", "true");
        Span iconBadge = new Span(icon);
        iconBadge.addClassNames("contact-info-badge", "contact-icon-" + iconName);

        Div details = new Div();
        details.addClassName("contact-info-details");
        Span label = I18n.localize(new Span(labelText));
        label.addClassName("contact-info-label");
        Span value = new Span();
        I18n.dataText(value, valueText);
        value.addClassName("contact-info-value");
        details.add(label, value);
        item.add(iconBadge, details);
        return item;
    }
}
