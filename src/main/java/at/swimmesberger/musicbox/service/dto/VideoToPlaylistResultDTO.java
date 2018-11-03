package at.swimmesberger.musicbox.service.dto;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoToPlaylistResultDTO {
    private final long playlistId;
    private final ProcessingStatus status;
    private final VideoIdDTO videoId;
    private final Long processingId;

    @JsonCreator
    public VideoToPlaylistResultDTO(@JsonProperty("playlist_id") long playlistId, @JsonProperty("status") ProcessingStatus status, @JsonProperty("video_id") VideoIdDTO videoId, @JsonProperty("processing_id") Long processingId) {
        this.playlistId = playlistId;
        this.status = status;
        this.videoId = videoId;
        this.processingId = processingId;
    }

    @JsonProperty("playlist_id")
    public long getPlaylistId() {
        return playlistId;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    @JsonProperty("video_id")
    public VideoIdDTO getVideoId() {
        return videoId;
    }

    @JsonProperty("processing_id")
    public Long getProcessingId() {
        return processingId;
    }
}
