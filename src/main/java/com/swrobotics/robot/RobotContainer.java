// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.swrobotics.robot;

import com.swrobotics.robot.control.ControlBoard;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;
import com.swrobotics.robot.subsystems.ExampleArmSubsystem;
import com.swrobotics.robot.subsystems.ExampleShooterSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;


public class RobotContainer {

  public final DriveSubsystem drive;
  public final ExampleArmSubsystem exampleArm;
  public final ExampleShooterSubsystem exampleShooter;
  public final ControlBoard controlboard;

  public RobotContainer() {
    drive = new DriveSubsystem();
    exampleArm = new ExampleArmSubsystem();
    exampleShooter = new ExampleShooterSubsystem();
    controlboard = new ControlBoard(this);
  }

  public void disabledInit() {

  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
