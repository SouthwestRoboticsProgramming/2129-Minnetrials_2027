package com.swrobotics.robot.commands;

import com.swrobotics.robot.logging.FieldView;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class DriveCommands {

    private DriveCommands() {}

    /**
     * Turns the tank drive toward an (X, Y) field coordinate and drives forward until within stopDistanceMeters.
     */
    public static Command turnAndDriveToPosition(
            DriveSubsystem drive,
            Supplier<Translation2d> targetPosSupplier,
            double stopDistanceMeters) {

        PIDController turnPid = new PIDController(0.015, 0.0, 0.001);
        turnPid.enableContinuousInput(-180.0, 180.0);
        turnPid.setTolerance(2.0); // degrees

        PIDController drivePid = new PIDController(0.65, 0.0, 0.0);
        drivePid.setTolerance(0.08); // meters

        return Commands.run(() -> {
            drive.setState(DriveSubsystem.State.AUTO_DRIVE);
            Pose2d currentPose = drive.getPose();
            Translation2d targetPos = targetPosSupplier.get();

            double dx = targetPos.getX() - currentPose.getX();
            double dy = targetPos.getY() - currentPose.getY();
            double targetAngleDeg = Math.toDegrees(Math.atan2(dy, dx));
            double distanceToTarget = currentPose.getTranslation().getDistance(targetPos);

            FieldView.setTargetPose(new Pose2d(targetPos, Rotation2d.fromDegrees(targetAngleDeg)));
            SmartDashboard.putNumber("AutoAlign/TargetAngleDeg", targetAngleDeg);
            SmartDashboard.putNumber("AutoAlign/DistanceRemainingM", distanceToTarget - stopDistanceMeters);

            double turnSpeed = MathUtil.clamp(
                    turnPid.calculate(currentPose.getRotation().getDegrees(), targetAngleDeg), -0.5, 0.5);

            // Drive forward once aimed within 12 degrees of the target
            double forwardSpeed = 0.0;
            if (Math.abs(turnPid.getPositionError()) < 12.0) {
                forwardSpeed = MathUtil.clamp(
                        -drivePid.calculate(distanceToTarget, stopDistanceMeters), -0.6, 0.6);
            }

            drive.drive(forwardSpeed, turnSpeed);
        }, drive)
        .until(() -> {
            double dist = drive.getPose().getTranslation().getDistance(targetPosSupplier.get());
            return Math.abs(dist - stopDistanceMeters) <= 0.08 && turnPid.atSetpoint();
        })
        .finallyDo(interrupted -> drive.stop())
        .withName("TurnAndDriveToPosition");
    }

    /**
     * Aims the robot continuously at an (X, Y) scoring coordinate while the driver controls forward/backward throttle.
     */
    public static Command aimAtTargetWhileDriving(
            DriveSubsystem drive,
            Supplier<Translation2d> targetPosSupplier,
            DoubleSupplier forwardThrottleSupplier) {

        PIDController turnPid = new PIDController(0.015, 0.0, 0.001);
        turnPid.enableContinuousInput(-180.0, 180.0);
        turnPid.setTolerance(1.5);

        return Commands.run(() -> {
            drive.setState(DriveSubsystem.State.AUTO_AIM);
            Pose2d currentPose = drive.getPose();
            Translation2d targetPos = targetPosSupplier.get();

            double dx = targetPos.getX() - currentPose.getX();
            double dy = targetPos.getY() - currentPose.getY();
            double targetAngleDeg = Math.toDegrees(Math.atan2(dy, dx));

            FieldView.setTargetPose(new Pose2d(targetPos, Rotation2d.fromDegrees(targetAngleDeg)));
            SmartDashboard.putNumber("AutoAlign/TargetAngleDeg", targetAngleDeg);
            SmartDashboard.putBoolean("AutoAlign/Aimed", turnPid.atSetpoint());

            double turnSpeed = MathUtil.clamp(
                    turnPid.calculate(currentPose.getRotation().getDegrees(), targetAngleDeg), -0.5, 0.5);

            drive.drive(-forwardThrottleSupplier.getAsDouble() * 0.75, turnSpeed);
        }, drive)
        .finallyDo(interrupted -> drive.stop())
        .withName("AimAtTargetWhileDriving");
    }

    /**
     * Turns in place to an exact Pigeon 2 heading.
     */
    public static Command turnToHeading(DriveSubsystem drive, double targetHeadingDeg) {
        PIDController turnPid = new PIDController(0.015, 0.0, 0.001);
        turnPid.enableContinuousInput(-180.0, 180.0);
        turnPid.setTolerance(2.0);

        return Commands.run(() -> {
            drive.setState(DriveSubsystem.State.AUTO_DRIVE);
            double turnSpeed = MathUtil.clamp(
                    turnPid.calculate(drive.getHeading().getDegrees(), targetHeadingDeg), -0.5, 0.5);
            drive.drive(0.0, turnSpeed);
        }, drive)
        .until(turnPid::atSetpoint)
        .finallyDo(interrupted -> drive.stop())
        .withName("TurnToHeading");
    }

    /**
     * Drives straight at a constant percent output while holding a Pigeon 2 angle.
     */
    public static Command driveStraightTimed(
            DriveSubsystem drive, double forwardPercent, double holdHeadingDeg, double seconds) {
        PIDController turnPid = new PIDController(0.015, 0.0, 0.001);
        turnPid.enableContinuousInput(-180.0, 180.0);

        return Commands.run(() -> {
            drive.setState(DriveSubsystem.State.AUTO_DRIVE);
            double turnSpeed = MathUtil.clamp(
                    turnPid.calculate(drive.getHeading().getDegrees(), holdHeadingDeg), -0.4, 0.4);
            drive.drive(forwardPercent, turnSpeed);
        }, drive)
        .withTimeout(seconds)
        .finallyDo(interrupted -> drive.stop())
        .withName("DriveStraightTimed");
    }
}