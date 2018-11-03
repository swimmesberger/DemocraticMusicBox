package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.domain.VideoId;
import at.swimmesberger.musicbox.domain.VideoPostProcessingUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoPostProcessingRepository extends JpaRepository<VideoPostProcessingUnit, Long> {
    List<VideoPostProcessingUnit> findByStatus(ProcessingStatus status);

    List<VideoPostProcessingUnit> findByStatusAndVideoId(ProcessingStatus status, VideoId videoId);
}
