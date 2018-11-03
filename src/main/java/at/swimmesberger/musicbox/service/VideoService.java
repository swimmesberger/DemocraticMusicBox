package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.domain.*;
import at.swimmesberger.musicbox.events.VideoCreatedEvent;
import at.swimmesberger.musicbox.events.VideoPostProcessingCreated;
import at.swimmesberger.musicbox.repository.PlaylistRepository;
import at.swimmesberger.musicbox.repository.VideoPostProcessingRepository;
import at.swimmesberger.musicbox.repository.VideoRepository;
import at.swimmesberger.musicbox.service.dto.*;
import at.swimmesberger.musicbox.service.errors.PlaylistNotFoundException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import at.swimmesberger.musicbox.service.processing.VideoProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class VideoService {
    private final Logger logger = LoggerFactory.getLogger(VideoService.class);
    private final VideoPostProcessingRepository postProcessingRepository;
    private final VideoRepository videoRepository;
    private final PlaylistRepository playlistRepository;
    private final VideoIdProcessingService idProcessingService;
    private final VideoProcessingService processingService;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper json;

    public VideoService(VideoPostProcessingRepository postProcessingRepository, VideoRepository videoRepository, PlaylistRepository playlistRepository,
                        VideoIdProcessingService idProcessingService, VideoProcessingService processingService, TransactionTemplate transactionTemplate,
                        ApplicationEventPublisher eventPublisher, ObjectMapper json) {
        this.postProcessingRepository = postProcessingRepository;
        this.videoRepository = videoRepository;
        this.playlistRepository = playlistRepository;
        this.idProcessingService = idProcessingService;
        this.processingService = processingService;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
        this.json = json;
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void initEvent() {
        this.transactionTemplate.execute(t -> {
            final List<VideoPostProcessingUnit> processingUnits = this.postProcessingRepository.findByStatus(ProcessingStatus.PEDNING);
            this.processPostProcessing(processingUnits);
            return null;
        });
    }

    @EventListener(VideoPostProcessingCreated.class)
    protected void videoPostProcessingCreatedEvent(VideoPostProcessingCreated event) {
        this.transactionTemplate.execute(t -> {
            final VideoPostProcessingUnit vppu = event.getVideoPostProcessingUnit();
            this.processPostProcessing(Collections.singleton(vppu));
            return null;
        });
    }

    @EventListener(VideoCreatedEvent.class)
    protected void videoCreatedEvent(VideoCreatedEvent event) {
        this.transactionTemplate.execute(t -> {
            final List<VideoPostProcessingUnit> processingUnits = this.postProcessingRepository.findByStatusAndVideoId(ProcessingStatus.PEDNING, event.getVideo().getId());
            this.processPostProcessing(processingUnits);
            return null;
        });
    }

    @Transactional
    public Page<Video> getAllVideos(Pageable pageable) {
        return this.videoRepository.findAll(pageable);
    }

    @Transactional
    public Page<Playlist> getAllPlaylists(Pageable pageable) {
        return this.playlistRepository.findAll(pageable);
    }

    @Transactional
    public Playlist createPlaylist(PlaylistDTO playlistDTO) {
        final Playlist playlist = new Playlist();
        playlist.setName(playlistDTO.getName());
        playlist.setDescription(playlistDTO.getDescription());
        playlist.setVideos(new ArrayList<>());
        return this.playlistRepository.save(playlist);
    }

    @Transactional
    public VideoToPlaylistResultDTO addVideoToPlayList(VideoToPlaylistDTO videoToPlaylistDTO) throws VideoServiceException {
        final VideoIdDTO videoId = this.idProcessingService.createVideoId(videoToPlaylistDTO.getVideoURI());
        final Optional<Playlist> playlist = this.playlistRepository.findById(videoToPlaylistDTO.getPlaylistID());
        if (!playlist.isPresent()) {
            throw new PlaylistNotFoundException("Playlist with id " + videoToPlaylistDTO.getPlaylistID() + " not found!");
        }
        final Optional<Video> video = this.videoRepository.findById(VideoId.create(videoId));
        if (!video.isPresent()) {
            final VideoPostProcessingUnit vppu = this.queueAddVideoToPlaylist(videoId, videoToPlaylistDTO.getPlaylistID());
            final VideoProcessingUnit vpu = this.processingService.queueVideo(videoId);
            return new VideoToPlaylistResultDTO(playlist.get().getId(), ProcessingStatus.PEDNING, videoId, vpu.getId());
        } else {
            final Playlist changedPlaylist = this.addVideoToPlaylist(video.get(), playlist.get());
            return new VideoToPlaylistResultDTO(changedPlaylist.getId(), ProcessingStatus.PROCESSED, videoId, null);
        }
    }

    private VideoPostProcessingUnit queuePostProcessing(VideoIdDTO videoIdDTO, VideoPostProcessingType type, PlaylistPostProcessDTO payload) throws VideoServiceException {
        VideoPostProcessingUnit vppu = new VideoPostProcessingUnit();
        vppu.setVideoId(VideoId.create(videoIdDTO));
        vppu.setType(type);
        vppu.setStatus(ProcessingStatus.PEDNING);
        try {
            vppu.setPayload(this.json.writeValueAsString(payload));
            vppu = this.postProcessingRepository.save(vppu);
            this.eventPublisher.publishEvent(new VideoPostProcessingCreated(this, vppu));
            return vppu;
        } catch (JsonProcessingException e) {
            throw new VideoServiceException(e);
        }
    }

    private VideoPostProcessingUnit queueAddVideoToPlaylist(VideoIdDTO videoIdDTO, long playlistID) throws VideoServiceException {
        return this.queuePostProcessing(videoIdDTO, VideoPostProcessingType.VIDEO_TO_PLAYLIST, new PlaylistPostProcessDTO(playlistID));
    }

    private Playlist addVideoToPlaylist(Video video, Playlist playlist) {
        playlist.getVideos().add(video);
        return this.playlistRepository.save(playlist);
    }

    private void processPostProcessing(Collection<VideoPostProcessingUnit> vppus) {
        for (final VideoPostProcessingUnit vppu : vppus) {
            if (vppu.getType() == VideoPostProcessingType.VIDEO_TO_PLAYLIST) {
                this.addVideoToPlaylist(vppu);
                vppu.setStatus(ProcessingStatus.PROCESSED);
                this.postProcessingRepository.save(vppu);
            }
        }
    }

    private void addVideoToPlaylist(final VideoPostProcessingUnit vppu) {
        final VideoId videoId = vppu.getVideoId();
        final PlaylistPostProcessDTO payload;
        try {
            payload = this.json.readValue(vppu.getPayload(), PlaylistPostProcessDTO.class);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        final long playlistID = payload.getPlaylistId();
        final Optional<Video> video = this.videoRepository.findById(videoId);
        final Optional<Playlist> playlist = this.playlistRepository.findById(playlistID);
        if (!video.isPresent()) {
            logger.info("Video with id {} not found (not processed?)", videoId);
            return;
        }
        if (!playlist.isPresent()) {
            logger.error("Playlist with id {} not found!", playlistID);
            return;
        }
        this.addVideoToPlaylist(video.get(), playlist.get());
    }
}
