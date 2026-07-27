package com.smartdesk.service.face;

public record FaceCaptureResult(boolean success, String message) {

    public static FaceCaptureResult success(String message) {
        return new FaceCaptureResult(true, message);
    }

    public static FaceCaptureResult failure(String message) {
        return new FaceCaptureResult(false, message);
    }
}
