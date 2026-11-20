package com.swrobotics.robot.commands;

import com.swrobotics.robot.config.FieldConstants;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public final class AutonomousCommands {

    private AutonomousCommands() {}

    /** Placeholder 1: Drive straight out of starting zone using Pigeon 2 heading hold */
    public static Command mobilityOnly(DriveSubsystem drive, ScoringArmSubsystem arm) {
        return Commands.sequence(
                Commands.runOnce(() -> drive.resetPose(FieldConstants.getDefaultStartPose())),
                arm.commandSetState(ScoringArmSubsystem.State.STOWING),
                DriveCommands.driveStraightTimed(drive, 0.45, FieldConstants.getDefaultStartPose().getRotation().getDegrees(), 2.2),
                drive.stopCommand()
        ).withName("Auto: Mobility Only");
    }

    /** Placeholder 2: Turn & drive to Cookie X/Y target, raise arm, score, and back out */
    public static Command scoreCookieAndLeave(DriveSubsystem drive, ScoringArmSubsystem arm) {
        return Commands.sequence(
                Commands.runOnce(() -> drive.resetPose(FieldConstants.getDefaultStartPose())),
                arm.commandSetState(ScoringArmSubsystem.State.HOMING),
                Commands.waitUntil(arm::isAtTarget).withTimeout(1.5),
                DriveCommands.turnAndDriveToPosition(drive, FieldConstants::getCookieScorePos, 0.85)
                        .withTimeout(4.0),
                arm.commandSetState(ScoringArmSubsystem.State.SCORE_COOKIE),
                Commands.waitUntil(arm::isAtTarget).withTimeout(1.5),
                Commands.waitSeconds(0.5),
                arm.commandSetState(ScoringArmSubsystem.State.STOWING),
                DriveCommands.driveStraightTimed(drive, -0.45, drive.getHeading().getDegrees(), 1.5),
                drive.stopCommand()
        ).withName("Auto: Score Cookie & Leave");
    }

    /** Placeholder 3: Score Milk X/Y target, turn to Ramp X/Y coordinate, and park on ramp */
    public static Command scoreMilkAndRampPark(DriveSubsystem drive, ScoringArmSubsystem arm) {
        return Commands.sequence(
                Commands.runOnce(() -> drive.resetPose(FieldConstants.getDefaultStartPose())),
                DriveCommands.turnAndDriveToPosition(drive, FieldConstants::getMilkScorePos, 1.0)
                        .withTimeout(4.0),
                arm.commandSetState(ScoringArmSubsystem.State.SCORE_MILK),
                Commands.waitUntil(arm::isAtTarget).withTimeout(1.5),
                Commands.waitSeconds(0.5),
                arm.commandSetState(ScoringArmSubsystem.State.STOWING),
                DriveCommands.turnAndDriveToPosition(drive, FieldConstants::getRampPos, 0.15)
                        .withTimeout(4.0),
                drive.stopCommand()
        ).withName("Auto: Score Milk & Ramp Park");
    }
}