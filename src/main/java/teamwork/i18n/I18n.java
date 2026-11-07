package teamwork.i18n;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.i18n.*;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.json.JsonMapper;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;

/**
 * Source-keyed translations and locale-aware bindings for existing Flow views. Never binds field values.
 */
public final class I18n {
    public static final String COOKIE = "toza-language";
    public static final List<String> LANGUAGES = List.of("uz", "en", "ru");
    private static final Map<String, String[]> MESSAGES = load();
    private static final Map<String, String> SOURCES = sources();
    private static final List<Template> TEMPLATES = templates();

    private I18n() {
    }

    private static Map<String, String[]> load() {
        try (var input = I18n.class.getResourceAsStream("/i18n/messages.json")) {
            var root = new JsonMapper().readTree(input);
            Map<String, String[]> result = new LinkedHashMap<>();
            root.properties().forEach(entry -> result.put(entry.getKey(),
                    new String[]{entry.getValue().size() > 2 ? entry.getValue().get(2).asString() : entry.getKey(),
                            entry.getValue().get(0).asString(), entry.getValue().get(1).asString()}));
            return Collections.unmodifiableMap(result);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load translations", e);
        }
    }

    private static Map<String, String> sources() {
        Map<String, String> result = new HashMap<>();
        MESSAGES.forEach((key, values) -> {
            for (String value : values) result.putIfAbsent(value, key);
        });
        MESSAGES.keySet().forEach(key -> result.put(key, key));
        return result;
    }

    private record Template(Pattern regex, String[] translations) {
    }

    private static List<Template> templates() {
        List<Template> result = new ArrayList<>();
        MESSAGES.values().stream().filter(values -> values[0].contains("{0}"))
                .sorted(Comparator.comparingInt((String[] values) -> values[0].length()).reversed())
                .forEach(values -> {
                    for (String value : values) {
                        String regex = Pattern.quote(value);
                        for (int i = 0; i < 8; i++) regex = regex.replace("{" + i + "}", "\\E(.*?)\\Q");
                        result.add(new Template(Pattern.compile("^" + regex + "$", Pattern.DOTALL), values));
                    }
                });
        return result;
    }

    public static String language() {
        return UI.getCurrent() == null ? "uz" : normalize(UI.getCurrent().getLocale().getLanguage());
    }

    public static String normalize(String language) {
        return LANGUAGES.contains(language) ? language : "uz";
    }

    public static String t(String source) {
        return translate(source, language());
    }

    public static String translate(String source, String language) {
        if (source == null || source.isEmpty()) return source;
        int index = LANGUAGES.indexOf(normalize(language));
        String key = SOURCES.get(source);
        if (key != null) return MESSAGES.get(key)[index];
        for (Template template : TEMPLATES) {
            Matcher match = template.regex.matcher(source);
            if (match.matches()) {
                String result = template.translations[index];
                for (int i = 1; i <= match.groupCount(); i++)
                    result = result.replace("{" + (i - 1) + "}", match.group(i));
                return result;
            }
        }
        if (source.endsWith(" | TozaCoin"))
            return translate(source.substring(0, source.length() - 11), language) + " | TozaCoin";
        Matcher decorated = Pattern.compile("^([\\s\\p{So}\\p{Sk}\\p{Sm}\\p{Mn}]*)(.*?)([\\s\\p{So}\\p{Sk}\\p{Sm}\\p{Mn}]*)$", Pattern.DOTALL).matcher(source);
        if (decorated.matches()) {
            key = SOURCES.get(decorated.group(2));
            if (key != null) return decorated.group(1) + MESSAGES.get(key)[index] + decorated.group(3);
        }
        return source;
    }

    public static <T extends Component> T localize(T component) {
        String text = component.getElement().getText();
        if (!text.isBlank() && (!translate(text, "ru").equals(text)
                || !translate(text, "en").equals(text) || !translate(text, "uz").equals(text))) {
            text(component, text);
        }
        for (String property : List.of("label", "placeholder", "helperText", "errorMessage", "headerTitle", "title", "alt")) {
            String value = component.getElement().getProperty(property, "");
            if (!value.isBlank()) property(component, property, value);
        }
        return component;
    }

