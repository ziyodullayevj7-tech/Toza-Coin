package teamwork.view.layout;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

public class MainLayout extends VerticalLayout implements RouterLayout {

    private final Div content = new Div();

    public MainLayout() {
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setWidthFull();
        setHeightFull();
        getStyle().set("min-height", "100vh");

        content.setWidthFull();
        content.getStyle().set("flex", "1");

        add(new Header());
        add(content);
        add(new Footer());
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        this.content.removeAll();
        this.content.getElement().appendChild(content.getElement());
    }
}
