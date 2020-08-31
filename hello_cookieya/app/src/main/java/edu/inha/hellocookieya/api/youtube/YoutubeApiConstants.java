package edu.inha.hellocookieya.api.youtube;

public class YoutubeApiConstants {
    public static String apiHostAddress = "https://www.googleapis.com/youtube/v3";
    public static String requestResourceType = "/videos?";
    public static String partialResourceRequest = "&part=snippet&fields=pageInfo,items(id,snippet(title,description))";

    public static String thumbnailApiHostAddress = "https://img.youtube.com/vi/";
    public static String defaultImage = "/default.jpg";
    public static String medium = "/mqdefault.jpg";
    public static String highImage = "/hqdefault.jpg";
    public static String standardImage = "/sddefault.jpg";
    public static String masresImage = "/maxresdefault.jpg";
}
