package teamwork.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiAnaliseResultDto {
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

    public void validate() {
        if (sameLocation == null || wasteCleared == null
                || confidenceScore == null
                || confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("AI returned an incomplete or invalid analysis");
        }
    }

    public boolean isEligibleForReward() {
        return Boolean.TRUE.equals(sameLocation)
                && Boolean.TRUE.equals(wasteCleared)
                && confidenceScore != null
                && confidenceScore >= 80;
    }
}