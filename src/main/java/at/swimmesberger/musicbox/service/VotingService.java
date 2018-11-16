package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.repository.VideoUserVoteRepository;
import org.springframework.stereotype.Service;

@Service
public class VotingService {
    private final VideoUserVoteRepository voteRepository;

    public VotingService(VideoUserVoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }
}
