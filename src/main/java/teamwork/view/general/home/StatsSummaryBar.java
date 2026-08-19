package teamwork.view.general.home;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/stats-summary.css")
public class StatsSummaryBar extends Div {

    public StatsSummaryBar() {
        addClassName("stats-summary-wrapper");

        Div container = new Div();
        container.addClassName("stats-summary-container");

        container.add(
            createStatItem("👥", "12,847", "Faol foydalanuvchilar"),
            createStatItem("📍", "89,320", "Yuborilgan xabarlar"),
            createStatItem("♻️", "247,500", "Tozalangan chiqindi (kg)"),
            createStatItem("🏆", "1,240", "Yakunlangan kampaniyalar"),
            createStatItem("🪙", "5.2M", "Tarqatilgan tanga")
        );

        add(container);
    }

    private Div createStatItem(String icon, String number, String label) {
        Div item = new Div();
        item.addClassName("stats-summary-item");

        Span iconSpan = new Span(icon);
        iconSpan.addClassName("stats-summary-icon");

        Span numberSpan = new Span(number);
        numberSpan.addClassName("stats-summary-number");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stats-summary-label");

        item.add(iconSpan, numberSpan, labelSpan);
        return item;
    }
}
