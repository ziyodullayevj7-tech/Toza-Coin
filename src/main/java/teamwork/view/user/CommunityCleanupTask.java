package teamwork.view.user;

/** Browser draft for an already-claimed cleanup; ownership is checked by the service on submission. */
public record CommunityCleanupTask(String reportId, String title, String address, String beforeImageUrl,
                                   double latitude, double longitude, String reporterId, int rewardCoins,
                                   long startedAt, String afterImageUrl) implements java.io.Serializable {
    CommunityCleanupTask started() {
        return new CommunityCleanupTask(reportId, title, address, beforeImageUrl, latitude, longitude,
                reporterId, rewardCoins, System.currentTimeMillis(), null);
    }
    CommunityCleanupTask withPhoto(String path) {
        return new CommunityCleanupTask(reportId, title, address, beforeImageUrl, latitude, longitude,
                reporterId, rewardCoins, startedAt, path);
    }
}
