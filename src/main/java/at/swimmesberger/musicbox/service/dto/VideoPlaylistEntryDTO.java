package at.swimmesberger.musicbox.service.dto;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoPlaylistEntryDTO extends VideoReturnDTO {
    private final Long playlistVideoEntryId;

    @JsonCreator
    public VideoPlaylistEntryDTO(@JsonProperty("video_id") VideoIdDTO videoId, @JsonProperty("video_id_string") String idString,
                                 @JsonProperty("processing_id") Long processingId, @JsonProperty("processing_time") Long processingTime,
                                 @JsonProperty("status") ProcessingStatus status, @JsonProperty("metadata") VideoMetadataDTO metadata,
                                 @JsonProperty("entry_id") Long playlistVideoEntryId) {
        super(videoId, idString, processingId, processingTime, status, metadata);
        this.playlistVideoEntryId = playlistVideoEntryId;
    }

    public VideoPlaylistEntryDTO(VideoReturnDTO videoReturnDTO, Long playlistVideoEntryId){
        super(videoReturnDTO);
        this.playlistVideoEntryId = playlistVideoEntryId;
    }

    @JsonProperty("video_id")
    @Override
    public VideoIdDTO getId() {
        return super.getId();
    }

    @JsonProperty("video_id_string")
    @Override
    public String getIdString() {
        return super.getIdString();
    }

    @JsonProperty("entry_id")
    public Long getPlaylistVideoEntryId() {
        return playlistVideoEntryId;
    }
}
