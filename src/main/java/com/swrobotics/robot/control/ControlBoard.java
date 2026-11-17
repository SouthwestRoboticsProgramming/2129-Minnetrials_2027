package com.swrobotics.robot.control;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.config.IOAllocation;
import com.swrobotics.robot.subsystems.scoringArm.ScoringArmSubsystem;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


public final class ControlBoard extends SubsystemBase {

    /*  
    *
    * Controller Layout:
    *
    * Driver:
    * 
    * Right stick - turn
    * left stick - forward/backward
    * 
    * Operator:
    * 
    * A - Arm Score Cookie
    * B - Arm Score Milk
    * X - Arm Stow
    * Y - Arm Origin
    *
    */

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

        robot.drive.setDefaultCommand(
                robot.drive.arcadeDrive(
                        () -> MathUtil.applyDeadband(
                                driver.getLeftY(),
                                driverDeadband.get()),
                        () -> MathUtil.applyDeadband(
                                driver.getRightX(),
                                driverDeadband.get())));

        driver.b().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.SCORE_COOKIE));
        driver.x().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.SCORE_MILK));
        driver.a().onTrue(robot.scoringArm.commandSetState(ScoringArmSubsystem.State.STOWING));



    }


    @Override
    public void periodic() {
    }
}