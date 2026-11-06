package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.service.MapService;
import teamwork.view.components.CleanupMap;
import teamwork.view.user.MapView;

@Route(value = "map-preview", layout = MainLayout.class)
@AnonymousAllowed
public class MapPreviewSection extends Div {
    public MapPreviewSection(MapService mapService) {
        setId("map");
        addClassNames("eco-section", "eco-map-section");
        Span label = I18n.localize(new Span("XARITA"));
        label.addClassName("eco-eyebrow");
        H2 title = I18n.localize(new H2("Yordam kerak bo'lgan joylar."));
        Paragraph description = I18n.localize(new Paragraph("Bu xaritada tekshiruvdan o'tgan va tozalash uchun berilgan joylar ko'rsatiladi. Nuqtani bosib manzilni ko'ring."));
        Div mapArea = I18n.localize(new Div());
        mapArea.addClassName("eco-public-map-area");
        try {
            var points = mapService.getLiveWasteForMap();
            mapArea.add(new CleanupMap(points));
            if (points.isEmpty()) {
                Span empty = I18n.localize(new Span("Hozircha tozalash uchun berilgan joylar yo'q."));
                empty.addClassName("eco-map-notice");
                mapArea.add(empty);
            }
        } catch (Exception ex) {
            Div error = I18n.localize(new Div(I18n.localize(new Paragraph("Xarita ma'lumotlarini yuklab bo'lmadi."))));
            com.vaadin.flow.component.button.Button retry = new com.vaadin.flow.component.button.Button("Qayta yuklash", e -> com.vaadin.flow.component.UI.getCurrent().getPage().reload());
            error.add(retry);
            error.addClassName("eco-map-error");
            mapArea.add(error);
        }
        RouterLink open = I18n.localize(new RouterLink("Xaritani ochish ↗", MapView.class));
        open.addClassName("eco-text-link");
        Div bottom = I18n.localize(new Div(I18n.localize(new Span("Faqat tozalanmagan, tozalash uchun mavjud joylar.")), open));
        bottom.addClassName("eco-map-bottom");
        add(label, title, description, mapArea, bottom);
    }
}
