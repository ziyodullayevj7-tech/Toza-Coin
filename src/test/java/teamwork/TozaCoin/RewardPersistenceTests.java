package teamwork.TozaCoin;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import teamwork.entity.*;
import teamwork.repository.*;
import teamwork.service.RewardService;
import teamwork.util.SecurityUtils;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class RewardPersistenceTests {
    @org.junit.jupiter.api.BeforeEach
    void authenticate() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "admin", "unused", java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @org.junit.jupiter.api.AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Autowired EntityManager em;
    @Autowired RewardService service;
    @Autowired RewardRepository rewards;
    @Autowired ProfileRewardRepository purchases;

    @Test
    void purchasePersistsLinksBalanceStockAndLedger() {
        ProfileEntity profile = new ProfileEntity();
        profile.setName("Reward purchase test");
        profile.setCoinBalance(100);
        em.persist(profile);
        RewardEntity reward = reward(true, 2);
        em.flush();
        String profileId = profile.getId();
        String rewardId = reward.getId();
        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(profileId);
            service.rewardSellForCoin(rewardId, 1);
            em.flush();
            em.clear();
            assertEquals(70, em.find(ProfileEntity.class, profileId).getCoinBalance());
            assertEquals(1, em.find(RewardEntity.class, rewardId).getQuantity());
            var owned = purchases.findAllByUserId(profileId, PageRequest.of(0, 10));
            assertEquals(1, owned.getTotalElements());
            assertEquals(rewardId, owned.getContent().getFirst().getRewardId());
            assertEquals(30, owned.getContent().getFirst().getCoinsSpent());
            var ledger = em.createQuery("SELECT t FROM CoinTransactionEntity t WHERE t.profileId = :id", CoinTransactionEntity.class)
                    .setParameter("id", profileId).getSingleResult();
            assertEquals(teamwork.enums.CoinType.SPEND, ledger.getCoinType());
            assertEquals(30, ledger.getAmount());
            assertEquals(1, service.getMyRewards(0, 10).getTotalElements());
        }
    }

    @Test
    void catalogIncludesLastItemAndExcludesInactiveOrEmptyRewards() {
        RewardEntity last = reward(true, 1);
        RewardEntity inactive = reward(false, 2);
        RewardEntity empty = reward(true, 0);
        em.flush();
        var visible = rewards.findAllByIsActiveAndQuantity(true, 1, PageRequest.of(0, 10000));
        var ids = visible.stream().map(RewardEntity::getId).toList();
        assertTrue(ids.contains(last.getId()));
        assertFalse(ids.contains(inactive.getId()));
        assertFalse(ids.contains(empty.getId()));
    }

    @Test
    void updateAndStatusChangePersistAndAllowKeepingOwnIdentity() {
        RewardEntity reward = reward(true, 3);
        em.flush();
        String id = reward.getId();
        var dto = service.toDto(reward);
        dto.setCoinPrice(25);
        dto.setQuantity(0);
        dto.setDescription("Updated trip");
        assertTrue(service.updateReward(id, dto));
        assertTrue(service.changeRewardStatus(id));
        em.flush();
        em.clear();
        var saved = em.find(RewardEntity.class, id);
        assertEquals(25, saved.getCoinPrice());
        assertEquals(0, saved.getQuantity());
        assertEquals("Updated trip", saved.getDescription());
        assertFalse(saved.getIsActive());
    }

    private RewardEntity reward(boolean active, int quantity) {
        RewardEntity reward = new RewardEntity();
        reward.setTitle("Test bus ticket " + java.util.UUID.randomUUID());
        reward.setProviderName("Test transit");
        reward.setCoinPrice(30);
        reward.setQuantity(quantity);
        reward.setIsActive(active);
        reward.setCreatedDateTime(LocalDateTime.now());
        em.persist(reward);
        return reward;
    }
}
