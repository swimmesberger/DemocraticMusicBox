package at.swimmesberger.musicbox.service.processing;

import at.swimmesberger.musicbox.service.dto.VideoUnit;
import at.swimmesberger.musicbox.service.errors.VideoProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeDlDriver {
    private final Logger logger = LoggerFactory.getLogger(YoutubeDlDriver.class);
    private final Optional<Path> binaryDirectory;
    private final Path videoDirectory;
    private final Path cacheDirectory;
    private final Pattern progressPattern;
    private final ObjectMapper json;

    public YoutubeDlDriver(Optional<Path> binaryDirectory, Path videoDirectory, Path cacheDirectory, ObjectMapper json) {
        this.binaryDirectory = Objects.requireNonNull(binaryDirectory);
        this.videoDirectory = Objects.requireNonNull(videoDirectory);
        this.cacheDirectory = Objects.requireNonNull(cacheDirectory);
        this.json = Objects.requireNonNull(json);
        this.progressPattern = Pattern.compile("\\[download\\]\\s+(?<percent>\\d{1,3}\\.\\d{1,5}).*");
    }

    public ProcessedVideo getVideo(VideoUnit unit) throws VideoProcessingException {
        final String videoIdString = unit.getIdString();
        try {
            final Path extMetaPath = this.videoDirectory.resolve(videoIdString + ".ext.info.json");
            final Path metaPath = this.videoDirectory.resolve(videoIdString + ".info.json");
            VideoMetadata metadata;
            if (Files.exists(extMetaPath)) {
                try (final Reader reader = Files.newBufferedReader(extMetaPath)) {
                    metadata = this.json.readValue(reader, VideoMetadata.class);
                }
            }else{
                if (!Files.exists(metaPath)) {
                    logger.info("No metadata file found at {}", metaPath);
                    return null;
                }
                logger.info("No metadata override file found at {}", extMetaPath);
                try (final Reader reader = Files.newBufferedReader(metaPath)) {
                    metadata = this.json.readValue(reader, VideoMetadata.class);
                }
            }
            final Path thumbPath = this.videoDirectory.resolve(videoIdString + ".jpg");
            Path videoPath = this.getVideoFile(videoIdString, metadata);
            if (!Files.exists(videoPath)) {
                logger.info("{} video not found like specified in metadata file trying different file extension (can happen when file is muxed)", videoPath);
                metadata = metadata.withExt("mkv");
                videoPath = this.videoDirectory.resolve(videoIdString + "." + "mkv");
                if (Files.exists(videoPath)) {
                    logger.info("Writing metadata override file to {}", extMetaPath);
                    try(final Writer writer = Files.newBufferedWriter(extMetaPath)){
                        this.json.writeValue(writer, metadata);
                    }
                }else{
                    throw new VideoProcessingException("No video file found");
                }
            }
            return new ProcessedVideo(unit, videoPath.toUri(), thumbPath.toUri(), metadata);
        } catch (IOException ioEx) {
            throw new VideoProcessingException(ioEx);
        }
    }

    public ProcessedVideo downloadVideo(VideoUnit unit, DoubleConsumer progress) throws VideoProcessingException {
        final ProcessedVideo cachedVideo = this.getVideo(unit);
        if (cachedVideo != null) {
            return cachedVideo;
        }

        final String binary;
        if (this.binaryDirectory.isPresent()) {
            binary = this.binaryDirectory.get().resolve("youtube-dl").toString();
        } else {
            binary = "youtube-dl";
        }

        final String videoIdString = unit.getIdString();
        final String url = unit.getUri().toString();

        final String videosPath = this.videoDirectory.resolve(videoIdString + "." + "%(ext)s").toString();
        final String cachePath = this.cacheDirectory.toString();
        final ProcessExecutor pExec = new ProcessExecutor().command(binary, "--write-info-json", "--newline", "--cache-dir", cachePath,
            "--no-call-home", "--write-thumbnail", "-f", "bestvideo+bestaudio/best", "-o", videosPath, url).redirectOutput(new LogOutputStream() {
            @Override
            protected void processLine(String line) {
                final Matcher matcher = progressPattern.matcher(line);
                if (matcher.matches()) {
                    final double pDouble = Double.parseDouble(matcher.group("percent"));
                    progress.accept(pDouble);
                }
                logger.info(line);
            }
        });
        try {
            final int exitValue = pExec.executeNoTimeout().getExitValue();
            progress.accept(100);
            if (exitValue != 0) {
                throw new VideoProcessingException("Error exit: " + exitValue);
            }
            return this.getVideo(unit);
        } catch (IOException e) {
            throw new VideoProcessingException(e);
        } catch (InterruptedException e) {
            throw new VideoProcessingException(e);
        }
    }

    public Path getVideoDirectory() {
        return videoDirectory;
    }

    public Path getCacheDirectory() {
        return cacheDirectory;
    }


    private Path getVideoFile(String videoIdString, VideoMetadata metadata){
        return this.videoDirectory.resolve(videoIdString + "." + metadata.getExt());
    }
}
