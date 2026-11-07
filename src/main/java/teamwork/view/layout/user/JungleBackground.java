package teamwork.view.layout.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * One scene per authenticated layout; its client component owns playback and cleanup.
 */
@Tag("toza-jungle-background")
@JsModule("./components/jungle-background.js")
public class JungleBackground extends Component {
}
