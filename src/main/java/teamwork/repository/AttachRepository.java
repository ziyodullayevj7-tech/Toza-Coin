package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teamwork.entity.AttachEntity;

public interface AttachRepository extends JpaRepository<AttachEntity, String > {
    @Query("FROM AttachEntity a ORDER BY a.createdDate DESC ")
    Page<AttachEntity> findAllByOrderByCreatedDateDesc(Pageable pageable);
}
