package com.swrobotics.robot.control;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.commands.DriveCommands;
import com.swrobotics.robot.config.FieldConstants;
import com.swrobotics.robot.config.IOAllocation;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmSubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public final class ControlBoard extends SubsystemBase {

    private final NTEntry<Double> driverDeadband =
            new NTDouble("Controls/DriverDeadband", 0.1).setPersistent();

    private final RobotContainer robot;

    public final CommandXboxController driver;
    public final CommandXboxController operator;
    public final CommandXboxController tester;

    public ControlBoard(RobotContainer robot) {
        this.robot = robot;

        driver = new CommandXboxController(IOAllocation.DRIVER_CONTROLLER_PORT);
        operator = new CommandXboxController(IOAllocation.OPERATOR_CONTROLLER_PORT);
        tester = new CommandXboxController(IOAllocation.TESTER_CONTROLLER_PORT);

        configureControls();
    }

    private void configureControls() {
        // Default teleop arcade drive
        robot.drive.setDefaultCommand(
                robot.drive.arcadeDrive(
                        () -> MathUtil.applyDeadband(driver.getLeftY(), driverDeadband.get()),
                        () -> MathUtil.applyDeadband(driver.getRightX(), driverDeadband.get())
                )
        );

        // Driver Right Bumper: Aim at Cookie X/Y position while driver controls throttle
        driver.rightBumper().whileTrue(
                DriveCommands.aimAtTargetWhileDriving(
                        robot.drive,
                        FieldConstants::getCookieScorePos,
                        () -> MathUtil.applyDeadband(driver.getLeftY(), driverDeadband.get()))
        );

        // Driver Left Bumper: Aim at Milk X/Y position while driver controls throttle
        driver.leftBumper().whileTrue(
                DriveCommands.aimAtTargetWhileDriving(
                        robot.drive,
                        FieldConstants::getMilkScorePos,
                        () -> MathUtil.applyDeadband(driver.getLeftY(), driverDeadband.get()))
        );

        // Driver Start: Zero Pigeon 2 heading forward
        driver.start().onTrue(
                Commands.runOnce(() -> robot.drive.resetPose(
                        new Pose2d(robot.drive.getPose().getTranslation(), Rotation2d.kZero)))
        );

        // Operator Scoring Arm Controls (Matches README)
        operator.a().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.SCORE_COOKIE));
        operator.b().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.SCORE_MILK));
        operator.x().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.STOWING));
        operator.y().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.HOMING));

        // Operator Back: Calibrate current physical arm angle as 0 degrees
        operator.back().onTrue(
                Commands.runOnce(robot.scoringArm::calibrateCurrentPositionAsZero)
        );
    }
}