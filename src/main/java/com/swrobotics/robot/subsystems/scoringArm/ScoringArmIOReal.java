package com.swrobotics.robot.subsystems.scoringArm;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.swrobotics.robot.config.IOAllocation;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;

public class ScoringArmIOReal implements ScoringArmIO {
    private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
    private static final double kGearReduction = 174.0;

    private final TalonFX motor;
    private final MotionMagicVoltage positionRequest = new MotionMagicVoltage(0.0);
    private final VoltageOut voltageRequest = new VoltageOut(0.0);

    private final StatusSignal<Angle> positionSignal;
    private final StatusSignal<AngularVelocity> velocitySignal;
    private final StatusSignal<Voltage> voltageSignal;
    private final StatusSignal<Current> currentSignal;
    private final StatusSignal<Temperature> tempSignal;

    public ScoringArmIOReal() {
        motor = new TalonFX(IOAllocation.CAN.kScoringArmMotor);

        TalonFXConfiguration config = new TalonFXConfiguration();
        config.MotorOutput.Inverted = kMotorInverted;
        config.MotorOutput.NeutralMode = NeutralModeValue.Coast;

        config.CurrentLimits.SupplyCurrentLimit = 40.0;
        config.CurrentLimits.SupplyCurrentLimitEnable = true;
        config.CurrentLimits.StatorCurrentLimit = 80.0;
        config.CurrentLimits.StatorCurrentLimitEnable = true;

        config.Slot0.kP = 1.6;
        config.Slot0.kI = 1.0;
        config.Slot0.kD = 0.1;

        config.MotionMagic.MotionMagicCruiseVelocity = 50;
        config.MotionMagic.MotionMagicAcceleration = 100;
        config.MotionMagic.MotionMagicJerk = 400;

        config.Feedback.SensorToMechanismRatio = 1.0;
        config.Feedback.RotorToSensorRatio = kGearReduction;

        motor.getConfigurator().apply(config);

        positionSignal = motor.getPosition();
        velocitySignal = motor.getVelocity();
        voltageSignal = motor.getMotorVoltage();
        currentSignal = motor.getStatorCurrent();
        tempSignal = motor.getDeviceTemp();
    }

    @Override
    public void updateInputs(ScoringArmIOInputs inputs) {
        BaseStatusSignal.refreshAll(positionSignal, velocitySignal, voltageSignal, currentSignal, tempSignal);
        inputs.rawRotations = positionSignal.getValueAsDouble();
        inputs.velocityRps = velocitySignal.getValueAsDouble();
        inputs.appliedVolts = voltageSignal.getValueAsDouble();
        inputs.statorCurrentAmps = currentSignal.getValueAsDouble();
        inputs.tempCelsius = tempSignal.getValueAsDouble();
    }

    @Override
    public void setPositionRotations(double rotations) {
        motor.setControl(positionRequest.withPosition(rotations));
    }

    @Override
    public void setVoltage(double volts) {
        motor.setControl(voltageRequest.withOutput(volts));
    }

    @Override
    public void resetEncoderPosition(double rotations) {
        motor.setPosition(rotations);
    }

    @Override
    public void setBrakeMode(boolean brake) {
        MotorOutputConfigs output = new MotorOutputConfigs();
        output.Inverted = kMotorInverted;
        output.NeutralMode = brake ? NeutralModeValue.Brake : NeutralModeValue.Coast;
        motor.getConfigurator().apply(output);
    }
}