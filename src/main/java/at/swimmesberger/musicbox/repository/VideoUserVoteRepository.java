package at.swimmesberger.musicbox.repository;

import at.swimmesberger.musicbox.domain.VideoUserVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoUserVoteRepository extends JpaRepository<VideoUserVote, Long> {
}
