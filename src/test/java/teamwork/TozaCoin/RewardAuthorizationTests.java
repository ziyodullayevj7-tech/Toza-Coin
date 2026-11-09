package teamwork.TozaCoin;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.data.domain.Page;
import teamwork.dto.reward.RewardDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.*;
import teamwork.service.*;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardAuthorizationTests {
    private AnnotationConfigApplicationContext context;
    private RewardService service;
    private RewardRepository rewards;
    private ProfileRewardRepository purchases;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("auth", mock(AuthService.class));
        context.getBeanFactory().registerSingleton("coins", mock(CoinTransactionService.class));
        context.register(SecurityConfiguration.class);
        context.refresh();
        service = context.getBean(RewardService.class);
        rewards = context.getBean(RewardRepository.class);
        purchases = context.getBean(ProfileRewardRepository.class);
        when(rewards.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(Page.empty());
        when(rewards.findAllByIsActiveAndQuantity(eq(true), eq(1), any())).thenReturn(Page.empty());
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void ordinaryUsersCannotCallAnyAdminOperation() {
        authenticate("ROLE_USER");
        for (Runnable operation : adminOperations()) {
            assertThrows(AccessDeniedException.class, operation::run);
        }
        verifyNoInteractions(rewards, purchases);
    }

    @Test
    void unauthenticatedCallersCannotReadOrPurchaseRewards() {
        for (Runnable operation : memberOperations()) {
            assertThrows(AuthenticationCredentialsNotFoundException.class, operation::run);
        }
        for (Runnable operation : adminOperations()) {
            assertThrows(AuthenticationCredentialsNotFoundException.class, operation::run);
        }
        verifyNoInteractions(rewards, purchases);
    }

    @Test
    void unrelatedRolesCannotAccessRewards() {
        authenticate("ROLE_OTHER");
        for (Runnable operation : memberOperations()) {
            assertThrows(AccessDeniedException.class, operation::run);
        }
    }

    @Test
    void usersAndAdminsCanBrowseAndReachPurchaseValidation() {
        for (String role : List.of("ROLE_USER", "ROLE_ADMIN")) {
            authenticate(role);
            assertTrue(service.getRewardsPagForUsers(0, 10).isEmpty());
            assertTrue(service.getRewardsPagForUser(0, 10).isEmpty());
            assertThrows(AppBadRequestException.class, () -> service.rewardSellForCoin("reward", 0));
        }
    }

    @Test
    void adminCanCreateAndBrowseAndReachMutationValidation() {
        authenticate("ROLE_ADMIN");
        RewardDto dto = new RewardDto();
        dto.setTitle("Bus ticket");
        dto.setProviderName("Transit");
        when(rewards.findByTitleAndProviderNameIgnoreCase(any(), any())).thenReturn(Optional.empty());
        assertTrue(service.createReward(dto));
        assertTrue(service.getRewardsPagForAdmin(0, 10).isEmpty());
        assertThrows(AppBadRequestException.class, () -> service.updateReward("", dto));
        assertThrows(AppBadRequestException.class, () -> service.changeRewardStatus(""));
        assertThrows(AppBadRequestException.class, () -> service.rewardQuantityUpdate("", 1));
    }

    private List<Runnable> adminOperations() {
        return List.of(() -> service.createReward(new RewardDto()),
                () -> service.getRewardsPagForAdmin(0, 10),
                () -> service.updateReward("reward", new RewardDto()),
                () -> service.changeRewardStatus("reward"),
                () -> service.rewardQuantityUpdate("reward", 1));
    }

    private List<Runnable> memberOperations() {
        return List.of(() -> service.getRewardsPagForUsers(0, 10),
                () -> service.getRewardsPagForUser(0, 10),
                () -> service.getMyRewards(0, 10),
                () -> service.rewardSellForCoin("reward", 1));
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test", "unused", List.of(new SimpleGrantedAuthority(role))));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class SecurityConfiguration {
        @Bean RewardService rewardService() { return new RewardService(); }
        @Bean RewardRepository rewards() { return mock(RewardRepository.class); }
        @Bean ProfileRewardRepository purchases() { return mock(ProfileRewardRepository.class); }
    }
}