    public static void text(Component component, String source) {
        bind(component, "text", source, value -> {
            var nodes = component.getElement().getChildren().filter(Element::isTextNode).toList();
            if (nodes.isEmpty()) component.getElement().appendChild(Element.createText(value));
            else {
                nodes.getFirst().setText(value);
                for (int i = 1; i < nodes.size(); i++) nodes.get(i).removeFromParent();
            }
        });
    }

    /** User-authored text is never interpreted as a catalog key or a translation template. */
    public static <T extends Component> T userText(T component, String value, String fallback) {
        if (value == null || value.isBlank()) text(component, fallback);
        else dataText(component, value);
        return component;
    }

    public static void dataText(Component component, String value) {
        State state = ComponentUtil.getData(component, State.class);
        if (state != null) state.bindings.remove("text");
        var nodes = component.getElement().getChildren().filter(Element::isTextNode).toList();
        if (nodes.isEmpty()) component.getElement().appendChild(Element.createText(value));
        else {
            nodes.getFirst().setText(value);
            for (int i = 1; i < nodes.size(); i++) nodes.get(i).removeFromParent();
        }
    }

    public static void placeholder(Component component, String source) {
        property(component, "placeholder", source);
    }

    public static void label(Component component, String source) {
        property(component, "label", source);
    }

    public static void title(Component component, String source) {
        property(component, "title", source);
    }

    public static void headerTitle(Dialog component, String source) {
        bind(component, "headerTitle", source, component::setHeaderTitle);
    }

    public static void property(Component component, String property, String source) {
        bind(component, property, source, value -> component.getElement().setProperty(property, value));
    }

    public static void attribute(Component component, String name, String source) {
        bind(component, "attr:" + name, source, value -> component.getElement().setAttribute(name, value));
    }

    private static void bind(Component component, String property, String source, SerializableConsumer<String> setter) {
        State state = ComponentUtil.getData(component, State.class);
        if (state == null) {
            state = new State();
            ComponentUtil.setData(component, State.class, state);
            State captured = state;
            component.addAttachListener(event -> {
                controller(event.getUI()).states.put(component, captured);
                captured.refresh();
                nativeMessages(component);
            });
            component.addDetachListener(event -> controller(event.getUI()).states.remove(component));
        }
        state.bindings.put(property, new Binding(source, setter));
        setter.accept(t(source));
        if (component.isAttached()) controller(component.getUI().orElseThrow()).states.put(component, state);
    }

    private record Binding(String source, SerializableConsumer<String> setter) implements Serializable {
        void refresh() {
            setter.accept(t(source));
        }
    }

    private static class State implements Serializable {
        final Map<String, Binding> bindings = new LinkedHashMap<>();

        void refresh() {
            new ArrayList<>(bindings.values()).forEach(Binding::refresh);
        }
    }

    private static class Controller extends Div implements LocaleChangeObserver {
        final Map<Component, State> states = new LinkedHashMap<>();
        String pageTitle = "TozaCoin";

        Controller() {
            getStyle().set("display", "none");
            getElement().setAttribute("aria-hidden", "true");
        }

        @Override
        public void localeChange(LocaleChangeEvent event) {
            new ArrayList<>(states.entrySet()).forEach(entry -> {
                entry.getValue().refresh();
                nativeMessages(entry.getKey());
            });
            UI ui = event.getUI();
            ui.getPage().setTitle(t(pageTitle));
            browserLocale(ui);
        }
    }

    private static Controller controller(UI ui) {
        Controller result = ComponentUtil.getData(ui, Controller.class);
        if (result == null) {
            result = new Controller();
            ComponentUtil.setData(ui, Controller.class, result);
            ui.add(result);
        }
        return result;
    }

