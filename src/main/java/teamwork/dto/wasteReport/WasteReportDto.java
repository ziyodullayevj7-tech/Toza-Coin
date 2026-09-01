package teamwork.dto.wasteReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import teamwork.dto.location.LocationDto;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WasteReportDto {
    private String imageUrl;
    private String capturedDate;
    private WasteTypeEnum wasteType;
    private SeverityLevelEnum  severityLevel;
    private String description;
    private ReportActionEnum  reportAction;
    private Integer rewardCoins;
    private ReportStatus reportStatus;
    private LocationDto location;
}
