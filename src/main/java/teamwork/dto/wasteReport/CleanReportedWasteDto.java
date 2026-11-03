package teamwork.dto.wasteReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CleanReportedWasteDto {
    private String wasteReportId;
    private String cleanedImageUrl;
    private Double cleanPeriod;

}
