package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

@com.vaadin.flow.component.dependency.CssImport("./themes/tozacoin/trashout.css")
public class MainLayout extends VerticalLayout implements RouterLayout {

    private final Div content = I18n.localize(new Div());

    public MainLayout() {
        addClassName("eco-public-layout");
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setWidthFull();
        getStyle().set("min-height", "100vh");

        content.addClassName("eco-public-content");
        content.setWidthFull();
        content.getStyle().set("flex", "1");
        content.getStyle().set("padding-top", "80px"); // Offset for fixed top Header

        add(new Header());
        add(content);
        add(new Footer());
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content instanceof teamwork.view.general.LandingView) {
            addClassName("eco-has-hero");
        } else {
            removeClassName("eco-has-hero");
        }
        this.content.removeAll();
        this.content.getElement().appendChild(content.getElement());
    }
}
