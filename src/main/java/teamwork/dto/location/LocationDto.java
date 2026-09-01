package teamwork.dto.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationDto {
    private double latitude;
    private double longitude;
    private String region;
    private String district;
    private String streetAddress;
}
