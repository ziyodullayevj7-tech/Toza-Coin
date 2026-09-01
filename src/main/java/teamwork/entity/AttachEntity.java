package teamwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachEntity {
    @Id
    private String id;

    @Column(name = "origin_name")
    private String originName;

    @Column(name = "size")
    private Long size;

    @Column(name = "type")
    private String type;

    @Column(name = "path")
    private String path;

    @Column(name = "duration")
    private Long duration;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
