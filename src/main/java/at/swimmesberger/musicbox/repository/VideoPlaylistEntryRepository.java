package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.VideoPlaylistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoPlaylistEntryRepository extends JpaRepository<VideoPlaylistEntry, Long> {
}
