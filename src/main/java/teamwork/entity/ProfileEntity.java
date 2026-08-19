package teamwork.entity;


import jakarta.persistence.*;
import lombok.Data;
import teamwork.enums.ProfileStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "profile")
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "surname")
    private String surname;
    @Column(name = "username", unique = true)
    private String username;     // phone or email
    @Column(name = "password")
    private String password;

    @Column(name = "temp_phone")
    private String tempPhone;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProfileStatus status = ProfileStatus.ACTIVE;
    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "photo_id")
    private String photoId;
    

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private List<ProfileRoleEntity> profileRoles;

    @Column
    private Long telegramId;

    @Transient
    private Integer postCount = 0;

}
