package teamwork.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TranslationProvider implements I18NProvider {
    @Override
    public List<Locale> getProvidedLocales() {
        return I18n.LANGUAGES.stream().map(Locale::forLanguageTag).toList();
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        String value = I18n.translate(key, locale.getLanguage());
        for (int i = 0; i < params.length; i++) value = value.replace("{" + i + "}", String.valueOf(params[i]));
        return value;
    }
}
