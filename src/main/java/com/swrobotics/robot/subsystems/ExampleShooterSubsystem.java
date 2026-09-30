package com.swrobotics.robot.subsystems;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleShooterSubsystem extends SubsystemBase {
    public enum State {
        IDLE,
        SHOOT,
        SLOW_SHOOT,
        REVERSE
    }

    // Hardware constants
    private static final int kShooterMotorID = 6;
    private static final InvertedValue kShooterMotorInverted = InvertedValue.Clockwise_Positive;
    private static final NeutralModeValue kNeutralMode = NeutralModeValue.Coast;

    // PID tunables
    // P: corrects current speed error.
    // I: corrects a speed error that stays for a long time.
    // D: reduces speed overshoot and fast changes.
    private final NTEntry<Double> kP = new NTDouble("ExampleShooter/kP", 0.12).setPersistent();
    private final NTEntry<Double> kI = new NTDouble("ExampleShooter/kI", 0.0).setPersistent();
    private final NTEntry<Double> kD = new NTDouble("ExampleShooter/kD", 0.0).setPersistent();

    // State tunables
    private final NTEntry<Double> shootVelocityRps = new NTDouble("ExampleShooter/ShootVelocityRPS", 60.0).setPersistent();
    private final NTEntry<Double> slowShootVelocityRps = new NTDouble("ExampleShooter/SlowShootVelocityRPS", 30.0).setPersistent();
    private final NTEntry<Double> reverseVoltage = new NTDouble("ExampleShooter/ReverseVoltage", -3.0).setPersistent();
    private final NTEntry<Double> atSpeedToleranceRps = new NTDouble("ExampleShooter/AtSpeedToleranceRPS", 3.0).setPersistent();

    private final TalonFX ShooterMotor = new TalonFX(kShooterMotorID);
    private final VelocityVoltage velocityControl = new VelocityVoltage(0.0);
    private final VoltageOut voltageControl = new VoltageOut(0.0);

    private State targetState = State.IDLE;

    public ExampleShooterSubsystem() {
        TalonFXConfiguration topConfig = new TalonFXConfiguration();

        topConfig.MotorOutput.Inverted = kShooterMotorInverted;
        topConfig.MotorOutput.NeutralMode = kNeutralMode;
        topConfig.Slot0.kP = kP.get();
        topConfig.Slot0.kI = kI.get();
        topConfig.Slot0.kD = kD.get();


        ShooterMotor.getConfigurator().apply(topConfig);

        setDefaultCommand(commandSetState(State.IDLE));
    }

    @Override
    public void periodic() {
        double targetVelocityRps = 0.0;
        double targetVoltage = 0.0;

        switch (targetState) {
            case IDLE:
                ShooterMotor.stopMotor();
                break;

            case SHOOT:
                targetVelocityRps = shootVelocityRps.get();
                ShooterMotor.setControl(velocityControl.withVelocity(targetVelocityRps));
                break;

            case SLOW_SHOOT:
                targetVelocityRps = slowShootVelocityRps.get();
                ShooterMotor.setControl(velocityControl.withVelocity(targetVelocityRps));
                break;

            case REVERSE:
                targetVoltage = reverseVoltage.get();
                ShooterMotor.setControl(voltageControl.withOutput(targetVoltage));
                break;
        }

        double topVelocityRps = ShooterMotor.getVelocity().getValueAsDouble();

        SmartDashboard.putString("ExampleShooter/State", targetState.name());
        SmartDashboard.putNumber("ExampleShooter/TargetVelocityRPS", targetVelocityRps);
        SmartDashboard.putNumber("ExampleShooter/TargetVoltage", targetVoltage);
        SmartDashboard.putNumber("ExampleShooter/VelocityRPS", topVelocityRps);
        SmartDashboard.putBoolean("ExampleShooter/AtSpeed", isAtSpeed());
        SmartDashboard.putNumber("ExampleShooter/MotorVoltage", ShooterMotor.getMotorVoltage().getValueAsDouble());
        SmartDashboard.putNumber("ExampleShooter/TemperatureCelsius", ShooterMotor.getDeviceTemp().getValueAsDouble());
    }

    public void setTargetState(State state) {
        targetState = state;
    }

    public boolean isAtSpeed() {
        if (targetState != State.SHOOT && targetState != State.SLOW_SHOOT) {
            return false;
        }

        double wantedSpeedRps = targetState == State.SHOOT ? shootVelocityRps.get() : slowShootVelocityRps.get();

        return MathUtil.isNear(wantedSpeedRps, Math.abs(ShooterMotor.getVelocity().getValueAsDouble()), atSpeedToleranceRps.get());
    }

    public Command commandSetState(State state) {
        return Commands.run(() -> setTargetState(state), this).withName("Shooter " + state.name());
    }
}