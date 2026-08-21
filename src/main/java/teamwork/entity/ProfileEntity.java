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

    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProfileStatus status = ProfileStatus.ACTIVE;
    @Column(name = "profile_image")
    private String profileImageUrl;
    @Column(name = "district")
    private String district;
    @Column(name = "mahalla")
    private String mahalla;
    @Column(name = "streak_days")
    private Integer streakDays;
    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "photo_id")
    private String photoId;
    

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private List<ProfileRoleEntity> profileRoles;

    @Column(nullable = false)
    private boolean blocked = false;
}
