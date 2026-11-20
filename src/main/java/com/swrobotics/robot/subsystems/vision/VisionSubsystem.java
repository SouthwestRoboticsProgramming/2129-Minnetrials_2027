package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.robot.logging.FieldView;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

import java.util.ArrayList;
import java.util.List;

public class VisionSubsystem extends SubsystemBase {
    private final VisionIO io;
    private final VisionIO.VisionIOInputs inputs = new VisionIO.VisionIOInputs();
    private final DriveSubsystem drive;

    private double lastFrontTimestamp = -1.0;
    private double lastRearTimestamp = -1.0;

    public VisionSubsystem(VisionIO io, DriveSubsystem drive) {
        this.io = io;
        this.drive = drive;
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Vision", inputs);

        List<Pose2d> visibleVisionPoses = new ArrayList<>();

        // Front Limelight field-map update
        if (inputs.frontTagCount > 0 && inputs.frontTimestamp != lastFrontTimestamp) {
            lastFrontTimestamp = inputs.frontTimestamp;
            double xyStdDev = (inputs.frontAvgDist * inputs.frontAvgDist) / inputs.frontTagCount;
            // 99999 on rotation = trust Pigeon 2 gyro for heading, use Limelight for X/Y
            drive.addVisionMeasurement(inputs.frontPose, inputs.frontTimestamp, VecBuilder.fill(xyStdDev, xyStdDev, 99999.0));
            visibleVisionPoses.add(inputs.frontPose);
        }

        // Rear Limelight field-map update (optional second camera)
        if (inputs.rearTagCount > 0 && inputs.rearTimestamp != lastRearTimestamp) {
            lastRearTimestamp = inputs.rearTimestamp;
            double xyStdDev = (inputs.rearAvgDist * inputs.rearAvgDist) / inputs.rearTagCount;
            drive.addVisionMeasurement(inputs.rearPose, inputs.rearTimestamp, VecBuilder.fill(xyStdDev, xyStdDev, 99999.0));
            visibleVisionPoses.add(inputs.rearPose);
        }

        FieldView.setVisionPoses(visibleVisionPoses.toArray(new Pose2d[0]));

        // Log Limelight visibility to SmartDashboard / Elastic
        SmartDashboard.putBoolean("Vision/FrontHasTarget", inputs.frontHasTarget);
        SmartDashboard.putNumber("Vision/FrontTagID", inputs.frontTagId);
        SmartDashboard.putNumber("Vision/FrontTxDeg", inputs.frontTx);
        SmartDashboard.putBoolean("Vision/RearHasTarget", inputs.rearHasTarget);
        SmartDashboard.putNumber("Vision/RearTagID", inputs.rearTagId);
    }
}