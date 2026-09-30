<div align="center">

<img src="https://img.shields.io/badge/SW%20ROBOTICS-2129-4B1F6F?style=for-the-badge&labelColor=000000" alt="SW Robotics 2129">

# SW Robotics Robot Code

### Command-Based Java Guide

<img src="https://img.shields.io/badge/Code%20Standard-Purple%20%2F%20Black-5E2D91?style=for-the-badge&labelColor=000000" alt="Purple and Black Code Standard">

</div>

---

> **Purpose:** This repository contains the command-based Java code for the SW Robotics competition robot. This document explains our code structure, subsystem format, NetworkTables tuning, SmartDashboard logging, commands, controller bindings, and autonomous routines.

---

# Project Structure

```text
src/main/java/com/swrobotics/robot/
│
├── Main.java
├── Robot.java
├── RobotContainer.java
│
├── config/
│   └── RobotConstants.java
│
├── control/
│   └── ControlBoard.java
│
├── commands/
│   ├── ExampleAutonomous.java
│   └── ExampleDriveCommand.java
│
└── subsystems/
    ├── ExampleArmSubsystem.java
    ├── ExampleShooterSubsystem.java
    │
    └── drive/
        └── DriveSubsystem.java
```

| Location | Responsibility |
|---|---|
| `Main.java` | Starts the robot program |
| `Robot.java` | Runs robot lifecycle methods and the command scheduler |
| `RobotContainer.java` | Creates subsystems, controls, default commands, button bindings, and autonomous routines |
| `config/` | Stores shared, robot-wide configuration if needed |
| `control/` | Maps driver and operator controller inputs to commands |
| `subsystems/` | Controls individual physical mechanisms |
| `commands/` | Contains reusable actions and autonomous sequences |

---

# Command-Based Overview

This project uses the WPILib command-based framework.

A **subsystem** represents a physical robot mechanism. A **command** represents an action the robot can perform with one or more mechanisms.

| Type | Description | Examples |
|---|---|---|
| Subsystem | Owns hardware and mechanism behavior | Drivebase, arm, shooter, intake |
| Command | Requests a robot action | Drive with joystick, move arm down, spin shooter |
| State | Describes the intended mode of a subsystem | `IDLE`, `SHOOT`, `UP`, `DOWN` |

The expected flow is:

```text
Controller Input
      ↓
ControlBoard Button Binding
      ↓
Command
      ↓
Subsystem State
      ↓
periodic()
      ↓
Motor/Sensor Control and SmartDashboard Logging
```

A subsystem owns its motors, sensors, configuration, states, NetworkTables entries, and SmartDashboard logs.

A command should request a subsystem behavior. It should not contain random motor-control code that belongs inside a subsystem.

---

# Subsystem Format

Every physical robot mechanism should have a subsystem.

Examples:

```text
DriveSubsystem
ExampleArmSubsystem
ExampleShooterSubsystem
IntakeSubsystem
IndexerSubsystem
ClimberSubsystem
```

A subsystem should usually follow this organization:

```java
public class ExampleSubsystem extends SubsystemBase {
    // 1. State enum

    // 2. Hard-coded hardware constants

    // 3. NetworkTables tunables

    // 4. Hardware objects and control requests

    // 5. Target state

    // 6. Constructor and hardware configuration

    // 7. periodic() motor control and SmartDashboard logs

    // 8. Public helper methods

    // 9. Command factory methods
}
```

## Basic subsystem template

```java
package com.swrobotics.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem extends SubsystemBase {
    public enum State {
        IDLE,
        ACTIVE
    }

    // Hardware constants
    private static final int kMotorID = 8;

    // Current requested mechanism behavior
    private State targetState = State.IDLE;

    public ExampleSubsystem() {
        // Create and configure hardware here.
    }

    @Override
    public void periodic() {
        switch (targetState) {
            case IDLE:
                // Stop or hold the mechanism.
                break;

            case ACTIVE:
                // Run the mechanism.
                break;
        }
    }

    public void setTargetState(State state) {
        targetState = state;
    }
}
```

