package com.swrobotics.robot.subsystems.scoringArm;

import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.config.IOAllocation;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// Import the AdvantageKit Logger
import org.littletonrobotics.junction.Logger;

public class ScoringArmSubsystem extends SubsystemBase {
    
    public enum State {
        SCORE_MILK,
        SCORE_COOKIE,
        STOWING,
        HOMING
    }

    private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
    private static final NeutralModeValue kNeutralMode = NeutralModeValue.Coast;
    private static final double kGearReduction = 174.0; // Motor rotations per 1 arm rotation

    private final NTEntry<Double> positionToleranceRotations = new NTDouble("ScoringArm/PositionToleranceRotations", 2.0).setPersistent();

    private final TalonFX scoringArmMotor;
    private final MotionMagicVoltage request = new MotionMagicVoltage(0.0);
    private final VoltageOut voltageControl = new VoltageOut(0.0);
    private final Timer homingTimer = new Timer();

    private State targetState;

    private boolean wasEnabled = false; 

    public ScoringArmSubsystem() {
        scoringArmMotor = new TalonFX(IOAllocation.kScoringArmMotor);
        
        targetState = State.HOMING;
        TalonFXConfiguration config = new TalonFXConfiguration();

        config.MotorOutput.Inverted = kMotorInverted;
        config.MotorOutput.NeutralMode = kNeutralMode;

        config.CurrentLimits.SupplyCurrentLimit = 40.0;
        config.CurrentLimits.SupplyCurrentLimitEnable = true;  
        config.CurrentLimits.StatorCurrentLimit = 80.0;
        config.CurrentLimits.StatorCurrentLimitEnable = true;

        config.Slot0.kP = 1.6;
        config.Slot0.kI = 1;
        config.Slot0.kD = 0.1;

        config.MotionMagic.MotionMagicCruiseVelocity = 50;
        config.MotionMagic.MotionMagicAcceleration = 100;
        config.MotionMagic.MotionMagicJerk = 400;

        config.Feedback.SensorToMechanismRatio = 1.0; 
        config.Feedback.RotorToSensorRatio = kGearReduction;

        scoringArmMotor.getConfigurator().apply(config);
    }

    @Override
    public void periodic() {
        boolean isEnabled = DriverStation.isEnabled();

        if (isEnabled != wasEnabled) {
            wasEnabled = isEnabled;
            
            MotorOutputConfigs outputConfigs = new MotorOutputConfigs();
            outputConfigs.Inverted = InvertedValue.Clockwise_Positive; 
            outputConfigs.NeutralMode = isEnabled ? NeutralModeValue.Brake : NeutralModeValue.Coast;
            
            scoringArmMotor.getConfigurator().apply(outputConfigs);
        }

        double targetPositionRotations = 0.0;

        switch (targetState) {
            case SCORE_MILK:
                targetPositionRotations = 10;
                break;
            case SCORE_COOKIE:
                targetPositionRotations = 15;
                break;
            case STOWING:
                scoringArmMotor.setControl(request.withPosition(targetPositionRotations));
                targetPositionRotations = 0.0;
                break;
            case HOMING:
                scoringArmMotor.setControl(voltageControl.withOutput(-3.0)); 
                
                double current = scoringArmMotor.getStatorCurrent().getValueAsDouble();
                boolean bypassedInrush = homingTimer.hasElapsed(0.75);
                boolean isStalled = current > 20.0; 

                if (bypassedInrush || isStalled) {
                    scoringArmMotor.setPosition(0.0); 
                    targetPositionRotations = 0.0;
                    targetState = State.STOWING;
                break;
                }
            }   

        scoringArmMotor.setControl(request.withPosition(targetPositionRotations));

        double actualPositionRotations = scoringArmMotor.getPosition().getValueAsDouble();
        double actualVelocityRps = scoringArmMotor.getVelocity().getValueAsDouble();

        
        Logger.recordOutput("ScoringArm/State", targetState.name());
        Logger.recordOutput("ScoringArm/TargetPositionRotations", targetPositionRotations);
        Logger.recordOutput("ScoringArm/ActualPositionRotations", actualPositionRotations);
        Logger.recordOutput("ScoringArm/AtTarget", Math.abs(targetPositionRotations - actualPositionRotations) <= positionToleranceRotations.get());
        Logger.recordOutput("ScoringArm/VelocityRPS", actualVelocityRps);
        Logger.recordOutput("ScoringArm/MotorVoltage", scoringArmMotor.getMotorVoltage().getValueAsDouble());
        Logger.recordOutput("ScoringArm/TemperatureCelsius", scoringArmMotor.getDeviceTemp().getValueAsDouble());
    }

    public void setTargetState(State state) {
        targetState = state;
    }

    // Command to set state of the Scoring Arm
    public Command commandSetState(State state) {
        return Commands.run(() -> setTargetState(state), this)
                .withName("Arm " + state.name());
    }
}