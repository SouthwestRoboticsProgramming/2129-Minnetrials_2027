package com.swrobotics.robot;

import com.swrobotics.robot.commands.AutonomousCommands;
import com.swrobotics.robot.control.ControlBoard;
import com.swrobotics.robot.logging.FieldView;
import com.swrobotics.robot.subsystems.drive.DriveIOReal;
import com.swrobotics.robot.subsystems.drive.DriveIOSim;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmIOReal;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmIOSim;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmSubsystem;
import com.swrobotics.robot.subsystems.vision.VisionIOLimelight;
import com.swrobotics.robot.subsystems.vision.VisionIOSim;
import com.swrobotics.robot.subsystems.vision.VisionSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class RobotContainer {

    public final DriveSubsystem drive;
    public final VisionSubsystem vision;
    public final ScoringArmSubsystem scoringArm;
    public final ControlBoard controlBoard;

    private final SendableChooser<Command> autoChooser;

    public RobotContainer() {
        if (Robot.isReal()) {
            drive = new DriveSubsystem(new DriveIOReal());
            vision = new VisionSubsystem(new VisionIOLimelight(), drive);
            scoringArm = new ScoringArmSubsystem(new ScoringArmIOReal());
        } else {
            drive = new DriveSubsystem(new DriveIOSim());
            vision = new VisionSubsystem(new VisionIOSim(drive), drive);
            scoringArm = new ScoringArmSubsystem(new ScoringArmIOSim());
        }

        controlBoard = new ControlBoard(this);

        FieldView.publish();

        autoChooser = new SendableChooser<>();
        autoChooser.setDefaultOption("1. Mobility Only",
                AutonomousCommands.mobilityOnly(drive, scoringArm));
        autoChooser.addOption("2. Score Cookie & Leave",
                AutonomousCommands.scoreCookieAndLeave(drive, scoringArm));
        autoChooser.addOption("3. Score Milk & Ramp Park",
                AutonomousCommands.scoreMilkAndRampPark(drive, scoringArm));
        autoChooser.addOption("Do Nothing", Commands.none());

        SmartDashboard.putData("Auto Selector", autoChooser);
    }

    public void disabledInit() {
        drive.stop();
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }
}