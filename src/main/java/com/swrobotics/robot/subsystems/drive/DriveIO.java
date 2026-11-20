package com.swrobotics.robot.subsystems.drive;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public interface DriveIO {
    class DriveIOInputs implements LoggableInputs {
        public double leftOutputPercent = 0.0;
        public double rightOutputPercent = 0.0;
        public double leftCurrentAmps = 0.0;
        public double rightCurrentAmps = 0.0;

        public boolean pigeonConnected = false;
        public Rotation2d gyroYaw = Rotation2d.kZero;
        public double gyroYawRateDegPerSec = 0.0;

        // Used in simulation to track wheel travel & ground-truth pose
        public double simLeftMeters = 0.0;
        public double simRightMeters = 0.0;
        public Pose2d simPose = Pose2d.kZero;

        @Override
        public void toLog(LogTable table) {
            table.put("LeftOutputPercent", leftOutputPercent);
            table.put("RightOutputPercent", rightOutputPercent);
            table.put("LeftCurrentAmps", leftCurrentAmps);
            table.put("RightCurrentAmps", rightCurrentAmps);
            table.put("PigeonConnected", pigeonConnected);
            table.put("GyroYawDeg", gyroYaw.getDegrees());
            table.put("GyroYawRateDegPerSec", gyroYawRateDegPerSec);
            table.put("SimLeftMeters", simLeftMeters);
            table.put("SimRightMeters", simRightMeters);
            table.put("SimPose", simPose);
        }

        @Override
        public void fromLog(LogTable table) {
            leftOutputPercent = table.get("LeftOutputPercent", leftOutputPercent);
            rightOutputPercent = table.get("RightOutputPercent", rightOutputPercent);
            leftCurrentAmps = table.get("LeftCurrentAmps", leftCurrentAmps);
            rightCurrentAmps = table.get("RightCurrentAmps", rightCurrentAmps);
            pigeonConnected = table.get("PigeonConnected", pigeonConnected);
            gyroYaw = Rotation2d.fromDegrees(table.get("GyroYawDeg", gyroYaw.getDegrees()));
            gyroYawRateDegPerSec = table.get("GyroYawRateDegPerSec", gyroYawRateDegPerSec);
            simLeftMeters = table.get("SimLeftMeters", simLeftMeters);
            simRightMeters = table.get("SimRightMeters", simRightMeters);
            simPose = table.get("SimPose", simPose);
        }
    }

    default void updateInputs(DriveIOInputs inputs) {}

    default void setWheelOutputs(double leftPercent, double rightPercent) {}

    default void resetGyroYaw(double yawDegrees) {}

    default void resetSimPose(Pose2d pose) {}
}