package com.swrobotics.robot.subsystems.scoringArm;

public class ScoringArmIOSim implements ScoringArmIO {
    private double simRotations = 0.0;
    private double targetRotations = 0.0;
    private double appliedVolts = 0.0;
    private boolean openLoop = false;

    @Override
    public void updateInputs(ScoringArmIOInputs inputs) {
        double prev = simRotations;
        if (openLoop) {
            simRotations = Math.max(0.0, simRotations + (appliedVolts * 0.01));
        } else {
            simRotations += (targetRotations - simRotations) * 0.2;
            appliedVolts = (targetRotations - simRotations) * 2.0;
        }

        inputs.rawRotations = simRotations;
        inputs.velocityRps = (simRotations - prev) / 0.02;
        inputs.appliedVolts = appliedVolts;
        inputs.statorCurrentAmps = Math.abs(appliedVolts) * 2.5;
        inputs.tempCelsius = 26.0;
    }

    @Override
    public void setPositionRotations(double rotations) {
        openLoop = false;
        targetRotations = rotations;
    }

    @Override
    public void setVoltage(double volts) {
        openLoop = true;
        appliedVolts = volts;
    }

    @Override
    public void resetEncoderPosition(double rotations) {
        simRotations = rotations;
        targetRotations = rotations;
    }
}