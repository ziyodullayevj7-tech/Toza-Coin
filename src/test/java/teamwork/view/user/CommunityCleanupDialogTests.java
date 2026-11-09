package teamwork.view.user;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import teamwork.dto.attach.AttachDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.service.*;
import teamwork.util.SecurityUtils;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunityCleanupDialogTests {
    private UI ui;
    private final WasteReportService reports=mock(WasteReportService.class);
    private final AttachService attachments=mock(AttachService.class);
    private final List<CommunityCleanupTask> changes=new ArrayList<>();
    private MockedStatic<SecurityUtils> security;
    private boolean completed;
    @BeforeEach void setup(){
        ui=new UI();UI.setCurrent(ui);ui.setLocale(java.util.Locale.forLanguageTag("uz"));
        security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::getCurrentUserId).thenReturn("cleaner");
    }
    @AfterEach void cleanup(){security.close();UI.setCurrent(null);}

    @Test void startClaimsOnlyOnceAndEnablesFinalPhotoStep(){
        when(reports.claimReportForCleaning("report")).thenReturn(true);
        var dialog=dialog(task(0,null,"reporter"));dialog.open();
        Button start=button(dialog,"cleanup-start");start.click();start.click();
        verify(reports,times(1)).claimReportForCleaning("report");
        assertTrue(changes.getFirst().startedAt()>0);
        assertFalse(button(dialog,"cleanup-submit").isEnabled());
        assertNotNull(button(dialog,"cleanup-open-camera"));
        assertFalse(completed);
    }

    @Test void finalCameraPhotoAndDurationAreSubmittedOnceToModeration(){
        var attachment=new AttachDto();attachment.setPath("/attaches/after.jpg");
        when(attachments.uploadBase64CameraPhoto(anyString())).thenReturn(attachment);
        when(reports.updateReportCleaner(any())).thenReturn(true);
        var dialog=dialog(task(System.currentTimeMillis()-120_000,null,"reporter"));dialog.open();
        dialog.acceptPhoto("data:image/jpeg;base64,example");
        assertEquals("/attaches/after.jpg",changes.getFirst().afterImageUrl());
        Button submit=button(dialog,"cleanup-submit");submit.click();submit.click();
        verify(reports,times(1)).updateReportCleaner(argThat(dto->"report".equals(dto.getWasteReportId())
                && "/attaches/after.jpg".equals(dto.getCleanedImageUrl()) && dto.getCleanPeriod()>=2));
        assertTrue(completed);assertFalse(dialog.isOpened());
    }

    @Test void ownReportCannotBeClaimedAndConcurrentClaimFailureDoesNotStartCleaning(){
        var own=dialog(task(0,null,"cleaner"));own.open();assertFalse(button(own,"cleanup-start").isEnabled());own.close();
        when(reports.claimReportForCleaning("report")).thenThrow(new AppBadRequestException("Already claimed"));
        var other=dialog(task(0,null,"reporter"));other.open();button(other,"cleanup-start").click();
        assertTrue(changes.isEmpty());assertFalse(completed);
        verify(reports,never()).updateReportCleaner(any());
    }

    @Test void resumingAndClosingAnAssignmentDoesNotSubmitOrReleaseIt(){
        var dialog=dialog(task(System.currentTimeMillis()-60_000,"/attaches/after.jpg","reporter"));dialog.open();
        assertTrue(button(dialog,"cleanup-submit").isEnabled());
        buttons(dialog).filter(b->"Keyinroq davom etish".equals(b.getText())).findFirst().orElseThrow().click();
        verifyNoInteractions(reports);assertFalse(completed);
    }

    @Test void directionsProvideNativeAppAndBrowserFallback(){
        var links=CommunityCleanupDialog.directions(task(0,null,"reporter"));
        var anchors=links.getChildren().filter(Anchor.class::isInstance).map(Anchor.class::cast).toList();
        assertTrue(anchors.get(0).getHref().startsWith("geo:41.3,69.2"));
        assertTrue(anchors.get(1).getHref().contains("destination=41.3,69.2"));
        assertEquals("_blank",anchors.get(1).getTarget().orElseThrow());
    }

    private CommunityCleanupDialog dialog(CommunityCleanupTask task){return new CommunityCleanupDialog(task,reports,attachments,(updated,done)->{changes.add(updated);completed=done;});}
    private CommunityCleanupTask task(long start,String photo,String reporter){return new CommunityCleanupTask("report","Plastik","Chilonzor","before.jpg",41.3,69.2,reporter,50,start,photo);}
    private Button button(Component root,String id){return buttons(root).filter(b->b.getId().orElse("").equals(id)).findFirst().orElseThrow();}
    private Stream<Button> buttons(Component root){return descendants(root).filter(Button.class::isInstance).map(Button.class::cast);}
    private Stream<Component> descendants(Component root){
        Stream<Component> children=root.getChildren();
        if(root instanceof com.vaadin.flow.component.dialog.Dialog dialog)children=Stream.concat(children,dialog.getFooter().getElement().getChildren().flatMap(e->e.getComponent().stream()));
        return children.flatMap(c->Stream.concat(Stream.of(c),descendants(c)));
    }
}
