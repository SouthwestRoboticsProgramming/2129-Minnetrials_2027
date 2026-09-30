package com.swrobotics.robot.subsystems.drive;

import java.util.function.Supplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class DriveSubsystem extends SubsystemBase {

    private static final int kLeftMotor1ID = 1;
    private static final int kLeftMotor2ID = 2;
    private static final int kRightMotor1ID = 3;
    private static final int kRightMotor2ID = 4;

    private static final double kDesaturationThreshold = 0.6;

    private final NTEntry<Double> forwardScale = new NTDouble("Drivebase/ForwardScale", 0.5).setPersistent();
    private final NTEntry<Double> turnScale = new NTDouble("Drivebase/TurnScale", 0.5).setPersistent();
    private final NTEntry<Double> aimKP = new NTDouble("Drivebase/Aim_kP", 0.0111).setPersistent();

    private final TalonSRX leftMotor1 = new TalonSRX(kLeftMotor1ID);
    private final TalonSRX leftMotor2 = new TalonSRX(kLeftMotor2ID);
    private final TalonSRX rightMotor1 = new TalonSRX(kRightMotor1ID);
    private final TalonSRX rightMotor2 = new TalonSRX(kRightMotor2ID);

    public DriveSubsystem() {
    }

    public Command arcadeDrive(
            Supplier<Double> forwardSupplier,
            Supplier<Double> turnSupplier) {
        return run(() -> {
            double forward = forwardSupplier.get() * forwardScale.get();
            double turn = turnSupplier.get() * turnScale.get();

            setDriveOutputs(forward, turn);
        }).withName("Arcade Drive");
    }

    public Command autoAim(
            Supplier<Double> txSupplier,
            Supplier<Double> forwardSupplier) {
        return run(() -> {
            double tx = txSupplier.get();
            double forward = forwardSupplier.get();

            double turn = tx * aimKP.get();

            setDriveOutputs(forward, turn);
        }).withName("Auto Aim");
    }

    public Command stopCommand() {
        return runOnce(this::stop)
                .withName("Drive Stop");
    }

    public void stop() {
        leftMotor1.set(ControlMode.PercentOutput, 0.0);
        leftMotor2.set(ControlMode.PercentOutput, 0.0);
        rightMotor1.set(ControlMode.PercentOutput, 0.0);
        rightMotor2.set(ControlMode.PercentOutput, 0.0);
    }

    private void setDriveOutputs(double forward, double turn) {
        double leftWheels = forward + turn;
        double rightWheels = -forward + turn;

        double maxOutput = Math.max(
                Math.abs(leftWheels),
                Math.abs(rightWheels));

        if (maxOutput > kDesaturationThreshold) {
            leftWheels /= maxOutput;
            rightWheels /= maxOutput;
        }

        leftMotor1.set(ControlMode.PercentOutput, leftWheels);
        leftMotor2.set(ControlMode.PercentOutput, leftWheels);
        rightMotor1.set(ControlMode.PercentOutput, rightWheels);
        rightMotor2.set(ControlMode.PercentOutput, rightWheels);
    }
}