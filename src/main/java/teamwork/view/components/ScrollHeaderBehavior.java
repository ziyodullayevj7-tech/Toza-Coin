package teamwork.view.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/** Client-side scrolling behavior, disposed when the containing header is detached. */
@Tag("toza-scroll-header")
@JsModule("./components/scroll-header.js")
public class ScrollHeaderBehavior extends Component {
}
