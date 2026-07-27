package com.smartdesk.service.face;

public enum FaceEnrollmentPose {
    FRONT("Front", "Look straight at the camera."),
    LEFT("Left Angle", "Turn your face slightly to the left."),
    RIGHT("Right Angle", "Turn your face slightly to the right."),
    UP("Upward", "Lift your chin slightly upward."),
    DOWN("Downward", "Lower your chin slightly downward.");

    private final String label;
    private final String instruction;

    FaceEnrollmentPose(String label, String instruction) {
        this.label = label;
        this.instruction = instruction;
    }

    public String label() {
        return label;
    }

    public String instruction() {
        return instruction;
    }

    public String fileName() {
        return name().toLowerCase() + ".png";
    }
}
