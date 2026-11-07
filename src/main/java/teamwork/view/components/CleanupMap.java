package teamwork.view.components;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import teamwork.dto.liveWasteMap.LiveWasteMapDto;

import java.util.List;

/**
 * Public, read-only map; its data is already limited to available cleanups by MapService.
 */
@Tag("toza-cleanup-map")
@JsModule("./components/cleanup-map.js")
public class CleanupMap extends Component {
    public CleanupMap(List<LiveWasteMapDto> points) {
        getElement().setAttribute("aria-label", "Tozalash uchun berilgan joylar xaritasi");
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (var point : points) {
            var loc = point.getLocation();
            if (loc == null) continue;
            double lat = loc.getLatitude(), lng = loc.getLongitude();
            if (!Double.isFinite(lat) || !Double.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180 || (lat == 0 && lng == 0))
                continue;
            if (!first) json.append(',');
            first = false;
            json.append("{\"lat\":").append(lat).append(",\"lng\":").append(lng)
                    .append(",\"address\":\"").append(escape(String.join(", ",
                            text(loc.getDistrict()), text(loc.getStreetAddress())))).append("\"}");
        }
        getElement().setProperty("markers", json.append(']').toString());
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == '"' || c == '\\') result.append('\\').append(c);
            else if (c < 32) result.append(I18n.format("\\u%04x", (int) c));
            else result.append(c);
        }
        return result.toString();
    }
}
