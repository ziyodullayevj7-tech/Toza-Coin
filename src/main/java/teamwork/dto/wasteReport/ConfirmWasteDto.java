package teamwork.dto.wasteReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import teamwork.enums.ReportStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmWasteDto {
    private String reportId;
    private String reporterId;
    private String reporterFullName;
    private String district;
    private String beforeImageUrl;
    private String afterImageUrl;
    private ReportStatus status;
    private Double cleanPeriod;
    private Integer confidenceScore;
    private String reasoning;
    private LocalDateTime reportedDateTime;
}
