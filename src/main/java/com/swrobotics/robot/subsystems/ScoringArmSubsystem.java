package com.swrobotics.robot.subsystems;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.config.IOAllocation;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ScoringArmSubsystem extends SubsystemBase {
    
    public enum State {
        
        SCORE_MILK,
        SCORE_COOKIE,
        STOWING,
        ORIGIN
    
    }

    
    private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
    private static final NeutralModeValue kNeutralMode = NeutralModeValue.Brake;

    // PID tunables
    // P: increases the speed of the arm to reach the target position. if you have a lot of overshoot, decrease this. if you have a lot of oscillation, decrease this.
    // I: fixes small error that remains for a long time. honestly, you probably don't need this.
    // D: reduces overshoot and oscillation from P. if you have a lot of overshoot, increase this. if you have a lot of oscillation, increase this.
    private final NTEntry<Double> kP = new NTDouble("ExampleArm/kP", 2.0).setPersistent();
    private final NTEntry<Double> kI = new NTDouble("ExampleArm/kI", 0.0).setPersistent();
    private final NTEntry<Double> kD = new NTDouble("ExampleArm/kD", 0.1).setPersistent();

    // Position tunables
    private final NTEntry<Double> Score_MilkPositionRotations = new NTDouble("ExampleArm/Score_MilkPositionRotations", 0.0).setPersistent();
    private final NTEntry<Double> Score_CookiePositionRotations = new NTDouble("ExampleArm/Score_CookiePositionRotations", 20.0).setPersistent();
    private final NTEntry<Double> StowingPositionRotations = new NTDouble("ExampleArm/StowingPositionRotations", 0.0).setPersistent();
    private final NTEntry<Double> OriginPositionRotations = new NTDouble("ExampleArm/OriginPositionRotations", 20.0).setPersistent();
    private final NTEntry<Double> positionToleranceRotations = new NTDouble("ExampleArm/PositionToleranceRotations", 0.25).setPersistent();

    private final TalonFX armMotor = new TalonFX(IOAllocation.kExampleArmMotor);
    private final MotionMagicVoltage motionMagicControl = new MotionMagicVoltage(0.0);

    private State targetState = State.ORIGIN;

    public ScoringArmSubsystem() {
        TalonFXConfiguration config = new TalonFXConfiguration();

        config.MotorOutput.Inverted = kMotorInverted;
        config.MotorOutput.NeutralMode = kNeutralMode;

        config.Slot0.kP = kP.get();
        config.Slot0.kI = kI.get();
        config.Slot0.kD = kD.get();

        armMotor.getConfigurator().apply(config);
    }

    @Override
    public void periodic() {
        double targetPositionRotations = 0.0;

        switch (targetState) {
            case SCORE_MILK:
                targetPositionRotations = Score_MilkPositionRotations.get();
                break;

            case SCORE_COOKIE:
                targetPositionRotations = Score_CookiePositionRotations.get();
                break;
            case STOWING:
                targetPositionRotations = StowingPositionRotations.get();
                break;

            case ORIGIN:
                targetPositionRotations = OriginPositionRotations.get();
                break;
        }

        armMotor.setControl(motionMagicControl.withPosition(targetPositionRotations));

        double actualPositionRotations = armMotor.getPosition().getValueAsDouble();
        double actualVelocityRps = armMotor.getVelocity().getValueAsDouble();

        SmartDashboard.putString("ExampleArm/State", targetState.name());
        SmartDashboard.putNumber("ExampleArm/TargetPositionRotations", targetPositionRotations);
        SmartDashboard.putNumber("ExampleArm/ActualPositionRotations", actualPositionRotations);
        SmartDashboard.putBoolean("ExampleArm/AtTarget", Math.abs(targetPositionRotations - actualPositionRotations) <= positionToleranceRotations.get());
        SmartDashboard.putNumber("ExampleArm/VelocityRPS", actualVelocityRps);
        SmartDashboard.putNumber("ExampleArm/MotorVoltage", armMotor.getMotorVoltage().getValueAsDouble());
        SmartDashboard.putNumber("ExampleArm/TemperatureCelsius", armMotor.getDeviceTemp().getValueAsDouble());
    }

    public void setTargetState(State state) {
        targetState = state;
    }


    public Command commandSetState(State state) {
        return Commands.run(() -> setTargetState(state), this).withName("Arm " + state.name());
    }
}