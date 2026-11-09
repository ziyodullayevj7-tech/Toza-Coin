package teamwork.dto.wasteReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import teamwork.enums.AiReviewStatus;
import teamwork.enums.ReportStatus;
import teamwork.enums.ReportActionEnum;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmWasteDto {
    private String reportId;
    private String reporterId;
    private String cleanerId;
    private String reporterFullName;
    private String district;
    private String beforeImageUrl;
    private String afterImageUrl;
    private ReportStatus status;
    private ReportActionEnum reportAction;
    private Double cleanPeriod;
    private Integer confidenceScore;
    private String reasoning;
    private AiReviewStatus aiReviewStatus;
    private Boolean wastePresent;
    private Boolean sameLocation;
    private Boolean wasteCleared;
    private String detectedAnchors;
    private LocalDateTime reportedDateTime;
}
