package at.swimmesberger.musicbox.service.youtube;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class YoutubeConfig {
    private final YoutubeConfigArgs args;

    @JsonCreator
    public YoutubeConfig(@JsonProperty("args") YoutubeConfigArgs args) {
        this.args = args;
    }

    public YoutubeConfigArgs getArgs() {
        return args;
    }

    public static class YoutubeConfigArgs {
        private final String thumbnailUrl;
        private final String keywords;
        private final String author;
        private final String title;
        private final String urlEncodedFmtStreamMap;
        private final String fmtList;

        @JsonCreator
        public YoutubeConfigArgs(@JsonProperty("thumbnail_url") String thumbnailUrl, @JsonProperty("keywords") String keywords, @JsonProperty("author") String author, @JsonProperty("title") String title, @JsonProperty("url_encoded_fmt_stream_map") String urlEncodedFmtStreamMap, @JsonProperty("fmt_list") String fmtList) {
            this.thumbnailUrl = thumbnailUrl;
            this.keywords = keywords;
            this.author = author;
            this.title = title;
            this.urlEncodedFmtStreamMap = urlEncodedFmtStreamMap;
            this.fmtList = fmtList;
        }

        @JsonProperty("thumbnail_url")
        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public String getKeywords() {
            return keywords;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        @JsonProperty("url_encoded_fmt_stream_map")
        public String getUrlEncodedFmtStreamMap() {
            return urlEncodedFmtStreamMap;
        }

        @JsonProperty("fmt_list")
        public String getFmtList() {
            return fmtList;
        }
    }
}
