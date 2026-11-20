package com.swrobotics.robot.config;

import edu.wpi.first.math.util.Units;

public final class RobotConstants {
    public static final double kMaxDriveSpeed = 3.0; // m/s
    public static final double kMaxDriveAcceleration = 2.0; // m/s^2
    public static final double kMaxDriveAngularSpeed = Math.PI; // rad/s
    public static final double kMaxDriveAngularAcceleration = Math.PI; // rad/s^2
    public static final double kTrackWidth = 0.6; // meters

    // Physics parameters for DifferentialDrivetrainSim
    public static final double kDriveGearRatio = 8.45;
    public static final double kWheelRadiusMeters = Units.inchesToMeters(3.0);
    public static final double kRobotMassKg = 45.0;
    public static final double kRobotMomentOfInertia = 4.5;

    // Limelight NetworkTables names (must match names in Limelight web UI)
    public static final String kFrontLimelightName = "limelight-front";
    public static final String kRearLimelightName = "limelight-rear";
}