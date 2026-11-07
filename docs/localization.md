TozaCoin supports Uzbek (`uz`, the default), English (`en`), and Russian (`ru`).
The selected language is stored in the `toza-language` cookie and restored when a UI starts.

`teamwork.i18n.TranslationProvider` integrates the catalog with Vaadin. `I18n.localize(component)`
binds display text to locale changes, while `I18n.text`, `I18n.label`, and `I18n.placeholder`
handle later updates. These bindings preserve field values and existing child components.
Bindings are removed from the UI registry when components detach.

Add copy to `src/main/resources/i18n/messages.json`. Each entry uses the original Uzbek
text as its key and an array of English and Russian translations. An optional third item
provides Uzbek text for legacy English source keys. Use `{0}`, `{1}`, etc. for dynamic
values. Browser-tab titles use `HasDynamicTitle`; validation messages use lazy providers
so newly displayed errors follow the current locale.

Keep domain values and user data unchanged. For selects, use `I18n::t` as the item-label
generator instead of translating the stored values. JavaScript-owned camera and map
messages use `components/localization.js` and the same catalog published by the server.
Use `I18n.userText(component, value, fallbackKey)` for account names, report descriptions,
and other user-authored text; only its empty-state fallback is translated.

Run `mvnw.cmd test` for catalog and regression tests. Browser checks should cover live
switching with a partially completed form, translated validation, navigation and refresh,
and header spacing at 320px, 390px, 768px, and desktop widths.
