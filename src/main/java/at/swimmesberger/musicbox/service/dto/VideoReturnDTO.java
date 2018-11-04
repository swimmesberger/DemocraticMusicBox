package at.swimmesberger.musicbox.service.dto;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoReturnDTO {
    private final VideoIdDTO id;
    private final String idString;
    private final Long processingId;
    private final Long processingTime;
    private final ProcessingStatus status;
    private final VideoMetadataDTO metadata;

    @JsonCreator
    public VideoReturnDTO(@JsonProperty("id") VideoIdDTO videoId, @JsonProperty("id_string") String idString, @JsonProperty("processing_id") Long processingId, @JsonProperty("processing_time") Long processingTime, @JsonProperty("status") ProcessingStatus status, @JsonProperty("metadata") VideoMetadataDTO metadata) {
        this.id = videoId;
        this.idString = idString;
        this.processingId = processingId;
        this.processingTime = processingTime;
        this.status = status;
        this.metadata = metadata;
    }

    @JsonProperty("id")
    public VideoIdDTO getId() {
        return id;
    }

    @JsonProperty("id_string")
    public String getIdString() {
        return idString;
    }

    @JsonProperty("processing_id")
    public Long getProcessingId() {
        return processingId;
    }

    @JsonProperty("processing_time")
    public Long getProcessingTime() {
        return processingTime;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public VideoMetadataDTO getMetadata() {
        return metadata;
    }
}
