package teamwork.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import teamwork.enums.WasteTypeEnum;
import teamwork.enums.SeverityLevelEnum;

@Getter
@Setter
@NoArgsConstructor
public class AiAnaliseResultDto {
    @JsonProperty("wastePresent")
    private Boolean wastePresent;

    @JsonProperty("isSameLocation")
    private Boolean sameLocation;

    @JsonProperty("wasteCleared")
    private Boolean wasteCleared;

    @JsonProperty("confidenceScore")
    private Integer confidenceScore;

    @JsonProperty("detectedAnchors")
    private List<String> detectedAnchors;

    @JsonProperty("reasoning")
    private String reasoning;

    private WasteTypeEnum wasteType;
    private SeverityLevelEnum severityLevel;

    private void validateClassification() {
        if (wasteType == null || severityLevel == null) {
            throw new IllegalArgumentException("AI returned an incomplete classification");
        }
    }

    public void validateCleanup() {
        validateClassification();
        if (sameLocation == null || wasteCleared == null
                || confidenceScore == null
                || confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("AI returned an incomplete or invalid analysis");
        }
    }

    public void validateReport() {
        validateClassification();
        if (wastePresent == null || confidenceScore == null
                || confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("AI returned an incomplete or invalid report analysis");
        }
    }

    public boolean isEligibleForCleanupReward() {
        return Boolean.TRUE.equals(sameLocation)
                && Boolean.TRUE.equals(wasteCleared)
                && confidenceScore != null
                && confidenceScore >= 80;
    }

    public boolean isEligibleForReportReward() {
        return Boolean.TRUE.equals(wastePresent)
                && confidenceScore != null
                && confidenceScore >= 80;
    }
}