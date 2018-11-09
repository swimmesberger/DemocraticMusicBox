package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.domain.*;
import at.swimmesberger.musicbox.events.VideoCreatedEvent;
import at.swimmesberger.musicbox.events.VideoPostProcessingCreated;
import at.swimmesberger.musicbox.repository.PlaylistRepository;
import at.swimmesberger.musicbox.repository.VideoPostProcessingRepository;
import at.swimmesberger.musicbox.repository.VideoRepository;
import at.swimmesberger.musicbox.service.dto.*;
import at.swimmesberger.musicbox.service.errors.PlaylistNotFoundException;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import at.swimmesberger.musicbox.service.processing.VideoProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        logger.info("Processing initial post processing units");
        this.transactionTemplate.execute(t -> {
            final List<VideoPostProcessingUnit> processingUnits = this.postProcessingRepository.findByStatus(ProcessingStatus.PEDNING);
            this.processPostProcessing(processingUnits);
            return null;
        });
    }

    @EventListener(VideoPostProcessingCreated.class)
    protected void videoPostProcessingCreatedEvent(VideoPostProcessingCreated event) {
        logger.info("Handle post processing created event {}", event);
        this.transactionTemplate.execute(t -> {
            final Optional<VideoPostProcessingUnit> vppu = this.postProcessingRepository.findById(event.getVideoPostProcessingUnitId());
            if (vppu.isPresent()) {
                this.processPostProcessing(Collections.singleton(vppu.get()));
            } else {
                logger.info("VideoPostProcessingUnit not found for event {}", event);
            }
            return null;
        });
    }

    @EventListener(VideoCreatedEvent.class)
    protected void videoCreatedEvent(VideoCreatedEvent event) {
        logger.info("Handle video processed event {}", event);
        this.transactionTemplate.execute(t -> {
            final Optional<VideoProcessingUnit> processingUnit = this.processingService.getProcessingUnit(event.getVideoProcessingId());
            if (processingUnit.isPresent()) {
                final List<VideoPostProcessingUnit> postProcessingUnits = this.postProcessingRepository.findByStatusAndProcessingUnit(ProcessingStatus.PEDNING, processingUnit.get());
                this.processPostProcessing(postProcessingUnits);
            } else {
                logger.info("Skipping processing of event {} because the VideoProcessingUnit is not found", event);
            }
            return null;
        });
    }

    @Transactional
    public List<VideoReturnDTO> getAllVideos(URIRepresentationDTO rep) {
        final Map<VideoIdDTO, List<VideoReturnDTO>> processingUnits = this.processingService.getProcessedVideos();
        return new ArrayList<>(this.getVideos(processingUnits, rep).values());
    }

    @Transactional
    public VideoReturnDTO getReturnVideoByID(VideoIdDTO id, URIRepresentationDTO rep) {
        final Map<VideoIdDTO, VideoReturnDTO> videos = this.getVideos(Collections.singletonList(id), rep);
        return videos.get(id);
    }

    @Transactional
    public List<PlaylistDTO> getAllPlaylists(URIRepresentationDTO rep) {
        final List<Playlist> playlists = this.playlistRepository.findAll();
        final List<PlaylistDTO> playlistDtos = new ArrayList<>(playlists.size());
        for (final Playlist playlist : playlists) {
            playlistDtos.add(this.playlistToDTO(playlist, rep));
        }
        return playlistDtos;
    }

    @Transactional
    public PlaylistDTO createPlaylist(CreatePlaylistDTO playlistDTO) {
        Playlist playlist = new Playlist();
        playlist.setName(playlistDTO.getName());
        playlist.setDescription(playlistDTO.getDescription());
        playlist.setVideos(new ArrayList<>());
        playlist = this.playlistRepository.save(playlist);
        return this.playlistToDTO(playlist, null);
    }

    public PlaylistDTO addVideoToPlayList(VideoToPlaylistDTO videoToPlaylistDTO, URIRepresentationDTO rep) throws VideoServiceException {
        final List<VideoProcessingUnit> vpus = new ArrayList<>();
        final List<VideoPostProcessingUnit> vppus = new ArrayList<>();
        final PlaylistDTO returnPlaylist = this.transactionTemplate.execute(s -> {
            final VideoIdDTO videoId = this.idProcessingService.createVideoId(videoToPlaylistDTO.getVideoURI());
            final Optional<Playlist> playlist = this.playlistRepository.findById(videoToPlaylistDTO.getPlaylistID());
            if (!playlist.isPresent()) {
                throw new PlaylistNotFoundException("Playlist with id " + videoToPlaylistDTO.getPlaylistID() + " not found!");
            }
            final Optional<Video> video = this.videoRepository.findById(VideoId.create(videoId));
            if (!video.isPresent()) {
                final PlaylistDTO playlistDTO = this.playlistToDTO(playlist.get(), rep);

                final VideoProcessingUnit vpu = this.processingService.createProcesingVideo(videoId);
                final VideoPostProcessingUnit vppu = this.createAddVideoToPlaylist(vpu, videoToPlaylistDTO.getPlaylistID());
                vpus.add(vpu);
                vppus.add(vppu);

                final VideoReturnDTO videoReturnDTO = this.processingService.createReturnVideo(vpu);
                final Map<VideoIdDTO, List<VideoReturnDTO>> processedMap = ImmutableMap.of(videoId, Collections.singletonList(videoReturnDTO));
                final Map<VideoIdDTO, VideoReturnDTO> returnVideos = this.getVideos(processedMap, Collections.emptyList(), rep);
                return playlistDTO.addVideo(returnVideos.get(videoId));
            } else {
                final Playlist changedPlaylist = this.addVideoToPlaylist(video.get(), playlist.get());
                return this.playlistToDTO(changedPlaylist, rep);
            }
        });
        for(final VideoProcessingUnit vpu : vpus){
            this.processingService.queueVideo(vpu);
        }
        for(final VideoPostProcessingUnit vppu : vppus){
            this.queuePostProcessing(vppu);
        }
        return returnPlaylist;
    }

    public VideoIdDTO createVideoIdFromIdString(String idString) throws UnsupportedVideoPlatformException {
        return this.idProcessingService.createVideoIdFromIdString(idString);
    }

    private PlaylistDTO playlistToDTO(Playlist playlist, URIRepresentationDTO rep){
        return new PlaylistDTO(playlist.getId(), playlist.getName(), playlist.getDescription(), this.getVideos(playlist, rep));
    }

    private Map<VideoIdDTO, VideoReturnDTO> getVideos(List<VideoIdDTO> videoIds, URIRepresentationDTO rep) {
        final Map<VideoIdDTO, List<VideoReturnDTO>> processedVideos = this.processingService.getProcessedVideos(videoIds);
        return this.getVideos(processedVideos, rep);
    }

    private Map<VideoIdDTO, VideoReturnDTO> getVideos(Map<VideoIdDTO, List<VideoReturnDTO>> processedVideos, URIRepresentationDTO rep) {
        if (processedVideos.size() <= 0) return Collections.emptyMap();
        final Collection<VideoIdDTO> videoIds = processedVideos.keySet();
        final List<Video> videoEntities = this.videoRepository.findAllByIdIn(VideoId.create(videoIds));
        return this.getVideos(processedVideos, videoEntities, rep);
    }

    private Map<VideoIdDTO, VideoReturnDTO> getVideos(Map<VideoIdDTO, List<VideoReturnDTO>> processedVideos, List<Video> videoEntities, URIRepresentationDTO rep) {
        final Collection<VideoIdDTO> videoIds = processedVideos.keySet();
        final Map<VideoIdDTO, VideoReturnDTO> returnDTOMap = new HashMap<>(processedVideos.size());
        final Map<VideoIdDTO, VideoReturnDTO> videoMap = new HashMap<>(videoEntities.size());
        for (final Video v : videoEntities) {
            videoMap.put(v.getId().toDTO(), this.createVideoReturnDTO(v, null, rep));
        }
        for (final VideoIdDTO dto : videoIds) {
            VideoReturnDTO lastDto = processedVideos.get(dto).iterator().next();
            final VideoReturnDTO videoById = videoMap.get(dto);
            if (videoById != null) {
                lastDto = this.createVideoReturnDTO(videoById, lastDto);
            }
            returnDTOMap.put(dto, lastDto);
        }
        return returnDTOMap;
    }

    private List<VideoReturnDTO> getVideos(Playlist playlist, URIRepresentationDTO rep) {
        final List<Video> videoEntities = playlist.getVideos();
        if(videoEntities.size() <= 0){
            return Collections.emptyList();
        }
        final List<VideoIdDTO> dtoIds = Video.toVideoIdDTO(videoEntities);
        final Map<VideoIdDTO, List<VideoReturnDTO>> processedVideos = this.processingService.getProcessedVideos(dtoIds);
        return new ArrayList<>(this.getVideos(processedVideos, videoEntities, rep).values());
    }

    private URI translateURI(URI uri, URIRepresentationDTO rep) {
        if (rep.getType() == URIRepresentationType.INTERNAL) return uri;
        final UriComponents baseURI = rep.getBase();
        final Path p = Paths.get(uri);
        final String fileName = p.getFileName().toString();
        final String ext = FilenameUtils.getExtension(fileName);
        final String baseName = FilenameUtils.getBaseName(fileName);
        final String apiEndpoint;
        if (ext.equals("jpg")) {
            apiEndpoint = "thumbnail";
        } else {
            apiEndpoint = "video";
        }
        return UriComponentsBuilder.newInstance().uriComponents(baseURI).pathSegment("api", "resources", apiEndpoint, baseName).build().toUri();
    }

    private VideoReturnDTO createVideoReturnDTO(Video video, VideoReturnDTO previous, URIRepresentationDTO rep) {
        final VideoIdDTO idDTO = video.getId().toDTO();
        final URI videoURI = this.translateURI(URI.create(video.getVideoURI()), rep);
        final URI thumbnailURI = this.translateURI(URI.create(video.getThumbnailURI()), rep);
        return new VideoReturnDTO(idDTO, this.idProcessingService.createIdStringFromVideoId(idDTO), previous == null ? null : previous.getProcessingId(), previous == null ? null : previous.getProcessingTime(), ProcessingStatus.PROCESSED,
            new VideoMetadataDTO(video.getTitle(), video.getDescription(), videoURI, thumbnailURI));
    }

    private VideoReturnDTO createVideoReturnDTO(VideoReturnDTO video, VideoReturnDTO previous) {
        return new VideoReturnDTO(video.getId(), video.getIdString(), previous == null ? null : previous.getProcessingId(), previous == null ? null : previous.getProcessingTime(), ProcessingStatus.PROCESSED, video.getMetadata());
    }

    private void queuePostProcessing(VideoPostProcessingUnit vppu){
        this.eventPublisher.publishEvent(new VideoPostProcessingCreated(this, vppu.getId()));
    }

    private VideoPostProcessingUnit createPostProcessing(VideoProcessingUnit vpu, VideoPostProcessingType type, PlaylistPostProcessDTO payload) throws VideoServiceException {
        VideoPostProcessingUnit vppu = new VideoPostProcessingUnit();
        vppu.setProcessingUnit(vpu);
        vppu.setType(type);
        vppu.setStatus(ProcessingStatus.PEDNING);
        try {
            vppu.setPayload(this.json.writeValueAsString(payload));
            vppu = this.postProcessingRepository.save(vppu);
            return vppu;
        } catch (JsonProcessingException e) {
            throw new VideoServiceException(e);
        }
    }

    private VideoPostProcessingUnit createAddVideoToPlaylist(VideoProcessingUnit vpu, long playlistID) throws VideoServiceException {
        return this.createPostProcessing(vpu, VideoPostProcessingType.VIDEO_TO_PLAYLIST, new PlaylistPostProcessDTO(playlistID));
    }

    private Playlist addVideoToPlaylist(Video video, Playlist playlist) {
        playlist.getVideos().add(video);
        return this.playlistRepository.save(playlist);
    }

    private void processPostProcessing(Collection<VideoPostProcessingUnit> vppus) {
        for (final VideoPostProcessingUnit vppu : vppus) {
            if (vppu.getStatus() == ProcessingStatus.PROCESSED) {
                logger.info("Skipping {}", vppu);
                continue;
            }
            if (vppu.getType() == VideoPostProcessingType.VIDEO_TO_PLAYLIST) {
                if(this.addVideoToPlaylist(vppu)) {
                    vppu.setStatus(ProcessingStatus.PROCESSED);
                    this.postProcessingRepository.save(vppu);
                }
            }
        }
    }

    private boolean addVideoToPlaylist(final VideoPostProcessingUnit vppu) {
        final VideoId videoId = vppu.getProcessingUnit().getVideoId();
        final PlaylistPostProcessDTO payload;
        try {
            payload = this.json.readValue(vppu.getPayload(), PlaylistPostProcessDTO.class);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return true;
        }
        final long playlistID = payload.getPlaylistId();
        final Optional<Video> video = this.videoRepository.findById(videoId);
        final Optional<Playlist> playlist = this.playlistRepository.findById(playlistID);
        if (!video.isPresent()) {
            logger.info("Video with id {} not found (not processed?)", videoId);
            return false;
        }
        if (!playlist.isPresent()) {
            logger.error("Playlist with id {} not found!", playlistID);
            return true;
        }
        this.addVideoToPlaylist(video.get(), playlist.get());
        return true;
    }
}
