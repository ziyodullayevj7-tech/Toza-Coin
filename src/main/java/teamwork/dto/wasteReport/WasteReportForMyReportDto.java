package teamwork.dto.wasteReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import teamwork.enums.ReportStatus;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WasteReportForMyReportDto {
    private String photoUrl;
    private ReportStatus reportStatus;
    private SeverityLevelEnum severityLevel;
    private WasteTypeEnum wasteType;
    private String description;
    private Integer coins;
    private String region;
    private String district;
    private String streetAddress;
    private LocalDate date;
}
