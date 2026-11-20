package com.swrobotics.robot.config;

public final class IOAllocation {
    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final int OPERATOR_CONTROLLER_PORT = 1;
    public static final int TESTER_CONTROLLER_PORT = 2;

    // Tank Drive TalonSRX CAN IDs
    public static final int kTLDriveMotor = 0;
    public static final int kTRDriveMotor = 1;
    public static final int kBLDriveMotor = 2;
    public static final int kBRDriveMotor = 3;

    // Pigeon 2 IMU CAN ID
    public static final int kPigeon2 = 4;

    // Scoring Arm TalonFX CAN IDs
    public static final int kScoringArmMotor = 5;
    public static final int kScoringArmEncoder = 6;

    public static final int kPDP = 50;
}