package teamwork.i18n;

import com.vaadin.flow.server.*;
import org.springframework.stereotype.Component;

@Component
public class LocalizationInit implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(init -> {
            String language = "uz";
            var cookies = VaadinService.getCurrentRequest().getCookies();
            if (cookies != null)
                for (var cookie : cookies) if (I18n.COOKIE.equals(cookie.getName())) language = cookie.getValue();
            I18n.initialize(init.getUI(), language);
        });
    }
}
