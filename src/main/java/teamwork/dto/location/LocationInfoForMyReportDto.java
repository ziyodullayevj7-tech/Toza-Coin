package teamwork.dto.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationInfoForMyReportDto {
    private String region;
    private String district;
    private String streetAddress;
}
