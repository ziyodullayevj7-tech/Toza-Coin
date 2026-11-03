package teamwork.dto.leaderBoard;

import teamwork.enums.ProfileStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardDto {
    private String profileId;
    private String fullName;
    private String district;
    private Integer streakDays;
    private Integer totalEarnedCoins;
    private Integer reportCount;
    private Integer coinBalance;
    private ProfileStatus status;
    private LocalDateTime createdDate;
}
