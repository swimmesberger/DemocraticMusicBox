package at.swimmesberger.musicbox.service.youtube;

import at.swimmesberger.musicbox.DemocraticMusicBoxApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemocraticMusicBoxApp.class)
public class YoutubeServiceTest {
    @Autowired
    private YoutubeService youtubeService;

    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    public void testYoutubeDirectURIContruction() throws IOException {
        final YoutubeConfig config;
        try (final InputStream in = YoutubeServiceTest.class.getResourceAsStream("youtube_config.json")) {
            config = this.jsonMapper.readValue(in, YoutubeConfig.class);
        }
        final URI videoURI = this.youtubeService.getDirectVideoURI(config);
        System.out.println(videoURI);
    }
}
