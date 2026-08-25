package teamwork.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDto {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String password;
    private List<String> roles;
}
