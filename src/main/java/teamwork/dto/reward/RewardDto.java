package teamwork.dto.reward;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewardDto {
    private String id;
    private String title;
    private String description;
    private Integer coinPrice;
    private String providerName;
    private String optionalRewardImage;
    private Integer quantity;
    private Boolean isActive;
}
