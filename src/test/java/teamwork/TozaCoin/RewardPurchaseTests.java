package teamwork.TozaCoin;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.entity.*;
import teamwork.enums.CoinType;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.*;
import teamwork.service.*;
import teamwork.util.SecurityUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardPurchaseTests {
    private final RewardRepository rewards = mock(RewardRepository.class);
    private final ProfileRewardRepository purchases = mock(ProfileRewardRepository.class);
    private final CoinTransactionService coins = mock(CoinTransactionService.class);
    private final ProfileRepository profiles = mock(ProfileRepository.class);
    private final AuthService auth = new AuthService();
    private final RewardService service = new RewardService();
    private final ProfileEntity profile = new ProfileEntity();
    private final RewardEntity reward = new RewardEntity();
    private MockedStatic<SecurityUtils> security;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(auth, "profileRepository", profiles);
        ReflectionTestUtils.setField(service, "rewardRepository", rewards);
        ReflectionTestUtils.setField(service, "profileRewardRepository", purchases);
        ReflectionTestUtils.setField(service, "coinTransactionService", coins);
        ReflectionTestUtils.setField(service, "authService", auth);
        profile.setId("user");
        profile.setCoinBalance(100);
        reward.setId("reward");
        reward.setCoinPrice(30);
        reward.setQuantity(3);
        reward.setIsActive(true);
        when(rewards.findByIdForUpdate("reward")).thenReturn(Optional.of(reward));
        when(profiles.findByIdForUpdate("user")).thenReturn(Optional.of(profile));
        when(profiles.getReferenceById("user")).thenReturn(profile);
        when(coins.save(any(), any())).thenReturn(true);
        security = mockStatic(SecurityUtils.class);
        security.when(SecurityUtils::getCurrentUserId).thenReturn("user");
    }

    @AfterEach
    void cleanup() { security.close(); }

    @Test
    void updatesEditableFieldsAndPreservesStatusAndCreationDate() {
        var created = java.time.LocalDateTime.now().minusDays(2);
        reward.setCreatedDateTime(created);
        reward.setIsActive(false);
        var dto = updateDto();
        dto.setQuantity(0);
        dto.setOptionalRewardImage(" ");
        assertTrue(service.updateReward("reward", dto));
        assertEquals("Bus ticket", reward.getTitle());
        assertEquals("Transit", reward.getProviderName());
        assertEquals("One trip", reward.getDescription());
        assertEquals(20, reward.getCoinPrice());
        assertEquals(0, reward.getQuantity());
        assertNull(reward.getOptionalRewardImage());
        assertFalse(reward.getIsActive());
        assertEquals(created, reward.getCreatedDateTime());
        verify(rewards).save(reward);
    }

    @Test
    void duplicateUpdateDoesNotMutateReward() {
        when(rewards.existsByTitleIgnoreCaseAndProviderNameIgnoreCaseAndIdNot(
                "Bus ticket", "Transit", "reward")).thenReturn(true);
        assertThrows(AppBadRequestException.class, () -> service.updateReward("reward", updateDto()));
        assertEquals(30, reward.getCoinPrice());
        verify(rewards, never()).save(any());
    }

    @Test
    void rejectsInvalidUpdateAndMissingReward() {
        assertThrows(AppBadRequestException.class, () -> service.updateReward("reward", null));
        var dto = updateDto();
        dto.setQuantity(-1);
        assertThrows(AppBadRequestException.class, () -> service.updateReward("reward", dto));
        dto.setQuantity(1);
        dto.setCoinPrice(0);
        assertThrows(AppBadRequestException.class, () -> service.updateReward("reward", dto));
        dto.setCoinPrice(20);
        dto.setTitle(" ");
        assertThrows(AppBadRequestException.class, () -> service.updateReward("reward", dto));
        assertThrows(teamwork.exceptions.ItemNotFoundException.class,
                () -> service.updateReward("missing", updateDto()));
        verify(rewards, never()).save(any());
    }

    @Test
    void statusToggleSavesBothDirectionsAndHandlesNull() {
        service.changeRewardStatus("reward");
        assertFalse(reward.getIsActive());
        service.changeRewardStatus("reward");
        assertTrue(reward.getIsActive());
        reward.setIsActive(null);
        service.changeRewardStatus("reward");
        assertTrue(reward.getIsActive());
        verify(rewards, times(3)).save(reward);
    }

    private teamwork.dto.reward.RewardDto updateDto() {
        var dto = new teamwork.dto.reward.RewardDto();
        dto.setTitle(" Bus ticket ");
        dto.setProviderName(" Transit ");
        dto.setDescription(" One trip ");
        dto.setCoinPrice(20);
        dto.setQuantity(5);
        return dto;
    }

    @Test
    void purchaseDebitsOnceAndPersistsStockHistoryAndSpendTransaction() {
        assertTrue(service.rewardSellForCoin("reward", 2));
        assertEquals(40, profile.getCoinBalance());
        assertEquals(1, reward.getQuantity());
        verify(purchases).save(argThat(p -> p.getProfile() == profile
                && p.getReward() == reward && p.getQuantity() == 2 && p.getCoinsSpent() == 60));
        verify(coins).save(argThat(t -> t.getCoinType() == CoinType.SPEND && t.getAmount() == 60), same(profile));
    }

    @Test
    void invalidQuantitiesNeverReachDatabase() {
        for (Integer quantity : new Integer[]{null, 0, -1}) {
            assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", quantity));
        }
        verifyNoInteractions(rewards, profiles, purchases, coins);
    }

    @Test
    void inactiveAndInsufficientStockCannotBePurchased() {
        reward.setIsActive(false);
        assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 1));
        reward.setIsActive(true);
        assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 4));
        verifyNoInteractions(profiles, purchases, coins);
    }

    @Test
    void invalidPricesAndOverflowCannotDebitBalance() {
        for (Integer price : new Integer[]{null, 0, -1, Integer.MAX_VALUE}) {
            reward.setCoinPrice(price);
            assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 2));
        }
        verifyNoInteractions(profiles, purchases, coins);
    }

    @Test
    void insufficientAndNullBalanceCannotChangeStock() {
        profile.setCoinBalance(29);
        assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 1));
        profile.setCoinBalance(null);
        assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 1));
        assertEquals(3, reward.getQuantity());
        verifyNoInteractions(purchases, coins);
    }

    @Test
    void ledgerFailureThrowsToRollBackPurchaseTransaction() {
        when(coins.save(any(), any())).thenReturn(false);
        assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 1));
    }

    @Test
    void purchaseHistoryUsesStoredCostAfterCatalogPriceChanges() {
        ProfileRewardEntity purchase = new ProfileRewardEntity();
        purchase.setId("purchase");
        purchase.setReward(reward);
        purchase.setQuantity(2);
        purchase.setCoinsSpent(60);
        purchase.setCreatedDateTime(java.time.LocalDateTime.now());
        reward.setCoinPrice(99);
        var dto = service.toMyRewardDto(purchase);
        assertEquals(60, dto.getCoinsSpent());
        assertEquals(2, dto.getPurchasedQuantity());
        assertEquals("purchase", dto.getPurchaseId());
    }
}
