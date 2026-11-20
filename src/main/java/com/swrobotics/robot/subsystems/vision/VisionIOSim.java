package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj.Timer;

public class VisionIOSim implements VisionIO {
    private final DriveSubsystem drive;

    public VisionIOSim(DriveSubsystem drive) {
        this.drive = drive;
    }

    @Override
    public void updateInputs(VisionIOInputs inputs) {
        // In simulation, feed clean simulated field-map pose updates
        inputs.frontHasTarget = true;
        inputs.frontTagId = 7;
        inputs.frontTx = 0.0;
        inputs.frontPose = drive.getPose();
        inputs.frontTimestamp = Timer.getFPGATimestamp();
        inputs.frontTagCount = 2;
        inputs.frontAvgDist = 2.0;

        inputs.rearHasTarget = false;
        inputs.rearTagCount = 0;
    }
}