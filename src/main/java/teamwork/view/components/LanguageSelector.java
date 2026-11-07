package teamwork.view.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.i18n.*;
import teamwork.i18n.I18n;

@CssImport("./themes/tozacoin/language-selector.css")
@com.vaadin.flow.component.dependency.JsModule("./components/localization.js")
public class LanguageSelector extends Div implements LocaleChangeObserver {
    private final Select<String> select = new Select<>();

    public LanguageSelector() {
        addClassName("language-selector");
        select.setItems("uz", "en", "ru");
        select.setItemLabelGenerator(code -> switch (code) {
            case "en" -> "English";
            case "ru" -> "Русский";
            default -> "O'zbekcha";
        });
        select.setValue(I18n.language());
        select.setAriaLabel(I18n.t("Tilni tanlash"));
        select.addValueChangeListener(event -> {
            if (event.isFromClient()) I18n.select(getUI().orElseThrow(), event.getValue());
        });
        add(select);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        select.setValue(I18n.normalize(event.getLocale().getLanguage()));
        select.setAriaLabel(I18n.t("Tilni tanlash"));
    }
}
