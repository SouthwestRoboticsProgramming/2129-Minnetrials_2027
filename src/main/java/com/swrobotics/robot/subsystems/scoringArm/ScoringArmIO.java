package com.swrobotics.robot.subsystems.scoringArm;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public interface ScoringArmIO {
    class ScoringArmIOInputs implements LoggableInputs {
        public double rawRotations = 0.0;
        public double velocityRps = 0.0;
        public double appliedVolts = 0.0;
        public double statorCurrentAmps = 0.0;
        public double tempCelsius = 0.0;

        @Override
        public void toLog(LogTable table) {
            table.put("RawRotations", rawRotations);
            table.put("VelocityRPS", velocityRps);
            table.put("AppliedVolts", appliedVolts);
            table.put("StatorCurrentAmps", statorCurrentAmps);
            table.put("TempCelsius", tempCelsius);
        }

        @Override
        public void fromLog(LogTable table) {
            rawRotations = table.get("RawRotations", rawRotations);
            velocityRps = table.get("VelocityRPS", velocityRps);
            appliedVolts = table.get("AppliedVolts", appliedVolts);
            statorCurrentAmps = table.get("StatorCurrentAmps", statorCurrentAmps);
            tempCelsius = table.get("TempCelsius", tempCelsius);
        }
    }

    default void updateInputs(ScoringArmIOInputs inputs) {}

    default void setPositionRotations(double rotations) {}

    default void setVoltage(double volts) {}

    default void resetEncoderPosition(double rotations) {}

    default void setBrakeMode(boolean brake) {}
}