package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value = "faq", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/faq.css")
public class FaqSection extends Div {

    private final Div accordionList = new Div();

    public FaqSection() {
        addClassName("faq-wrapper");

        Div container = new Div();
        container.addClassName("faq-container");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("faq-header");

        Span badge = new Span("SAVOL-JAVOB");
        badge.addClassName("faq-badge");

        H2 title = new H2("Ko'p so'raladigan savollar");
        title.addClassName("faq-title");

        headerBlock.add(badge, title);

        // Accordion Container
        accordionList.addClassName("faq-accordion-list");

        container.add(headerBlock, accordionList);
        add(container);

        // Load Default Questions (All Collapsed By Default)
        setFaqItems(getDefaultSampleFaqs());
    }

    // Public method to populate FAQ items dynamically from a backend Service
    public void setFaqItems(List<FaqItem> items) {
        accordionList.removeAll();
        if (items == null || items.isEmpty()) {
            return;
        }

        for (FaqItem item : items) {
            accordionList.add(createFaqItemCard(item, false));
        }
    }

    private Div createFaqItemCard(FaqItem item, boolean initiallyExpanded) {
        Div card = new Div();
        card.addClassName("faq-item-card");
        if (initiallyExpanded) {
            card.addClassName("expanded");
        }

        // Question Row Header
        Div headerRow = new Div();
        headerRow.addClassName("faq-item-header");

        H3 questionText = new H3(item.getQuestion());
        questionText.addClassName("faq-question-text");

        Span chevron = new Span("v");
        chevron.addClassName("faq-chevron");

        headerRow.add(questionText, chevron);

        // Answer Wrapper
        Div answerWrapper = new Div();
        answerWrapper.addClassName("faq-answer-wrapper");

        Div divider = new Div();
        divider.addClassName("faq-answer-divider");

        Paragraph answerText = new Paragraph(item.getAnswer());
        answerText.addClassName("faq-answer-text");

        answerWrapper.add(divider, answerText);

        card.add(headerRow, answerWrapper);

        // Toggle Expand/Collapse on Click
        card.addClickListener(event -> {
            if (card.hasClassName("expanded")) {
                card.removeClassName("expanded");
            } else {
                card.addClassName("expanded");
            }
        });

        return card;
    }

    private List<FaqItem> getDefaultSampleFaqs() {
        List<FaqItem> list = new ArrayList<>();
        list.add(new FaqItem("1", "TozaCoin qanday ishlaydi?", "TozaCoin platformasida siz ifloslangan hudud rasmini olib GPS joylashuvi bilan yuborasiz. Xabar tasdiqlangach, sizga raqamli tangalar beriladi."));
        list.add(new FaqItem("2", "Tangalarni qanday ishlatish mumkin?", "Yig'ilgan tangalarni katalogdagi internet-paketlar, transport kartalari, kino chiptalari va boshqa mukofotlarga almashtirishingiz mumkin."));
        list.add(new FaqItem("3", "Rasm nima uchun faqat kamerasidan olinadi?", "Soxta yoki internetdan olingan suratlarni oldini olish hamda aniq vaqt va geolokatsiyani tasdiqlash uchun foto faqat ilova kamerasi orqali olinadi."));
        list.add(new FaqItem("4", "Nima uchun o'z xabarimni o'zim tozalasam kam tanga olaman?", "Tizim adolat tamoyiliga asoslangan: boshqa fuqarolar yuborgan muammoni hal etish ko'proq rag'batlantiriladi va suiiste'mol qilishning oldi olinadi."));
        list.add(new FaqItem("5", "Xabar qanchalik tez tekshiriladi?", "AI tekshiruvi bir necha soniyada amalga oshiriladi. Keyin moderator 24–48 soat ichida yakuniy tasdiqlaydi. Favqulodda holatlarda tezroq ko'rib chiqiladi."));
        list.add(new FaqItem("6", "Kampaniyaga qo'shilish bepulmi?", "Ha, barcha tozalash kampaniyalari va tadbirlarda ishtirok etish to'liq bepul."));
        list.add(new FaqItem("7", "Ilova qaysi qurilmalarda ishlaydi?", "TozaCoin ilovasi Android va iOS smartfonlarida, shuningdek veb-brauzer orqali har qanday qurilmada ishlaydi."));
        list.add(new FaqItem("8", "Shaxsiya ma'lumotlarim xavfsizmi?", "Ha, barcha shaxsiy ma'lumotlar xavfsiz shifrlanadi va uchinchi shaxslarga berilmaydi."));
        return list;
    }

    // Service DTO Model for FAQ Items
    public static class FaqItem {
        private String id;
        private String question;
        private String answer;

        public FaqItem() {}

        public FaqItem(String id, String question, String answer) {
            this.id = id;
            this.question = question;
            this.answer = answer;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}
