package com.syndicati.utils.image.imagekit;

/**
 * Minimal upload response we care about.
 */
public record ImageKitUploadResult(
    String fileId,
    String url,
    String name,
    String filePath
) {}

