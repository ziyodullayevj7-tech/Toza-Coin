package teamwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.enums.AiReviewStatus;
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

    @Column(nullable = false, name = "before_image_url")
    private String beforeImageUrl;

    @Column(name = "after_image_url")
    private String afterImageUrl;

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

    @Column(name = "reporter_reward_coins")
    private Integer reporterRewardCoins;

    @Column(name = "cleaner_reward_coins")
    private Integer cleanerRewardCoins;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    @Column(name = "reporter_id", insertable = false, updatable = false)
    private String reporterId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private ProfileEntity reporter;

    @Column(name = "cleaner_id", insertable = false, updatable = false)
    private String cleanerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_id")
    private ProfileEntity cleaner;

    @Column(name = "clean_period")
    private Double cleanPeriod;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_review_status")
    private AiReviewStatus aiReviewStatus;

    @Column(name = "ai_waste_present")
    private Boolean aiWastePresent;

    @Column(name = "ai_same_location")
    private Boolean aiSameLocation;

    @Column(name = "ai_waste_cleared")
    private Boolean aiWasteCleared;

    @Column(name = "ai_detected_anchors", columnDefinition = "TEXT")
    private String aiDetectedAnchors;

    @Column(name = "ai_reviewed_at")
    private LocalDateTime aiReviewedAt;

    @Column(name = "ai_reward_granted")
    private Boolean aiRewardGranted;

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
