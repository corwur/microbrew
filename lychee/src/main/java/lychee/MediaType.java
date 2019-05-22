package lychee;

public enum MediaType {
    APPLICATION_JSON("application/json"),
    PLAIN_TEXT("plain/text");

    private final String mediaType;

    MediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}