    public static void initialize(UI ui, String language) {
        ui.setLocale(Locale.forLanguageTag(normalize(language)));
        controller(ui);
        ui.addAfterNavigationListener(event -> {
            var title = ui.getInternals().getActiveRouterTargetsChain().stream()
                    .map(component -> component.getClass().getAnnotation(com.vaadin.flow.router.PageTitle.class))
                    .filter(Objects::nonNull).findFirst().orElse(null);
            if (title != null) controller(ui).pageTitle = title.value();
            ui.getInternals().getActiveRouterTargetsChain().stream()
                    .filter(component -> component instanceof com.vaadin.flow.router.HasDynamicTitle)
                    .findFirst().ifPresent(component -> controller(ui).pageTitle = ((com.vaadin.flow.router.HasDynamicTitle) component).getPageTitle());
            ui.getPage().setTitle(t(controller(ui).pageTitle));
            browserLocale(ui);
        });
    }

    public static void select(UI ui, String language) {
        String code = normalize(language);
        Cookie cookie = new Cookie(COOKIE, code);
        cookie.setPath("/");
        cookie.setMaxAge(31536000);
        cookie.setAttribute("SameSite", "Lax");
        if (VaadinService.getCurrentResponse() != null) VaadinService.getCurrentResponse().addCookie(cookie);
        ui.setLocale(Locale.forLanguageTag(code));
    }

    private static void browserLocale(UI ui) {
        try {
            Map<String, String> catalog = new LinkedHashMap<>();
            MESSAGES.keySet().forEach(key -> catalog.put(key, t(key)));
            ui.getPage().executeJs("document.documentElement.lang=$0;window.tozaMessages=JSON.parse($1);window.dispatchEvent(new CustomEvent('toza-language-changed'));", language(), new JsonMapper().writeValueAsString(catalog));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot publish UI locale", e);
        }
    }

    private static void nativeMessages(Component component) {
        if (component instanceof HasValidation validation && validation.getErrorMessage() != null && !validation.getErrorMessage().isBlank()) {
            validation.setErrorMessage(t(validation.getErrorMessage()));
        }
        if (component instanceof com.vaadin.flow.component.textfield.PasswordField field) {
            field.getElement().executeJs("this.i18n = {...this.i18n, reveal: $0, hide: $1};",
                    t("Parolni ko'rsatish"), t("Parolni yashirish"));
        }
        if (component instanceof com.vaadin.flow.component.textfield.EmailField field) {
            field.setI18n(new com.vaadin.flow.component.textfield.EmailField.EmailFieldI18n()
                    .setRequiredErrorMessage(t("Email manzilingizni kiriting."))
                    .setPatternErrorMessage(t("To'g'ri email manzilini kiriting.")));
        }
        if (component instanceof com.vaadin.flow.component.combobox.ComboBox<?> combo) refreshCombo(combo);
        if (component instanceof com.vaadin.flow.component.select.Select<?> select) refreshSelect(select);
    }

    private static <T> void refreshCombo(com.vaadin.flow.component.combobox.ComboBox<T> combo) {
        // Refresh the selected item's caption as well as the dropdown, without changing its value.
        combo.setItemLabelGenerator(combo.getItemLabelGenerator());
    }

    private static <T> void refreshSelect(com.vaadin.flow.component.select.Select<T> select) {
        select.setItemLabelGenerator(select.getItemLabelGenerator());
    }

    public static Notification notify(String message) {
        Notification notification = Notification.show("");
        notification.add(localize(new com.vaadin.flow.component.html.Span(message)));
        return notification;
    }

    public static Notification notify(String message, int duration, Notification.Position position) {
        Notification notification = Notification.show("", duration, position);
        notification.add(localize(new com.vaadin.flow.component.html.Span(message)));
        return notification;
    }

    public static String format(String format, Object... args) {
        return t(String.format(format, args));
    }

    public static String format(Locale locale, String format, Object... args) {
        return t(String.format(locale, format, args));
    }
}
