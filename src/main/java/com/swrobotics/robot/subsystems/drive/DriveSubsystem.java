package com.swrobotics.robot.subsystems.drive;

import java.util.function.Supplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.config.IOAllocation;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class DriveSubsystem extends SubsystemBase {

    private static final double kDesaturationThreshold = 0.6;

    private final NTEntry<Double> forwardScale;
    private final NTEntry<Double> turnScale;
    private final NTEntry<Double> aimKP;

    private final TalonSRX leftMotor1;
    private final TalonSRX leftMotor2;
    private final TalonSRX rightMotor1;
    private final TalonSRX rightMotor2;

    public DriveSubsystem() {
        forwardScale = new NTDouble("Drivebase/ForwardScale", 0.75).setPersistent();
        turnScale = new NTDouble("Drivebase/TurnScale", 0.75).setPersistent();
        aimKP = new NTDouble("Drivebase/Aim_kP", 0.0111).setPersistent();

        leftMotor1 = new TalonSRX(IOAllocation.kTLDriveMotor);
        leftMotor2 = new TalonSRX(IOAllocation.kBLDriveMotor);
        rightMotor1 = new TalonSRX(IOAllocation.kTRDriveMotor);
        rightMotor2 = new TalonSRX(IOAllocation.kBRDriveMotor);

        // Configure current limits to prevent motor burnout
        configureMotorLimits();
    }

    private void configureMotorLimits() {
        // TalonSRX only supports Supply current limits. 
        // Parameters: Enable, Continuous Limit (Amps), Peak Threshold (Amps), Peak Duration (Seconds)
        SupplyCurrentLimitConfiguration supplyLimit = new SupplyCurrentLimitConfiguration(true, 40, 60, 0.1);

        leftMotor1.configSupplyCurrentLimit(supplyLimit);
        leftMotor2.configSupplyCurrentLimit(supplyLimit);
        rightMotor1.configSupplyCurrentLimit(supplyLimit);
        rightMotor2.configSupplyCurrentLimit(supplyLimit);
    }

    // Command to drive the robot using arcade controls
    public Command arcadeDrive(Supplier<Double> forwardSupplier, Supplier<Double> turnSupplier) {
        return Commands.run(() -> {
            double forward = forwardSupplier.get() * forwardScale.get();
            double turn = turnSupplier.get() * turnScale.get();

            setDriveOutputs(forward, turn);
        }, this).withName("Arcade Drive");
    }

    // Command to automatically aim the robot using vision targets
    public Command autoAim(Supplier<Double> txSupplier, Supplier<Double> forwardSupplier) {
        return Commands.run(() -> {
            double tx = txSupplier.get();
            double forward = forwardSupplier.get();
            double turn = tx * aimKP.get();

            setDriveOutputs(forward, turn);
        }, this).withName("Auto Aim");
    }

    // Command to stop the drive base
    public Command stopCommand() {
        return Commands.runOnce(this::stop, this)
                .withName("Drive Stop");
    }

    public void stop() {
        leftMotor1.set(ControlMode.PercentOutput, 0.0);
        leftMotor2.set(ControlMode.PercentOutput, 0.0);
        rightMotor1.set(ControlMode.PercentOutput, 0.0);
        rightMotor2.set(ControlMode.PercentOutput, 0.0);
    }

    // Helper method to safely calculate and apply motor outputs
    private void setDriveOutputs(double forward, double turn) {
        double leftWheels = forward + turn;
        double rightWheels = -forward + turn;

        // Desaturate wheel speeds to prevent values greater than 1.0 (or threshold)
        double maxOutput = Math.max(
                Math.abs(leftWheels),
                Math.abs(rightWheels)
        );

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