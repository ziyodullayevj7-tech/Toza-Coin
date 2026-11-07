package teamwork.TozaCoin;

import org.junit.jupiter.api.Test;
import teamwork.i18n.I18n;
import teamwork.i18n.TranslationProvider;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class TranslationTests {
    @Test
    void translatesAllThreeLocalesAndFallsBackToUzbek() {
        assertEquals("Kirish", I18n.translate("Kirish", "uz"));
        assertEquals("Sign in", I18n.translate("Kirish", "en"));
        assertEquals("Войти", I18n.translate("Kirish", "ru"));
        assertEquals("Kirish", I18n.translate("Kirish", "invalid"));
    }

    @Test
    void preservesNamesAndOtherUserContent() {
        assertEquals("Jahongir Ziyodullayev", I18n.translate("Jahongir Ziyodullayev", "ru"));
        assertEquals("hello@example.com", I18n.translate("hello@example.com", "en"));
        var description = I18n.userText(new com.vaadin.flow.component.html.Paragraph(), "Place a bag here", "Xabar");
        assertEquals("Place a bag here", description.getText());
        var name = I18n.userText(new com.vaadin.flow.component.html.Span(), "Report", "Foydalanuvchi");
        assertEquals("Report", name.getText());
    }

    @Test
    void preservesDynamicValuesAndTranslatesPreviouslyRenderedText() {
        assertEquals("Hello, Alice!", I18n.translate("Salom, Alice!", "en"));
        assertEquals("Привет, Alice!", I18n.translate("Hello, Alice!", "ru"));
        assertEquals("Page 2 / 5 · 24 reports", I18n.translate("Sahifa 2 / 5 · 24 ta xabar", "en"));
        assertEquals("Шаг 3 из 5", I18n.translate("Qadam 3 / 5", "ru"));
    }

    @Test
    void preservesDecorationAndPageTitles() {
        assertEquals("← Previous", I18n.translate("← Avvalgi", "en"));
        assertEquals("Войти | TozaCoin", I18n.translate("Kirish | TozaCoin", "ru"));
        assertEquals("Подтвердите пароль", I18n.translate("Parolni tasdiqlash", "ru"));
        assertEquals("📷 Take a photo", I18n.translate("📷 Kamera orqali rasm olish", "en"));
        assertEquals("Photo captured successfully! ✅", I18n.translate("Surat muvaffaqiyatli olindi! ✅", "en"));
        assertEquals("Xatolik yuz berdi.", I18n.translate("Something went wrong", "uz"));
    }

    @Test
    void providerSupportsAllRequestedLanguages() {
        var provider = new TranslationProvider();
        assertEquals(3, provider.getProvidedLocales().size());
        assertEquals("Hello, Sam!", provider.getTranslation("Salom, {0}!", Locale.ENGLISH, "Sam"));
    }
}
