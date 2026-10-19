package teamwork.dto.leaderBoard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
