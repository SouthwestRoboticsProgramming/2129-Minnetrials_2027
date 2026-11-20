package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.robot.config.FieldConstants;
import com.swrobotics.robot.config.RobotConstants;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;

public class DriveIOSim implements DriveIO {
    private final DifferentialDrivetrainSim sim;
    private double leftPercent = 0.0;
    private double rightPercent = 0.0;

    public DriveIOSim() {
        sim = new DifferentialDrivetrainSim(
                DCMotor.getCIM(2),
                RobotConstants.kDriveGearRatio,
                RobotConstants.kRobotMomentOfInertia,
                RobotConstants.kRobotMassKg,
                RobotConstants.kWheelRadiusMeters,
                RobotConstants.kTrackWidth,
                null
        );
        sim.setPose(FieldConstants.kBlueCenterStart);
    }

    @Override
    public void updateInputs(DriveIOInputs inputs) {
        double volts = RobotController.getBatteryVoltage();
        // Matches your DriveSubsystem sign convention: left = +forward, right = -forward
        sim.setInputs(leftPercent * volts, -rightPercent * volts);
        sim.update(0.02);

        inputs.pigeonConnected = true;
        inputs.gyroYaw = sim.getHeading();
        inputs.gyroYawRateDegPerSec = Math.toDegrees(
                (sim.getRightVelocityMetersPerSecond() - sim.getLeftVelocityMetersPerSecond())
                        / RobotConstants.kTrackWidth);

        inputs.leftOutputPercent = leftPercent;
        inputs.rightOutputPercent = rightPercent;
        inputs.leftCurrentAmps = sim.getLeftCurrentDrawAmps();
        inputs.rightCurrentAmps = sim.getRightCurrentDrawAmps();
        inputs.simLeftMeters = sim.getLeftPositionMeters();
        inputs.simRightMeters = sim.getRightPositionMeters();
        inputs.simPose = sim.getPose();
    }

    @Override
    public void setWheelOutputs(double leftPercent, double rightPercent) {
        this.leftPercent = leftPercent;
        this.rightPercent = rightPercent;
    }

    @Override
    public void resetSimPose(Pose2d pose) {
        sim.setPose(pose);
    }
}