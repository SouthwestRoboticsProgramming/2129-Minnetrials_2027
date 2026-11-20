package com.swrobotics.robot.config;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public final class FieldConstants {
    public static final double kFieldLengthMeters = 16.54;
    public static final double kFieldWidthMeters = 8.21;

    // Blue Alliance Scoring Coordinates (Update X/Y to match your MinneTrials 2026 field map)
    public static final Translation2d kBlueCookieScorePos = new Translation2d(3.2, 5.5);
    public static final Translation2d kBlueMilkScorePos = new Translation2d(3.2, 2.7);
    public static final Translation2d kBlueRampPos = new Translation2d(4.8, 4.1);

    // Red Alliance Scoring Coordinates
    public static final Translation2d kRedCookieScorePos = new Translation2d(13.34, 5.5);
    public static final Translation2d kRedMilkScorePos = new Translation2d(13.34, 2.7);
    public static final Translation2d kRedRampPos = new Translation2d(11.74, 4.1);

    // Starting Poses
    public static final Pose2d kBlueCenterStart = new Pose2d(2.0, 4.1, Rotation2d.fromDegrees(0));
    public static final Pose2d kRedCenterStart = new Pose2d(14.54, 4.1, Rotation2d.fromDegrees(180));

    public static boolean isRedAlliance() {
        return DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red;
    }

    public static Translation2d getCookieScorePos() {
        return isRedAlliance() ? kRedCookieScorePos : kBlueCookieScorePos;
    }

    public static Translation2d getMilkScorePos() {
        return isRedAlliance() ? kRedMilkScorePos : kBlueMilkScorePos;
    }

    public static Translation2d getRampPos() {
        return isRedAlliance() ? kRedRampPos : kBlueRampPos;
    }

    public static Pose2d getDefaultStartPose() {
        return isRedAlliance() ? kRedCenterStart : kBlueCenterStart;
    }
}