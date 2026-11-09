package teamwork.TozaCoin;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import teamwork.dto.wasteReport.*;
import teamwork.entity.*;
import teamwork.enums.*;
import teamwork.service.*;
import teamwork.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ReportPublicationTests {
    @Autowired EntityManager em;
    @Autowired AdminService admin;
    @Autowired MapService map;
    @Autowired WasteReportService reports;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void reportOnlyApprovalPublishesAndCommunityCleanupReturnsForSeparateApproval() {
        ProfileEntity reporter = profile();
        ProfileEntity cleaner = profile();
        WasteReportEntity report = report(reporter);
        em.flush();
        String id = report.getId(), reporterId = reporter.getId(), cleanerId = cleaner.getId();
        assertFalse(onMap(id));
        assertTrue(inQueue(id));
        assertTrue(admin.confirmReport(update(id, ReportStatus.IN_PROGRESS)));
        em.flush(); em.clear();
        assertTrue(onMap(id));
        assertFalse(inQueue(id));
        assertEquals(15, em.find(ProfileEntity.class, reporterId).getCoinBalance());
        assertEquals(50, em.find(WasteReportEntity.class, id).getCleanerRewardCoins());

        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(cleanerId);
            assertTrue(reports.claimReportForCleaning(id));
            em.flush(); em.clear();
            assertFalse(onMap(id));
            var cleanup = new CleanReportedWasteDto();
            cleanup.setWasteReportId(id);
            cleanup.setCleanedImageUrl("after.jpg");
            cleanup.setCleanPeriod(10.0);
            assertTrue(reports.updateReportCleaner(cleanup));
        }
        em.flush(); em.clear();
        assertTrue(inQueue(id));
        assertTrue(admin.confirmReport(update(id, ReportStatus.RESOLVED)));
        em.flush(); em.clear();
        assertEquals(ReportStatus.RESOLVED, em.find(WasteReportEntity.class, id).getReportStatus());
        assertFalse(onMap(id));
        assertFalse(inQueue(id));
        assertEquals(15, em.find(ProfileEntity.class, reporterId).getCoinBalance());
        assertEquals(50, em.find(ProfileEntity.class, cleanerId).getCoinBalance());
    }

    @Test
    void rejectedReportsRemainOffTheMapWithoutRewards() {
        var owner = profile();
        var report = report(owner);
        em.flush();
        admin.confirmReport(update(report.getId(), ReportStatus.REJECTED));
        em.flush();
        assertFalse(onMap(report.getId()));
        assertEquals(0, owner.getCoinBalance());
    }

    @Test
    void ordinaryUsersCannotApproveReports() {
        var report = report(profile());
        em.flush();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user", "unused", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> admin.confirmReport(update(report.getId(), ReportStatus.IN_PROGRESS)));
        assertEquals(ReportStatus.PENDING, report.getReportStatus());
        assertFalse(onMap(report.getId()));
    }

    private boolean onMap(String id) { return map.getLiveWasteForMap().stream().anyMatch(p -> id.equals(p.getId())); }
    private boolean inQueue(String id) { return reports.getUnresolvedReports(0, 1000).stream().anyMatch(p -> id.equals(p.getReportId())); }
    private ConfirmWasteDto update(String id, ReportStatus status) {
        var dto = new ConfirmWasteDto();dto.setReportId(id);dto.setStatus(status);return dto;
    }
    private ProfileEntity profile() {
        var profile = new ProfileEntity();profile.setName("Publication test");profile.setCoinBalance(0);
        profile.setTotalEarnedCoins(0);profile.setReportCount(0);em.persist(profile);return profile;
    }
    private WasteReportEntity report(ProfileEntity reporter) {
        var location = new LocationEntity();location.setLatitude(41.3);location.setLongitude(69.2);
        var report = new WasteReportEntity();report.setLocation(location);report.setReporter(reporter);
        report.setBeforeImageUrl("before.jpg");report.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        report.setReportStatus(ReportStatus.PENDING);report.setCategory(WasteTypeEnum.PLASTIC);
        report.setSeverity(SeverityLevelEnum.LOW);report.setCreatedDate(LocalDateTime.now());
        em.persist(report);return report;
    }
}
