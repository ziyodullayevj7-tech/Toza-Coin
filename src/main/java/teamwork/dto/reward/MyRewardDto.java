package teamwork.dto.reward;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyRewardDto {
    private String purchaseId;
    private String rewardTitle;
    private String rewardProviderName;
    private Integer purchasedQuantity;
    private Integer coinsSpent;
    private String purchasedDateTime;
}
