package com.swrobotics.robot.control;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.config.IOAllocation;
import com.swrobotics.robot.subsystems.ExampleArmSubsystem;
import com.swrobotics.robot.subsystems.ExampleShooterSubsystem;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


public final class ControlBoard extends SubsystemBase {
    // =========================================================================
    // NetworkTables tunables
    // =========================================================================

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
        configureRumbles();
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

    operator.a().onTrue(robot.exampleArm.commandSetState(ExampleArmSubsystem.State.UP));
    operator.b().onTrue(robot.exampleArm.commandSetState(ExampleArmSubsystem.State.DOWN));

    operator.rightBumper().whileTrue(robot.exampleShooter.commandSetState(ExampleShooterSubsystem.State.SHOOT));
    operator.leftBumper().whileTrue(robot.exampleShooter.commandSetState(ExampleShooterSubsystem.State.REVERSE));


    }

    private void configureRumbles() {
        // Add controller rumble logic here when needed.
    }

    @Override
    public void periodic() {
    }
}