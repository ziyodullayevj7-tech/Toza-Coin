package teamwork.config;

import lombok.Getter;
import lombok.Setter;
import teamwork.entity.ProfileEntity;
import teamwork.enums.ProfileStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private String id;
    private String name;
    private String surname;
    private String username;
    private String phoneNumber;
    private String password;
    private ProfileStatus status;
    private List<SimpleGrantedAuthority> roles;

    public CustomUserDetails(ProfileEntity entity) {
        this.status = entity.getStatus();
        this.phoneNumber = entity.getPhoneNumber();
        this.username = entity.getUsername();
        this.surname = entity.getSurname();
        this.name = entity.getName();
        this.id = entity.getId();
        this.password = entity.getPassword();

        List<SimpleGrantedAuthority> roleList = new LinkedList<>();

        if (entity.getProfileRoles() != null) {
            entity.getProfileRoles().forEach(role -> {
                if (role.getRoles() != null) {
                    roleList.add(new SimpleGrantedAuthority(role.getRoles().name()));
                }
            });
        }

        if (roleList.isEmpty()) {
            roleList.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.roles = roleList;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return this.roles;
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }
}
