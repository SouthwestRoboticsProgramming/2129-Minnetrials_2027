package com.swrobotics.robot.subsystems;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleArmSubsystem extends SubsystemBase {
    public enum State {
        UP,
        DOWN
    }

    // Hardware constants
    private static final int kMotorID = 5;
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
    private final NTEntry<Double> upPositionRotations = new NTDouble("ExampleArm/UpPositionRotations", 0.0).setPersistent();
    private final NTEntry<Double> downPositionRotations = new NTDouble("ExampleArm/DownPositionRotations", 20.0).setPersistent();
    private final NTEntry<Double> positionToleranceRotations = new NTDouble("ExampleArm/PositionToleranceRotations", 0.25).setPersistent();

    private final TalonFX armMotor = new TalonFX(kMotorID);
    private final MotionMagicVoltage motionMagicControl = new MotionMagicVoltage(0.0);

    private State targetState = State.UP;

    public ExampleArmSubsystem() {
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
            case UP:
                targetPositionRotations = upPositionRotations.get();
                break;

            case DOWN:
                targetPositionRotations = downPositionRotations.get();
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

    public boolean isAtTarget() {
        double targetPositionRotations = targetState == State.UP ? upPositionRotations.get() : downPositionRotations.get();
        return Math.abs(targetPositionRotations - armMotor.getPosition().getValueAsDouble()) <= positionToleranceRotations.get();
    }

    public Command commandSetState(State state) {
        return Commands.run(() -> setTargetState(state), this).withName("Arm " + state.name());
    }
}