## State rules

States should describe a real mechanism behavior.

Good state names:

```java
IDLE
INTAKE
OUTTAKE
FEED
HOLD
UP
DOWN
STOW
DEPLOY
SHOOT
REVERSE
CLIMB
```

Avoid state names based on code details or controller buttons.

Avoid:

```java
BUTTON_A
MOTOR_50_PERCENT
RUN_MOTOR
TEST_2
DO_THING
```

---

# Constants and Tunables

Every number in robot code should have a name and a reason.

Do not leave unexplained numbers directly in motor-control calls.

Bad:

```java
motor.setControl(voltageControl.withOutput(6.0));
```

Better:

```java
private final NTEntry<Double> intakeVoltage = new NTDouble("Intake/IntakeVoltage", 6.0).setPersistent();
```

```java
motor.setControl(voltageControl.withOutput(intakeVoltage.get()));
```

## Hard-coded constants

Use hard-coded values for physical hardware configuration and fixed design decisions.

```java
private static final int kMotorID = 5;
private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
private static final NeutralModeValue kNeutralMode = NeutralModeValue.Brake;
```

Use hard-coded constants for:

- CAN IDs
- DIO ports
- PWM ports
- Motor inversion
- Brake/coast mode
- Fixed physical configuration
- Static safety choices

Do not make CAN IDs NetworkTables values.

## NetworkTables tunables

Use NetworkTables entries for numbers that may change during robot testing, calibration, driver practice, or mechanism tuning.

```java
private final NTEntry<Double> kP = new NTDouble("ExampleArm/kP", 2.0).setPersistent();
private final NTEntry<Double> kI = new NTDouble("ExampleArm/kI", 0.0).setPersistent();
private final NTEntry<Double> kD = new NTDouble("ExampleArm/kD", 0.1).setPersistent();
```

Use NetworkTables values for:

- PID values
- Arm setpoints
- Shooter speed targets
- Drive speed scaling
- Joystick deadband
- Motion Magic velocity and acceleration
- Current limits that may need testing adjustments
- Vision aiming constants

## PID overview

| Term | Full Name | Purpose |
|---|---|---|
| `kP` | Proportional | Responds to the current error |
| `kI` | Integral | Corrects error that remains for a long time |
| `kD` | Derivative | Reduces overshoot and oscillation |

Safe tuning order:

1. Start with `kI = 0.0` and `kD = 0.0`.
2. Increase `kP` until the mechanism responds well.
3. Add a small `kD` only if the mechanism overshoots or oscillates.
4. Add a very small `kI` only if a consistent remaining error cannot be fixed another way.

---

# SmartDashboard Logging

Every subsystem should report useful information to SmartDashboard.

The goal of dashboard logging is to answer:

- What state is the subsystem in?
- What target did the code request?
- What does the sensor actually read?
- Is the mechanism at the target?
- Is the motor drawing too much current?
- Is the motor getting too hot?
- Is closed-loop control behaving correctly?

## Log naming format

Use this naming pattern:

```text
SubsystemName/ValueName
```

Examples:

```java
SmartDashboard.putString("ExampleArm/State", targetState.name());
SmartDashboard.putNumber("ExampleArm/TargetPositionRotations", targetPositionRotations);
SmartDashboard.putNumber("ExampleArm/ActualPositionRotations", armMotor.getPosition().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/MotorVoltage", armMotor.getMotorVoltage().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/SupplyCurrentAmps", armMotor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/TemperatureCelsius", armMotor.getDeviceTemp().getValueAsDouble());
```

## Recommended TalonFX logs

```java
SmartDashboard.putNumber("Subsystem/PositionRotations", motor.getPosition().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/VelocityRPS", motor.getVelocity().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/MotorVoltage", motor.getMotorVoltage().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/SupplyCurrentAmps", motor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/StatorCurrentAmps", motor.getStatorCurrent().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/TemperatureCelsius", motor.getDeviceTemp().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/ClosedLoopError", motor.getClosedLoopError().getValueAsDouble());
```

