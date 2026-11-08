package teamwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_reward")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRewardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "profile_id", insertable = false, updatable = false)
    private String profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @Column(name = "reward_id", insertable = false, updatable = false)
    private String rewardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private RewardEntity reward;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "coins_spent")
    private Integer coinsSpent;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDateTime;
}
