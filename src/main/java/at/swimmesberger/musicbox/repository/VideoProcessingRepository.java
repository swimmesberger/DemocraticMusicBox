package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.VideoProcessingUnit;
import at.swimmesberger.musicbox.domain.ProcessingStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoProcessingRepository extends CrudRepository<VideoProcessingUnit, Long> {
    List<VideoProcessingUnit> findByStatus(ProcessingStatus status);
}