---

# Adding a New Subsystem

Follow this process whenever you add a new mechanism.

## 1. Create the file

For an intake:

```text
src/main/java/com/swrobotics/robot/subsystems/intake/IntakeSubsystem.java
```

```java
package com.swrobotics.robot.subsystems.intake;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSubsystem extends SubsystemBase {
}
```

## 2. Add states

```java
public enum State {
    IDLE,
    INTAKE,
    OUTTAKE
}
```

## 3. Add hardware constants

```java
private static final int kMotorID = 8;
private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
private static final NeutralModeValue kNeutralMode = NeutralModeValue.Coast;
```

## 4. Add tunable values

```java
private final NTEntry<Double> intakeVoltage = new NTDouble("Intake/IntakeVoltage", 6.0).setPersistent();
private final NTEntry<Double> outtakeVoltage = new NTDouble("Intake/OuttakeVoltage", -4.0).setPersistent();
```

## 5. Create motor hardware

```java
private final TalonFX motor = new TalonFX(kMotorID);
private final VoltageOut voltageControl = new VoltageOut(0.0);
```

## 6. Control the mechanism in `periodic()`

```java
@Override
public void periodic() {
    switch (targetState) {
        case IDLE:
            motor.stopMotor();
            break;

        case INTAKE:
            motor.setControl(voltageControl.withOutput(intakeVoltage.get()));
            break;

        case OUTTAKE:
            motor.setControl(voltageControl.withOutput(outtakeVoltage.get()));
            break;
    }
}
```

## 7. Add SmartDashboard logs

```java
SmartDashboard.putString("Intake/State", targetState.name());
SmartDashboard.putNumber("Intake/VelocityRPS", motor.getVelocity().getValueAsDouble());
SmartDashboard.putNumber("Intake/SupplyCurrentAmps", motor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("Intake/TemperatureCelsius", motor.getDeviceTemp().getValueAsDouble());
```

## 8. Add state methods and a command

```java
public void setTargetState(State state) {
    targetState = state;
}

public Command commandSetState(State state) {
    return Commands.run(() -> setTargetState(state), this).withName("Intake " + state.name());
}
```

## 9. Create it in `RobotContainer`

```java
public final IntakeSubsystem intake = new IntakeSubsystem();
```

## 10. Bind a controller input

```java
operator.rightTrigger().whileTrue(robot.intake.commandSetState(IntakeSubsystem.State.INTAKE));
```

---

# Command Format

Commands represent robot actions.

Use a command when you need to:

- Change a subsystem state
- Coordinate multiple subsystems
- Run a mechanism for a limited time
- Wait for a sensor
- Write an autonomous routine
- Build a reusable robot action

## Command patterns

| Pattern | Use when | Example |
|---|---|---|
| `Commands.run(...)` | The action continues until interrupted | Keep shooter spinning while held |
| `Commands.runOnce(...)` | The action occurs once and then finishes | Set arm state to `DOWN` |
| `.withTimeout(...)` | Stop after a fixed amount of time | Drive forward for 2 seconds |
| `.until(...)` | Stop when a condition becomes true | Intake until a sensor detects a note |
| `Commands.sequence(...)` | Run actions in order | Drive, shoot, then stop |
| `Commands.parallel(...)` | Run actions at the same time | Drive while raising an arm |

## Simple state command

Subsystem commands should usually be short:

```java
public Command commandSetState(State state) {
    return Commands.run(() -> setTargetState(state), this).withName("Shooter " + state.name());
}
```

---

# Example Drive Command

Create:

```text
src/main/java/com/swrobotics/robot/commands/ExampleDriveCommand.java
```

