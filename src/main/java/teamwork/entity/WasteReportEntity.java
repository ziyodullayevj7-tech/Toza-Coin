package teamwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    @Column(name = "is_live_camera")
    @Builder.Default
    private Boolean isLiveCamera = true;

    @Column(name = "captured_date")
    @CreationTimestamp
    private LocalDateTime capturedDate;

    @Column(name = "location_id", insertable = false, updatable = false)
    private String locationId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false)
    private WasteTypeEnum category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false)
    private SeverityLevelEnum  severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_action", nullable = false)
    private ReportActionEnum reportAction;

    @Column(name = "reward_coins")
    private Integer rewardCoins;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    @Column(name = "reporter_id", insertable = false, updatable = false)
    private String reporterId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private ProfileEntity reporter;

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
