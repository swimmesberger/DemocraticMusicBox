package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.VideoId;
import at.swimmesberger.musicbox.domain.VideoProcessingUnit;
import at.swimmesberger.musicbox.domain.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VideoProcessingRepository extends JpaRepository<VideoProcessingUnit, Long> {
    List<VideoProcessingUnit> findByStatus(ProcessingStatus status);

    List<VideoProcessingUnit> findAllByOrderByCreatedAtAsc();

    List<VideoProcessingUnit> findAllByVideoIdInOrderByCreatedAtAsc(Collection<VideoId> ids);
}