```java
package com.swrobotics.robot.commands;

import java.util.function.DoubleSupplier;

import com.swrobotics.robot.subsystems.drive.DriveSubsystem;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;

public class ExampleDriveCommand extends Command {
    private static final double kDeadband = 0.10;

    private final DriveSubsystem drive;
    private final DoubleSupplier forwardSupplier;
    private final DoubleSupplier turnSupplier;

    public ExampleDriveCommand(DriveSubsystem drive, DoubleSupplier forwardSupplier, DoubleSupplier turnSupplier) {
        this.drive = drive;
        this.forwardSupplier = forwardSupplier;
        this.turnSupplier = turnSupplier;

        addRequirements(drive);
    }

    @Override
    public void execute() {
        double forward = MathUtil.applyDeadband(forwardSupplier.getAsDouble(), kDeadband);
        double turn = MathUtil.applyDeadband(turnSupplier.getAsDouble(), kDeadband);

        drive.arcadeDrive(forward, turn);
    }

    @Override
    public void end(boolean interrupted) {
        drive.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
```

Your `DriveSubsystem` needs these public methods:

```java
public void arcadeDrive(double forward, double turn) {
    double leftOutput = forward + turn;
    double rightOutput = forward - turn;

    leftMotor1.set(ControlMode.PercentOutput, leftOutput);
    leftMotor2.set(ControlMode.PercentOutput, leftOutput);
    rightMotor1.set(ControlMode.PercentOutput, rightOutput);
    rightMotor2.set(ControlMode.PercentOutput, rightOutput);
}

public void stop() {
    leftMotor1.set(ControlMode.PercentOutput, 0.0);
    leftMotor2.set(ControlMode.PercentOutput, 0.0);
    rightMotor1.set(ControlMode.PercentOutput, 0.0);
    rightMotor2.set(ControlMode.PercentOutput, 0.0);
}
```

Set it as the default drive command:

```java
drive.setDefaultCommand(new ExampleDriveCommand(drive, () -> controls.driver.getLeftY(), () -> controls.driver.getRightX()));
```

This command runs continuously during teleoperated driving. It reads joystick values, applies deadband, and sends arcade-drive outputs to the drivetrain.

---

# Example Autonomous Command

Create:

```text
src/main/java/com/swrobotics/robot/commands/ExampleAutonomous.java
```

```java
package com.swrobotics.robot.commands;

import com.swrobotics.robot.subsystems.ExampleShooterSubsystem;
import com.swrobotics.robot.subsystems.drive.DriveSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public final class ExampleAutonomous {
    private ExampleAutonomous() {
    }

    public static Command create(DriveSubsystem drive, ExampleShooterSubsystem shooter) {
        return Commands.sequence(
                shooter.commandSetState(ExampleShooterSubsystem.State.IDLE).withTimeout(0.02),
                drive.driveForTime(0.40, 0.0, 2.0),
                drive.stopCommand(),
                shooter.commandSetState(ExampleShooterSubsystem.State.SHOOT).withTimeout(1.5),
                shooter.commandSetState(ExampleShooterSubsystem.State.IDLE).withTimeout(0.02)
        ).withName("Example Autonomous");
    }
}
```

The routine does this:

| Step | Action |
|---|---|
| 1 | Make sure the shooter is idle |
| 2 | Drive forward at 40% output for 2 seconds |
| 3 | Stop the drivetrain |
| 4 | Spin the shooter for 1.5 seconds |
| 5 | Return the shooter to idle |

Add these methods to `DriveSubsystem`:

```java
public Command driveForTime(double forwardOutput, double turnOutput, double seconds) {
    return run(() -> arcadeDrive(forwardOutput, turnOutput)).withTimeout(seconds).finallyDo(interrupted -> stop()).withName("Drive For Time");
}

public Command stopCommand() {
    return runOnce(this::stop).withName("Drive Stop");
}
```

Add this to `RobotContainer.java`:

```java
import com.swrobotics.robot.commands.ExampleAutonomous;

public Command getAutonomousCommand() {
    return ExampleAutonomous.create(drive, exampleShooter);
}
```

---

# Controller Bindings

Controller bindings belong in:

```text
control/ControlBoard.java
```

Example:

