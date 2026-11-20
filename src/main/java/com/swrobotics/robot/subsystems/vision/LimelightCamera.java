package com.swrobotics.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;

public final class LimelightCamera {
    private final NetworkTable table;
    private final String name;

    public record PoseEstimate(Pose2d pose, double timestamp, int tagCount, double avgTagDist) {}

    public LimelightCamera(String name) {
        this.name = name;
        this.table = NetworkTableInstance.getDefault().getTable(name);
    }

    public PoseEstimate getLatestPoseEstimate() {
        double[] data = table.getEntry("botpose_wpiblue").getDoubleArray(new double[0]);
        if (data.length < 11) return null;

        double x = data[0];
        double y = data[1];
        double yaw = data[5];
        double latencyMs = data[6];
        int tagCount = (int) data[7];
        double avgTagDist = data[9];

        if (tagCount == 0 || (x == 0.0 && y == 0.0)) return null;

        Pose2d pose = new Pose2d(x, y, Rotation2d.fromDegrees(yaw));
        double timestamp = Timer.getFPGATimestamp() - (latencyMs / 1000.0);

        return new PoseEstimate(pose, timestamp, tagCount, avgTagDist);
    }

    public boolean hasTarget() {
        return table.getEntry("tv").getDouble(0.0) == 1.0;
    }

    public int getTagId() {
        return (int) table.getEntry("tid").getDouble(-1.0);
    }

    public double getTx() {
        return table.getEntry("tx").getDouble(0.0);
    }

    public String getName() {
        return name;
    }
}