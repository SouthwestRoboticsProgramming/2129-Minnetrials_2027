package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.robot.config.RobotConstants;
import edu.wpi.first.networktables.NetworkTableInstance;

public class VisionIOLimelight implements VisionIO {
    private final LimelightCamera frontCamera;
    private final LimelightCamera rearCamera;

    public VisionIOLimelight() {
        frontCamera = new LimelightCamera(RobotConstants.kFrontLimelightName);
        rearCamera = new LimelightCamera(RobotConstants.kRearLimelightName);

        // Publish MJPEG stream URLs so Elastic's Camera widget finds them automatically
        NetworkTableInstance.getDefault()
                .getTable("CameraPublisher")
                .getSubTable("LimelightFront")
                .getStringArrayTopic("streams")
                .publish()
                .set(new String[]{"mjpg:http://" + RobotConstants.kFrontLimelightName + ".local:5800/?action=stream"});

        NetworkTableInstance.getDefault()
                .getTable("CameraPublisher")
                .getSubTable("LimelightRear")
                .getStringArrayTopic("streams")
                .publish()
                .set(new String[]{"mjpg:http://" + RobotConstants.kRearLimelightName + ".local:5800/?action=stream"});
    }

    @Override
    public void updateInputs(VisionIOInputs inputs) {
        inputs.frontHasTarget = frontCamera.hasTarget();
        inputs.frontTagId = frontCamera.getTagId();
        inputs.frontTx = frontCamera.getTx();
        LimelightCamera.PoseEstimate frontEst = frontCamera.getLatestPoseEstimate();
        if (frontEst != null) {
            inputs.frontPose = frontEst.pose();
            inputs.frontTimestamp = frontEst.timestamp();
            inputs.frontTagCount = frontEst.tagCount();
            inputs.frontAvgDist = frontEst.avgTagDist();
        } else {
            inputs.frontTagCount = 0;
        }

        inputs.rearHasTarget = rearCamera.hasTarget();
        inputs.rearTagId = rearCamera.getTagId();
        LimelightCamera.PoseEstimate rearEst = rearCamera.getLatestPoseEstimate();
        if (rearEst != null) {
            inputs.rearPose = rearEst.pose();
            inputs.rearTimestamp = rearEst.timestamp();
            inputs.rearTagCount = rearEst.tagCount();
            inputs.rearAvgDist = rearEst.avgTagDist();
        } else {
            inputs.rearTagCount = 0;
        }
    }
}