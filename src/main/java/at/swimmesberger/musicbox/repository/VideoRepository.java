package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.Video;
import at.swimmesberger.musicbox.domain.VideoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, VideoId> {
}
