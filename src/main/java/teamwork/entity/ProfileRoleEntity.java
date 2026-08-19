package teamwork.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import teamwork.enums.ProfileRole;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "profile_role")
public class ProfileRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "profile_id")
    private String profileId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", insertable = false, updatable = false)
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "roles")
    private ProfileRole roles;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
