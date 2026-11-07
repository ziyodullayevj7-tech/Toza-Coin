package teamwork.TozaCoin;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import teamwork.entity.*;
import teamwork.enums.*;
import teamwork.repository.WasteReportRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MyWorkQueryTests {
    @Autowired
    EntityManager em;
    @Autowired
    WasteReportRepository reports;

    @Test
    void includesBothRolesWithoutDuplicatesAndSeparatesSubmittedCleanups() {
        ProfileEntity owner = profile();
        ProfileEntity other = profile();
        WasteReportEntity reported = report(owner, null, null);
        WasteReportEntity cleanedForOther = report(other, owner, "after-other.jpg");
        WasteReportEntity cleanedOwn = report(owner, owner, "after-own.jpg");
        report(other, null, null);
        em.flush();
        em.clear();

        var all = reports.findMyWork(owner.getId(), null, PageRequest.of(0, 10));
        assertEquals(3, all.getTotalElements());
        assertEquals(3, all.stream().map(WasteReportEntity::getId).distinct().count());
        var cleaned = reports.findMyWork(owner.getId(), true, PageRequest.of(0, 10));
        assertEquals(java.util.Set.of(cleanedOwn.getId(), cleanedForOther.getId()),
                cleaned.stream().map(WasteReportEntity::getId).collect(java.util.stream.Collectors.toSet()));
        var pending = reports.findMyWork(owner.getId(), false, PageRequest.of(0, 10));
        assertEquals(java.util.List.of(reported.getId()), pending.stream().map(WasteReportEntity::getId).toList());
        // A cleanup with a submitted after photo belongs in Cleaned while moderation is pending.
        assertTrue(cleaned.stream().allMatch(r -> r.getReportStatus() == ReportStatus.PENDING));
    }

    @Test
    void paginationCountsUseTheSameParticipantAndPhotoFilters() {
        ProfileEntity owner = profile();
        ProfileEntity other = profile();
        report(owner, null, "");
        report(other, owner, "after.jpg");
        report(owner, owner, "after-own.jpg");
        em.flush();
        em.clear();
        var first = reports.findMyWork(owner.getId(), true, PageRequest.of(0, 1));
        assertEquals(2, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
        assertTrue(first.hasNext());
        assertEquals(1, reports.findMyWork(owner.getId(), false, PageRequest.of(0, 1)).getTotalElements());
        assertTrue(reports.findMyWork("nonexistent-user", null, PageRequest.of(0, 10)).isEmpty());
    }

    private ProfileEntity profile() {
        ProfileEntity profile = new ProfileEntity();
        profile.setName("UI query test");
        em.persist(profile);
        return profile;
    }

    private WasteReportEntity report(ProfileEntity reporter, ProfileEntity cleaner, String after) {
        LocationEntity location = new LocationEntity();
        location.setLatitude(41.3);
        location.setLongitude(69.2);
        WasteReportEntity report = new WasteReportEntity();
        report.setLocation(location);
        report.setReporter(reporter);
        report.setCleaner(cleaner);
        report.setBeforeImageUrl("before.jpg");
        report.setAfterImageUrl(after);
        report.setCategory(WasteTypeEnum.PLASTIC);
        report.setSeverity(SeverityLevelEnum.LOW);
        report.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        report.setReportStatus(ReportStatus.PENDING);
        report.setCreatedDate(LocalDateTime.now());
        em.persist(report);
        return report;
    }
}
