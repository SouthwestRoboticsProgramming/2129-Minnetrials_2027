package com.swrobotics.robot;

import com.swrobotics.robot.control.ControlBoard;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
public class RobotContainer {

  public final DriveSubsystem drive;
  public final ScoringArmSubsystem scoringArm;
  public final ControlBoard controlboard;

  public RobotContainer() {

    drive = new DriveSubsystem();
    scoringArm = new ScoringArmSubsystem();
    controlboard = new ControlBoard(this);

  }

  public void disabledInit() {

  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
  
}
