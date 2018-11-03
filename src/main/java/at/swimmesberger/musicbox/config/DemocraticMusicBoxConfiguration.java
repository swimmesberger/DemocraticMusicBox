package at.swimmesberger.musicbox.config;

import at.swimmesberger.musicbox.service.processing.YoutubeDlDriver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DemocraticMusicBoxConfiguration {
    @Bean
    public YoutubeDlDriver youtubeDlDriver(@Value("${processing.video.directory}") String videoDirectory, @Value("${processing.video.cache}") String videoCache, @Value("${processing.binary.directory:#{null}}") String binaryDirectory, ObjectMapper json) {
        final Optional<Path> binaryDirectoryOp = (binaryDirectory == null || binaryDirectory.isEmpty()) ? Optional.empty() : Optional.of(Paths.get(binaryDirectory));
        return new YoutubeDlDriver(binaryDirectoryOp, Paths.get(videoDirectory), Paths.get(videoCache), json);
    }

    @Bean(name = "blockingExecutor", destroyMethod = "shutdownNow")
    public ExecutorService blockingProcessorExecutor() {
        return Executors.newWorkStealingPool();
    }
}
