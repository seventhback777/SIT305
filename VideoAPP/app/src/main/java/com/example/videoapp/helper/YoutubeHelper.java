package com.example.videoapp.helper;

import android.net.Uri;

/**
 * Handles YouTube URL parsing and iFrame HTML generation.
 * Supports standard, short, and embed URL formats.
 */
public class YoutubeHelper {

    /**
     * Extracts the YouTube video ID from various URL formats:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://youtu.be/VIDEO_ID
     * - https://www.youtube.com/embed/VIDEO_ID
     *
     * @return video ID string, or null if the URL is not a valid YouTube URL
     */
    public static String extractVideoId(String url) {
        if (url == null || url.trim().isEmpty()) return null;

        try {
            Uri uri = Uri.parse(url.trim());
            String host = uri.getHost();
            if (host == null) return null;

            // youtu.be/VIDEO_ID
            if (host.contains("youtu.be")) {
                String path = uri.getPath();
                if (path != null && path.length() > 1) {
                    return path.substring(1); // strip leading /
                }
            }

            // youtube.com/watch?v=VIDEO_ID
            if (host.contains("youtube.com")) {
                String path = uri.getPath();
                if (path != null && path.startsWith("/embed/")) {
                    return path.substring(7);
                }
                return uri.getQueryParameter("v");
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Returns the mobile YouTube watch URL for direct WebView loading.
     */
    public static String buildWatchUrl(String videoId) {
        return "https://m.youtube.com/watch?v=" + videoId;
    }
}
