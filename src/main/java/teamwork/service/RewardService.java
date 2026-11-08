package teamwork.service;

import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import teamwork.dto.reward.MyRewardDto;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.enums.CoinType;
import teamwork.dto.reward.RewardDto;
import teamwork.entity.ProfileEntity;
import teamwork.entity.ProfileRewardEntity;
import teamwork.entity.RewardEntity;
import teamwork.exceptions.AppBadRequestException;
import teamwork.exceptions.ItemNotFoundException;
import teamwork.repository.ProfileRewardRepository;
import teamwork.repository.RewardRepository;
import teamwork.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class RewardService {
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    private ProfileRewardRepository profileRewardRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private CoinTransactionService coinTransactionService;

    @PreAuthorize("hasRole('ADMIN')")
    public Boolean createReward(RewardDto dto) {
        Optional<RewardEntity> optionalReward = rewardRepository.findByTitleAndProviderNameIgnoreCase(dto.getTitle(), dto.getProviderName());
        if (optionalReward.isPresent()) {
            throw new AppBadRequestException("This reward has already been created");
        }
        RewardEntity entity = new RewardEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCoinPrice(dto.getCoinPrice());
        entity.setProviderName(dto.getProviderName());
        entity.setOptionalRewardImage(dto.getOptionalRewardImage());
        entity.setQuantity(dto.getQuantity());
        entity.setIsActive(Boolean.TRUE);
        entity.setCreatedDateTime(LocalDateTime.now());

        rewardRepository.save(entity);

        return true;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Page<RewardDto> getRewardsPagForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RewardEntity> rewards = rewardRepository.findAll(pageable);
        return getRewardDtos(pageable, rewards);
    }

    @Transactional
    public Page<RewardDto> getRewardsPagForUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RewardEntity> rewards = rewardRepository.findAllByIsActiveAndQuantity(Boolean.TRUE, 1, pageable);
        return getRewardDtos(pageable, rewards);
    }

    @Transactional
    public Page<MyRewardDto> getMyRewards(int page, int size) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);

        Page<ProfileRewardEntity> profileRewards = profileRewardRepository.findAllByUserId(currentUserId, pageable);

        List<ProfileRewardEntity> entities = profileRewards.getContent();
        long totalElements = profileRewards.getTotalElements();

        List<MyRewardDto> dtos = new ArrayList<>();
        entities.forEach(entity -> dtos.add(toMyRewardDto(entity)));

        return new PageImpl<>(dtos, pageable, totalElements);
    }

    public MyRewardDto toMyRewardDto(ProfileRewardEntity entity) {
        MyRewardDto dto = new MyRewardDto();
        dto.setPurchaseId(entity.getId());
        dto.setRewardTitle(entity.getReward().getTitle());
        dto.setRewardProviderName(entity.getReward().getProviderName());
        dto.setPurchasedQuantity(entity.getQuantity());
        dto.setCoinsSpent(entity.getCoinsSpent());
        dto.setPurchasedDateTime(entity.getCreatedDateTime().toString());
        return dto;
    }

    public RewardDto toDto(RewardEntity entity) {
        RewardDto dto = new RewardDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setProviderName(entity.getProviderName());
        dto.setDescription(entity.getDescription());
        dto.setCoinPrice(entity.getCoinPrice());
        dto.setQuantity(entity.getQuantity());
        dto.setOptionalRewardImage(entity.getOptionalRewardImage());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }

    @Transactional
    public Boolean rewardSellForCoin(String rewardId, Integer quantity) {
        validatePurchaseInput(rewardId, quantity);
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new AppBadRequestException("Authentication required");
        }
        // Always lock stock before debiting the profile. Both locks last until commit.
        RewardEntity reward = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new ItemNotFoundException("Reward not found"));
        validateAvailability(reward, quantity);
        Integer price = reward.getCoinPrice();
        if (price == null || price <= 0) {
            throw new AppBadRequestException("Reward price must be positive");
        }
        final int totalCost;
        try {
            totalCost = Math.multiplyExact(price, quantity);
        } catch (ArithmeticException exception) {
            throw new AppBadRequestException("Purchase total exceeds the supported amount");
        }
        if (!Boolean.TRUE.equals(authService.updateUserBalanceForRewardSell(currentUserId, totalCost))) {
            throw new AppBadRequestException("Could not debit the reward purchase");
        }
        ProfileEntity profile = authService.getCurrentProfile(currentUserId);
        reward.setQuantity(reward.getQuantity() - quantity);
        rewardRepository.save(reward);

        ProfileRewardEntity purchase = new ProfileRewardEntity();
        purchase.setProfile(profile);
        purchase.setReward(reward);
        purchase.setQuantity(quantity);
        purchase.setCoinsSpent(totalCost);
        purchase.setCreatedDateTime(LocalDateTime.now());
        profileRewardRepository.save(purchase);

        CoinTransactionDto transaction = new CoinTransactionDto();
        transaction.setCoinType(CoinType.SPEND);
        transaction.setAmount(totalCost);
        transaction.setDescription("Reward purchase: " + rewardId + ", quantity: " + quantity);
        if (!coinTransactionService.save(transaction, profile)) {
            throw new AppBadRequestException("Could not record the reward purchase transaction");
        }
        return true;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean rewardQuantityUpdate(String rewardId, Integer quantity) {
        validatePurchaseInput(rewardId, quantity);
        RewardEntity reward = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new ItemNotFoundException("Reward not found"));
        validateAvailability(reward, quantity);
        reward.setQuantity(reward.getQuantity() - quantity);
        rewardRepository.save(reward);
        return true;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean changeRewardStatus(String rewardId) {
        validateRewardId(rewardId);
        RewardEntity entity = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new ItemNotFoundException("Reward not found"));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        rewardRepository.save(entity);
        return true;
    }

    /** Replaces editable catalog fields; quantity is the new available stock. */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean updateReward(String rewardId, RewardDto dto) {
        validateRewardId(rewardId);
        if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getProviderName() == null || dto.getProviderName().isBlank()
                || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new AppBadRequestException("Title, provider and description are required");
        }
        if (dto.getCoinPrice() == null || dto.getCoinPrice() <= 0
                || dto.getQuantity() == null || dto.getQuantity() < 0) {
            throw new AppBadRequestException("Price must be positive and quantity cannot be negative");
        }
        String title = dto.getTitle().trim();
        String provider = dto.getProviderName().trim();
        RewardEntity entity = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new ItemNotFoundException("Reward not found"));
        if (rewardRepository.existsByTitleIgnoreCaseAndProviderNameIgnoreCaseAndIdNot(
                title, provider, rewardId)) {
            throw new AppBadRequestException("This reward has already been created");
        }
        entity.setTitle(title);
        entity.setProviderName(provider);
        entity.setDescription(dto.getDescription().trim());
        entity.setCoinPrice(dto.getCoinPrice());
        entity.setQuantity(dto.getQuantity());
        String image = dto.getOptionalRewardImage();
        entity.setOptionalRewardImage(image == null || image.isBlank() ? null : image.trim());
        rewardRepository.save(entity);
        return true;
    }

    private void validateRewardId(String rewardId) {
        if (rewardId == null || rewardId.isBlank()) {
            throw new AppBadRequestException("Reward ID is required");
        }
    }

    private void validatePurchaseInput(String rewardId, Integer quantity) {
        if (rewardId == null || rewardId.isBlank() || quantity == null || quantity <= 0) {
            throw new AppBadRequestException("A reward ID and positive quantity are required");
        }
    }

    private void validateAvailability(RewardEntity reward, int quantity) {
        if (!Boolean.TRUE.equals(reward.getIsActive())) {
            throw new AppBadRequestException("Reward is inactive");
        }
        if (reward.getQuantity() == null || reward.getQuantity() < quantity) {
            throw new AppBadRequestException("Not enough rewards available");
        }
    }

    @Transactional
    public Page<RewardDto> getRewardsPagForUser(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RewardEntity> rewards = rewardRepository.findAllByIsActiveAndQuantity(Boolean.TRUE, 1, pageable);
        return getRewardDtos(pageable, rewards);
    }

    @NonNull
    private Page<RewardDto> getRewardDtos(Pageable pageable, Page<RewardEntity> rewards) {
        List<RewardEntity> entities = rewards.getContent();
        long totalElements = rewards.getTotalElements();

        List<RewardDto> dtos = new ArrayList<>();
        entities.forEach(entity -> dtos.add(toDto(entity)));

        return new PageImpl<>(dtos, pageable, totalElements);
    }
}
