package com.smartdesk.service.face;

public record FaceVerificationResult(
        boolean success,
        String message,
        double claimedUserScore,
        double nearestOtherUserScore
) {

    public static FaceVerificationResult success(String message, double claimedUserScore, double nearestOtherUserScore) {
        return new FaceVerificationResult(true, message, claimedUserScore, nearestOtherUserScore);
    }

    public static FaceVerificationResult failure(String message, double claimedUserScore, double nearestOtherUserScore) {
        return new FaceVerificationResult(false, message, claimedUserScore, nearestOtherUserScore);
    }
}
