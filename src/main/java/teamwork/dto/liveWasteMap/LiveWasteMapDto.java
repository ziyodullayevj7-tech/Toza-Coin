package teamwork.dto.liveWasteMap;

import lombok.Getter;
import lombok.Setter;
import teamwork.dto.location.LocationDto;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;

@Getter
@Setter
public class LiveWasteMapDto {
    private String id;
    private String imageUrl;
    private WasteTypeEnum wasteType;
    private SeverityLevelEnum severityLevel;
    private Integer rewardCoins;
    private String reporterId;
    private LocationDto location;
}
