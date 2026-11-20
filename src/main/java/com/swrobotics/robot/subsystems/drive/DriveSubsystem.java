package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.config.FieldConstants;
import com.swrobotics.robot.config.RobotConstants;
import com.swrobotics.robot.logging.FieldView;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

import java.util.function.Supplier;

public class DriveSubsystem extends SubsystemBase {

    public enum State {
        TELEOP_DRIVE,
        AUTO_AIM,
        AUTO_DRIVE,
        STOPPED
    }

    private static final double kDesaturationThreshold = 0.85;

    private final DriveIO io;
    private final DriveIO.DriveIOInputs inputs = new DriveIO.DriveIOInputs();

    private final NTEntry<Double> forwardScale = new NTDouble("Drivebase/ForwardScale", 0.75).setPersistent();
    private final NTEntry<Double> turnScale = new NTDouble("Drivebase/TurnScale", 0.75).setPersistent();

    private final DifferentialDrivePoseEstimator poseEstimator;
    private State currentState = State.TELEOP_DRIVE;

    public DriveSubsystem(DriveIO io) {
        this.io = io;

        DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(RobotConstants.kTrackWidth);
        poseEstimator = new DifferentialDrivePoseEstimator(
                kinematics,
                Rotation2d.kZero,
                0.0,
                0.0,
                FieldConstants.kBlueCenterStart,
                VecBuilder.fill(0.05, 0.05, Math.toRadians(1.0)),
                VecBuilder.fill(0.35, 0.35, Math.toRadians(30.0))
        );
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Drivebase", inputs);

        // Update pose estimator: uses simulated wheels in Sim, and Pigeon 2 + Limelight X/Y on Real robot
        if (RobotBase.isSimulation()) {
            poseEstimator.update(inputs.gyroYaw, inputs.simLeftMeters, inputs.simRightMeters);
        } else {
            poseEstimator.update(inputs.gyroYaw, 0.0, 0.0);
        }

        Pose2d currentPose = getPose();
        FieldView.setRobotPose(currentPose);

        // AdvantageKit & SmartDashboard Logging for Elastic
        Logger.recordOutput("Drivebase/State", currentState.name());
        Logger.recordOutput("Drivebase/Pose", currentPose);

        SmartDashboard.putString("Drivebase/State", currentState.name());
        SmartDashboard.putNumber("Drivebase/HeadingDeg", getHeading().getDegrees());
        SmartDashboard.putNumber("Drivebase/PoseX", currentPose.getX());
        SmartDashboard.putNumber("Drivebase/PoseY", currentPose.getY());
        SmartDashboard.putNumber("Drivebase/LeftOutput", inputs.leftOutputPercent);
        SmartDashboard.putNumber("Drivebase/RightOutput", inputs.rightOutputPercent);
        SmartDashboard.putNumber("Drivebase/LeftCurrentAmps", inputs.leftCurrentAmps);
        SmartDashboard.putNumber("Drivebase/RightCurrentAmps", inputs.rightCurrentAmps);
        SmartDashboard.putBoolean("Drivebase/PigeonConnected", inputs.pigeonConnected);
    }

    public void setState(State state) {
        currentState = state;
    }

    public Pose2d getPose() {
        return poseEstimator.getEstimatedPosition();
    }

    public Rotation2d getHeading() {
        return inputs.gyroYaw;
    }

    public void resetPose(Pose2d pose) {
        io.resetGyroYaw(pose.getRotation().getDegrees());
        io.resetSimPose(pose);
        poseEstimator.resetPosition(pose.getRotation(), inputs.simLeftMeters, inputs.simRightMeters, pose);
        FieldView.setRobotPose(pose);
    }

    public void addVisionMeasurement(Pose2d visionPose, double timestampSeconds, Matrix<N3, N1> stdDevs) {
        poseEstimator.addVisionMeasurement(visionPose, timestampSeconds, stdDevs);
    }

    public Command arcadeDrive(Supplier<Double> forwardSupplier, Supplier<Double> turnSupplier) {
        return Commands.run(() -> {
            setState(State.TELEOP_DRIVE);
            double forward = -forwardSupplier.get() * forwardScale.get();
            double turn = turnSupplier.get() * turnScale.get();
            drive(forward, turn);
        }, this).withName("Arcade Drive");
    }

    public void drive(double forward, double turn) {
        double leftWheels = forward + turn;
        double rightWheels = -forward + turn;

        double maxOutput = Math.max(Math.abs(leftWheels), Math.abs(rightWheels));
        if (maxOutput > kDesaturationThreshold) {
            leftWheels = (leftWheels / maxOutput) * kDesaturationThreshold;
            rightWheels = (rightWheels / maxOutput) * kDesaturationThreshold;
        }

        io.setWheelOutputs(leftWheels, rightWheels);
    }

    public Command stopCommand() {
        return Commands.runOnce(this::stop, this).withName("Drive Stop");
    }

    public void stop() {
        setState(State.STOPPED);
        io.setWheelOutputs(0.0, 0.0);
    }
}