package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "waste-category", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/waste-category.css")
public class WasteCategorySection extends Div {

    private final Div grid = new Div();
    private final Map<String, Div> categoryCardsMap = new HashMap<>();

    public WasteCategorySection() {
        addClassName("waste-category-wrapper");

        Div container = new Div();
        container.addClassName("waste-category-container");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("waste-category-header");

        Span badge = new Span("TOIFALAR");
        badge.addClassName("waste-category-badge");

        H2 title = new H2("Ifloslanish turlari");
        title.addClassName("waste-category-title");

        headerBlock.add(badge, title);

        // 6 Cards Grid Container
        grid.addClassName("waste-category-grid");

        container.add(headerBlock, grid);
        add(container);

        // Load Default Categories
        setCategories(getDefaultSampleCategories());
    }

    // Dynamic method for loading Categories from a Service
    public void setCategories(List<CategoryItem> categories) {
        grid.removeAll();
        categoryCardsMap.clear();

        if (categories == null || categories.isEmpty()) {
            return;
        }

        for (CategoryItem cat : categories) {
            grid.add(createCategoryCard(cat));
        }
    }

    // ClickListener support for each Category Card
    public void addCategoryClickListener(String categoryId, ComponentEventListener<ClickEvent<Div>> listener) {
        Div card = categoryCardsMap.get(categoryId);
        if (card != null) {
            card.addClickListener(listener);
        }
    }

    private Div createCategoryCard(CategoryItem item) {
        Div card = new Div();
        card.addClassName("category-card");

        Span iconBadge = new Span(item.getIconEmoji());
        iconBadge.addClassName("category-icon-badge");
        iconBadge.addClassName(item.getBadgeColor());

        Span label = new Span(item.getTitle());
        label.addClassName("category-card-label");

        card.add(iconBadge, label);
        categoryCardsMap.put(item.getId(), card);

        return card;
    }

    private List<CategoryItem> getDefaultSampleCategories() {
        List<CategoryItem> list = new ArrayList<>();
        list.add(new CategoryItem("1", "Plastik chiqindi", "🧴", "pink"));
        list.add(new CategoryItem("2", "Noqonuniy tashlash", "🚯", "red"));
        list.add(new CategoryItem("3", "Suv ifloslanishi", "💧", "blue"));
        list.add(new CategoryItem("4", "Havo ifloslanishi", "🌫️", "grey"));
        list.add(new CategoryItem("5", "Elektron chiqindi", "💻", "purple"));
        list.add(new CategoryItem("6", "Jamoat ifloslanishi", "🏙️", "yellow"));
        return list;
    }

    // Service DTO Model for Category Items
    public static class CategoryItem {
        private String id;
        private String title;
        private String iconEmoji;
        private String badgeColor;

        public CategoryItem() {}

        public CategoryItem(String id, String title, String iconEmoji, String badgeColor) {
            this.id = id;
            this.title = title;
            this.iconEmoji = iconEmoji;
            this.badgeColor = badgeColor;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getIconEmoji() { return iconEmoji; }
        public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

        public String getBadgeColor() { return badgeColor; }
        public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }
    }
}
