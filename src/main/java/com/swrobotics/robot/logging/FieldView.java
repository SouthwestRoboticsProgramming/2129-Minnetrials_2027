package com.swrobotics.robot.logging;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.FieldObject2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public final class FieldView {
    private static final Field2d field = new Field2d();

    public static final FieldObject2d robotPose = field.getRobotObject();
    public static final FieldObject2d visionEstimates = field.getObject("Vision estimates");
    public static final FieldObject2d targetGoal = field.getObject("Target goal");

    public static void publish() {
        SmartDashboard.putData("Field View", field);
    }

    public static void setRobotPose(Pose2d pose) {
        robotPose.setPose(pose);
    }

    public static void setVisionPoses(Pose2d... poses) {
        visionEstimates.setPoses(poses);
    }

    public static void setTargetPose(Pose2d pose) {
        targetGoal.setPose(pose);
    }
}