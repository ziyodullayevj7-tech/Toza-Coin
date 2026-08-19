package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

@CssImport("./themes/tozacoin/review.css")
public class ReviewSection extends Div {

    private final Div grid = new Div();

    public ReviewSection() {
        addClassName("review-wrapper");

        Div container = new Div();
        container.addClassName("review-container");

        // Header Title Block
        Div headerBlock = new Div();
        headerBlock.addClassName("review-header");

        Span badge = new Span("FIKRLAR");
        badge.addClassName("review-badge");

        H2 title = new H2("Ko'ngillilar nima deyishadi");
        title.addClassName("review-title");

        headerBlock.add(badge, title);

        // 3 Cards Grid Container
        grid.addClassName("review-grid");

        container.add(headerBlock, grid);
        add(container);

        // Load Default Sample Reviews
        setReviews(getDefaultSampleReviews());
    }

    // Public method for loading dynamic Review data from a Service
    public void setReviews(List<ReviewItem> reviews) {
        grid.removeAll();
        if (reviews == null || reviews.isEmpty()) {
            return;
        }

        for (ReviewItem review : reviews) {
            grid.add(createReviewCard(review));
        }
    }

    private Div createReviewCard(ReviewItem item) {
        Div card = new Div();
        card.addClassName("review-card");

        // Stars Rating
        Div stars = new Div();
        stars.setText("★★★★★");
        stars.addClassName("review-stars");

        // Quote
        Paragraph quote = new Paragraph(item.getQuoteText());
        quote.addClassName("review-quote");

        // User Footer Row
        Div userRow = new Div();
        userRow.addClassName("review-user-row");

        Div userLeft = new Div();
        userLeft.addClassName("review-user-left");

        Div avatar = new Div();
        avatar.setText(item.getInitials());
        avatar.addClassName("review-user-avatar");
        avatar.addClassName(item.getAvatarColor());

        Div userInfo = new Div();
        userInfo.addClassName("review-user-info");

        Span name = new Span(item.getName());
        name.addClassName("review-user-name");

        Span city = new Span(item.getCity());
        city.addClassName("review-user-city");

        userInfo.add(name, city);
        userLeft.add(avatar, userInfo);

        Span coins = new Span();
        coins.addClassName("review-coins-badge");
        coins.add(new Span("🪙"), new Span(item.getCoinsEarned()));

        userRow.add(userLeft, coins);

        card.add(stars, quote, userRow);
        return card;
    }

    private List<ReviewItem> getDefaultSampleReviews() {
        List<ReviewItem> list = new ArrayList<>();
        list.add(new ReviewItem("1", "DA", "Dilnoza Abdullayeva", "Toshkent", "\"TozaCoin meni mahallam tozaligiga e'tibor berishga undadi. Har tozalaganimda tanga olaman va bu juda qoniqarli!\"", "1240", "green"));
        list.add(new ReviewItem("2", "SM", "Sardor Mirzayev", "Samarqand", "\"Ilgari ko'chada chiqindi ko'rsam befarq o'tar edim. Endi suratga olib xabar beraman. Oddiy harakat katta o'zgarish qiladi.\"", "980", "blue"));
        list.add(new ReviewItem("3", "GR", "Gulnora Rahimova", "Farg'ona", "\"Bolalarimga ham o'rgatdim. Ular maktabdan qaytayotganda chiqindi ko'rsalar suratga olishadi. Biz birga 2000 tanga yig'dik!\"", "2000", "yellow"));
        return list;
    }

    // Service DTO Model for Review Items
    public static class ReviewItem {
        private String id;
        private String initials;
        private String name;
        private String city;
        private String quoteText;
        private String coinsEarned;
        private String avatarColor;

        public ReviewItem() {}

        public ReviewItem(String id, String initials, String name, String city, String quoteText, String coinsEarned, String avatarColor) {
            this.id = id;
            this.initials = initials;
            this.name = name;
            this.city = city;
            this.quoteText = quoteText;
            this.coinsEarned = coinsEarned;
            this.avatarColor = avatarColor;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getInitials() { return initials; }
        public void setInitials(String initials) { this.initials = initials; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getQuoteText() { return quoteText; }
        public void setQuoteText(String quoteText) { this.quoteText = quoteText; }

        public String getCoinsEarned() { return coinsEarned; }
        public void setCoinsEarned(String coinsEarned) { this.coinsEarned = coinsEarned; }

        public String getAvatarColor() { return avatarColor; }
        public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }
    }
}
