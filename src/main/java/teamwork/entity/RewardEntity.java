package teamwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "coin_price")
    private Integer coinPrice;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "image_url")
    private String optionalRewardImage;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;
}
