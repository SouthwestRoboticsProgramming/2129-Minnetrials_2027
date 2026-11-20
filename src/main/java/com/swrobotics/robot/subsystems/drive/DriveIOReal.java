package com.swrobotics.robot.subsystems.drive;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.swrobotics.robot.config.IOAllocation;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public class DriveIOReal implements DriveIO {
    private final TalonSRX leftMotor1;
    private final TalonSRX leftMotor2;
    private final TalonSRX rightMotor1;
    private final TalonSRX rightMotor2;

    private final Pigeon2 pigeon;
    private final StatusSignal<Angle> yawSignal;
    private final StatusSignal<AngularVelocity> yawRateSignal;

    private double lastLeftPercent = 0.0;
    private double lastRightPercent = 0.0;

    public DriveIOReal() {
        leftMotor1 = new TalonSRX(IOAllocation.kTLDriveMotor);
        leftMotor2 = new TalonSRX(IOAllocation.kBLDriveMotor);
        rightMotor1 = new TalonSRX(IOAllocation.kTRDriveMotor);
        rightMotor2 = new TalonSRX(IOAllocation.kBRDriveMotor);

        SupplyCurrentLimitConfiguration limit = new SupplyCurrentLimitConfiguration(true, 40, 60, 0.1);
        for (TalonSRX motor : new TalonSRX[]{leftMotor1, leftMotor2, rightMotor1, rightMotor2}) {
            motor.configFactoryDefault();
            motor.configSupplyCurrentLimit(limit);
            motor.setNeutralMode(NeutralMode.Brake);
        }

        pigeon = new Pigeon2(IOAllocation.kPigeon2);
        pigeon.getConfigurator().apply(new Pigeon2Configuration());
        yawSignal = pigeon.getYaw();
        yawRateSignal = pigeon.getAngularVelocityZWorld();
    }

    @Override
    public void updateInputs(DriveIOInputs inputs) {
        inputs.pigeonConnected = BaseStatusSignal.refreshAll(yawSignal, yawRateSignal).isOK();
        inputs.gyroYaw = Rotation2d.fromDegrees(yawSignal.getValueAsDouble());
        inputs.gyroYawRateDegPerSec = yawRateSignal.getValueAsDouble();

        inputs.leftOutputPercent = lastLeftPercent;
        inputs.rightOutputPercent = lastRightPercent;
        inputs.leftCurrentAmps = leftMotor1.getSupplyCurrent() + leftMotor2.getSupplyCurrent();
        inputs.rightCurrentAmps = rightMotor1.getSupplyCurrent() + rightMotor2.getSupplyCurrent();
    }

    @Override
    public void setWheelOutputs(double leftPercent, double rightPercent) {
        lastLeftPercent = leftPercent;
        lastRightPercent = rightPercent;

        leftMotor1.set(ControlMode.PercentOutput, leftPercent);
        leftMotor2.set(ControlMode.PercentOutput, leftPercent);
        rightMotor1.set(ControlMode.PercentOutput, rightPercent);
        rightMotor2.set(ControlMode.PercentOutput, rightPercent);
    }

    @Override
    public void resetGyroYaw(double yawDegrees) {
        pigeon.setYaw(yawDegrees);
    }
}