package teamwork.TozaCoin;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.*;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import teamwork.dto.reward.*;
import teamwork.service.*;
import teamwork.view.admin.AdminRewardsView;
import teamwork.view.user.RewardsView;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardViewsTests {
    private UI ui;
    private final RewardService service = mock(RewardService.class);
    private final AuthService auth = mock(AuthService.class);
    private final RewardDto reward = RewardDto.builder().id("reward").title("Bus ticket")
            .description("One trip").providerName("Transit").coinPrice(30).quantity(3).isActive(true).build();

    @BeforeEach
    void setup() {
        ui = new UI();
        UI.setCurrent(ui);
        UI.getCurrent().setLocale(java.util.Locale.forLanguageTag("uz"));
        when(auth.getBalanceByUserId(any())).thenReturn(100);
        when(service.getRewardsPagForUsers(0, 9)).thenReturn(new PageImpl<>(List.of(reward)));
        when(service.getMyRewards(0, 9)).thenReturn(Page.empty());
        when(service.getRewardsPagForAdmin(0, 10)).thenReturn(new PageImpl<>(List.of(reward)));
    }

    @AfterEach
    void cleanup() { UI.setCurrent(null); }

    @Test
    void purchasePassesQuantityToServiceAndRefreshesBalance() {
        var view = new RewardsView(service, auth);
        assertTrue(buttonWithText(view, "Tangaga olish").isEnabled());
        Dialog dialog = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openPurchase", reward);
        descendants(dialog).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .findFirst().orElseThrow().setValue(2);
        when(service.rewardSellForCoin("reward", 2)).thenReturn(true);
        button(dialog, "reward-purchase-confirm").click();
        button(dialog, "reward-purchase-confirm").click();
        verify(service, times(1)).rewardSellForCoin("reward", 2);
        verify(auth, times(2)).getBalanceByUserId(any());
        assertFalse(dialog.isOpened());
    }

    @Test
    void insufficientBalanceDisablesPurchaseAndHistoryTabLoadsHistory() {
        when(auth.getBalanceByUserId(any())).thenReturn(10);
        var view = new RewardsView(service, auth);
        assertFalse(buttonWithText(view, "Tangalar yetarli emas").isEnabled());
        button(view, "rewards-history-tab").click();
        verify(service).getMyRewards(0, 9);
        assertFalse(byId(view, "rewards-catalog").isVisible());
    }

    @Test
    void emptyCatalogAndLoadFailureCannotPurchase() {
        when(service.getRewardsPagForUsers(0, 9)).thenReturn(Page.empty());
        var empty = new RewardsView(service, auth);
        assertEquals(0, byId(empty, "rewards-catalog").getChildren().count());
        assertFalse(button(empty, "rewards-next").isEnabled());
        when(service.getRewardsPagForUsers(0, 9)).thenThrow(new IllegalStateException("Unavailable"));
        var failed = new RewardsView(service, auth);
        assertEquals(0, byId(failed, "rewards-catalog").getChildren().count());
        assertFalse(button(failed, "rewards-next").isEnabled());
    }

    @Test
    void adminFormValidatesThenCallsCreate() {
        var view = new AdminRewardsView(service, mock(AttachService.class));
        Dialog dialog = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openEditor", (RewardDto) null);
        button(dialog, "reward-save").click();
        verify(service, never()).createReward(any());
        var texts = descendants(dialog).filter(TextField.class::isInstance).map(TextField.class::cast).toList();
        texts.get(0).setValue("Bus ticket");
        texts.get(1).setValue("Transit");
        descendants(dialog).filter(TextArea.class::isInstance).map(TextArea.class::cast)
                .findFirst().orElseThrow().setValue("One trip");
        var numbers = descendants(dialog).filter(IntegerField.class::isInstance).map(IntegerField.class::cast).toList();
        numbers.get(0).setValue(30);
        numbers.get(1).setValue(3);
        when(service.createReward(any())).thenReturn(true);
        button(dialog, "reward-save").click();
        verify(service).createReward(argThat(dto -> dto.getCoinPrice() == 30 && dto.getQuantity() == 3
                && "Bus ticket".equals(dto.getTitle())));
        assertFalse(dialog.isOpened());
    }

    @Test
    void ownedRewardsShowPurchaseDataAndReceiptWithoutCatalogLookup() {
        MyRewardDto purchase = MyRewardDto.builder().purchaseId("purchase-123")
                .rewardTitle("Bus ticket").rewardProviderName("Transit")
                .purchasedQuantity(2).coinsSpent(60).purchasedDateTime("2026-11-08T14:30:00").build();
        when(service.getMyRewards(0, 9)).thenReturn(new PageImpl<>(List.of(purchase)));
        var view = new RewardsView(service, auth);
        button(view, "rewards-history-tab").click();
        var owned = byId(view, "rewards-history");
        assertTrue(owned.isVisible());
        assertEquals(1, owned.getChildren().count());
        assertTrue(owned.getElement().getTextRecursively().contains("60 tanga"));
        Dialog receipt = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openReceipt", purchase);
        assertTrue(receipt.getElement().getTextRecursively().contains("purchase-123"));
        assertTrue(receipt.getElement().getTextRecursively().contains("08.11.2026 14:30"));
        verify(service, times(1)).getRewardsPagForUsers(0, 9);
    }

    @Test
    void successfulPurchaseOffersHistoryAndRecalculatesRemainingBalance() {
        var view = new RewardsView(service, auth);
        Dialog checkout = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openPurchase", reward);
        descendants(checkout).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .findFirst().orElseThrow().setValue(2);
        assertTrue(checkout.getElement().getTextRecursively().contains("Xariddan keyingi balans: 40 tanga"));
        when(service.rewardSellForCoin("reward", 2)).thenReturn(true);
        button(checkout, "reward-purchase-confirm").click();
        var success = byId(view, "reward-purchase-success");
        assertTrue(success.isVisible());
        buttonWithText(success, "Mening mukofotlarim").click();
        verify(service).getMyRewards(0, 9);
        assertFalse(success.isVisible());
    }

    @Test
    void userCanInspectFullRewardInformationBeforeBuying() {
        reward.setOptionalRewardImage("/attaches/ticket.png");
        var view = new RewardsView(service, auth);
        Dialog details = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openRewardDetails", reward);
        assertTrue(details.getElement().getTextRecursively().contains("One trip"));
        assertEquals("/attaches/ticket.png", descendants(details)
                .filter(com.vaadin.flow.component.html.Image.class::isInstance)
                .map(com.vaadin.flow.component.html.Image.class::cast).findFirst().orElseThrow().getSrc());
        verify(service, never()).rewardSellForCoin(anyString(), anyInt());
    }

    @Test
    void adminRewardDetailsShowPhotoAndSelectedRewardInformation() {
        reward.setOptionalRewardImage("/attaches/ticket.png");
        var view = new AdminRewardsView(service, mock(AttachService.class));
        Dialog dialog = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openDetails", reward);
        assertTrue(dialog.isOpened());
        var image = descendants(dialog).filter(com.vaadin.flow.component.html.Image.class::isInstance)
                .map(com.vaadin.flow.component.html.Image.class::cast).findFirst().orElseThrow();
        assertEquals("/attaches/ticket.png", image.getSrc());
        var text = descendants(dialog).map(c -> c.getElement().getTextRecursively()).toList();
        assertTrue(text.contains("Bus ticket"));
        assertTrue(text.contains("Transit"));
        assertTrue(text.contains("One trip"));
        assertTrue(text.contains("30"));
        assertTrue(text.contains("3"));
        buttonWithText(dialog, "Yopish").click();
        assertFalse(dialog.isOpened());
        verify(service, never()).updateReward(any(), any());
    }

    @Test
    void adminRewardWithoutPhotoHasPlaceholderAndCanOpenEditorFromDetails() {
        var view = new AdminRewardsView(service, mock(AttachService.class));
        Dialog dialog = org.springframework.test.util.ReflectionTestUtils.invokeMethod(view, "openDetails", reward);
        assertTrue(descendants(dialog).noneMatch(com.vaadin.flow.component.html.Image.class::isInstance));
        assertTrue(descendants(dialog).anyMatch(c -> "Rasm mavjud emas".equals(c.getElement().getTextRecursively())));
        buttonWithText(dialog, "Tahrirlash").click();
        assertFalse(dialog.isOpened());
        verify(service, never()).updateReward(any(), any());
    }

    private Button button(Component root, String id) { return (Button) byId(root, id); }
    private Component byId(Component root, String id) {
        return descendants(root).filter(c -> c.getId().orElse("").equals(id)).findFirst().orElseThrow();
    }
    private Button buttonWithText(Component root, String text) {
        return descendants(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }
    private Stream<Component> descendants(Component root) {
        Stream<Component> children = root.getChildren();
        if (root instanceof Dialog dialog) {
            children = Stream.concat(children, dialog.getFooter().getElement().getChildren().flatMap(element -> element.getComponent().stream()));
        }
        return children.flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
    }
}