```java
private void configureControls() {
    robot.drive.setDefaultCommand(new ExampleDriveCommand(robot.drive, () -> driver.getLeftY(), () -> driver.getRightX()));

    operator.a().onTrue(robot.exampleArm.commandSetState(ExampleArmSubsystem.State.UP));
    operator.b().onTrue(robot.exampleArm.commandSetState(ExampleArmSubsystem.State.DOWN));

    operator.rightBumper().whileTrue(robot.exampleShooter.commandSetState(ExampleShooterSubsystem.State.SHOOT));
    operator.leftBumper().whileTrue(robot.exampleShooter.commandSetState(ExampleShooterSubsystem.State.REVERSE));
}
```

## Button binding choices

| Binding | Use case |
|---|---|
| `onTrue()` | Change a state once when pressed |
| `whileTrue()` | Run a command only while a button is held |
| `onFalse()` | Run a command when a button is released |
| `toggleOnTrue()` | Toggle a command on and off |

Use `onTrue()` for arm positions because the arm should stay at the requested position.

Use `whileTrue()` for a shooter when the shooter should stop immediately after the driver releases the button.

---

# Code Style Rules

## Naming

```java
private static final int kMotorID = 5;
private final TalonFX shooterMotor = new TalonFX(kMotorID);
private final NTEntry<Double> shootVelocityRps = new NTDouble("Shooter/ShootVelocityRPS", 60.0).setPersistent();
```

| Item | Style | Example |
|---|---|---|
| Class | PascalCase | `ExampleShooterSubsystem` |
| Method | camelCase | `setTargetState()` |
| Variable | camelCase | `targetVelocityRps` |
| Hard constant | `k` + PascalCase | `kMotorID` |
| NetworkTables entry | camelCase | `shootVelocityRps` |
| Enum value | UPPER_SNAKE_CASE | `SLOW_SHOOT` |
| Dashboard key | `Subsystem/ValueName` | `ExampleShooter/TopVelocityRPS` |

## Keep responsibilities separate

| Location | Should contain |
|---|---|
| Subsystem | Motors, sensors, subsystem states, motor configuration, periodic control, dashboard logs |
| Command | A requested action, command sequence, timing, multi-subsystem coordination |
| ControlBoard | Controller mappings and button bindings |
| RobotContainer | Subsystem construction, control construction, autonomous selection |
| Robot | Robot lifecycle methods and command scheduler |

Do not put button logic inside a subsystem.

Do not put direct motor-control code inside `ControlBoard`.

Do not put entire autonomous routines inside `Robot.java`.

---

# Testing Checklist

Before testing a new subsystem:

- Verify the CAN ID.
- Verify motor inversion.
- Keep mechanisms safely elevated or disconnected when possible.
- Start with low voltage, low percent output, or low target speed.
- Confirm the desired state appears in SmartDashboard.
- Confirm sensor position and velocity update correctly.
- Confirm current draw is reasonable.
- Watch motor temperature.
- Confirm `IDLE` stops motors that should not remain powered.
- Tune `kP` before adjusting `kI` and `kD`.
- Test autonomous routines in a clear area.

---

# Common Problems

| Problem | Likely Cause | First Check |
|---|---|---|
| Motor does not move | Wrong CAN ID, disabled robot, command not scheduled | CAN ID, robot enabled state, subsystem dashboard state |
| Motor moves backward | Incorrect inversion | `InvertedValue` in subsystem configuration |
| Arm targets wrong position | Encoder units or setpoints are incorrect | Actual position dashboard value and target position |
| Shooter does not reach target speed | Incorrect PID, friction, battery voltage, inversion | Actual velocity, target velocity, current draw |
| Shooter remains on | Missing or broken `IDLE` behavior | Shooter default command and `IDLE` state |
| Two commands fight each other | Commands have incorrect subsystem requirements | Command construction and `addRequirements()` |
| NetworkTables update has no effect | Motor configuration was applied only in constructor | Add an intentional config-apply command |
| Joystick drifts | Deadband too small | Driver deadband NetworkTables entry |

---

<div align="center">

## SW Robotics Code Standard

**Build clearly. Log useful data. Tune carefully. Test safely.**

</div>
