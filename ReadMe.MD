<div align="center">

# <span style="color:#b46cff;">SW Robotics Robot Code</span>

### <span style="color:#d2a6ff;">Command-Based Java Guide</span>

---

</div>

> This guide explains the format used by this robot project: how files are organized, how subsystems work, how to add a mechanism, how to add commands, and how to write basic autonomous routines.

---

## <span style="color:#b46cff;">Project Structure</span>

```text
src/main/java/com/swrobotics/robot/
│
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

| File or folder | Purpose |
|---|---|
| `Robot.java` | Runs the WPILib robot lifecycle: disabled, autonomous, teleop, and test |
| `RobotContainer.java` | Creates subsystems, creates controls, binds buttons, and returns the autonomous command |
| `config/` | Shared robot-wide setup such as controller ports, CAN IDs, field dimensions, or team-wide constants |
| `control/` | Driver/operator controllers and button bindings |
| `subsystems/` | Code for physical robot mechanisms: drivebase, arm, shooter, intake, climber, etc. |
| `commands/` | Reusable actions that coordinate one or more subsystems |
| `SmartDashboard` | Live telemetry values visible while the robot is connected |
| `NetworkTables` entries | Persistent values that can be adjusted while testing without redeploying |

---

## <span style="color:#b46cff;">How Command-Based Works</span>

The robot code uses WPILib’s **command-based** system.

There are two main concepts:

| Concept | Meaning | Example |
|---|---|---|
| Subsystem | Represents a physical robot mechanism | Drivebase, arm, shooter, intake |
| Command | Represents an action using one or more subsystems | Drive with joystick, move arm down, spin shooter |

A subsystem owns its motors, sensors, configuration, states, and SmartDashboard logs.

A command tells a subsystem what it should do. The WPILib scheduler ensures that only one command at a time can control a subsystem.

For example, if the shooter is already running a `SHOOT` command, a `REVERSE` command can interrupt it because both commands require the shooter subsystem. This avoids two pieces of code fighting over the same motors. [51][53]

### Basic control flow

```text
Driver presses a button
        ↓
ControlBoard schedules a command
        ↓
Command changes a subsystem state
        ↓
Subsystem periodic() reads that state
        ↓
Subsystem commands motors and logs dashboard data
```

---

## <span style="color:#b46cff;">Subsystem Format</span>

Every mechanism should be represented by one subsystem class.

A beginner-friendly subsystem usually contains these sections in this order:

```java
public class ExampleSubsystem extends SubsystemBase {
    // 1. State enum

    // 2. Hard-coded hardware constants

    // 3. NetworkTables tuning entries

    // 4. Motors, sensors, and CTRE control requests

    // 5. Current target state

    // 6. Constructor: create hardware and apply configs

    // 7. periodic(): use state to control hardware and log values

    // 8. Public methods: set state, read sensors, check conditions

    // 9. Command factory methods
}
```

### Example subsystem skeleton

```java
package com.swrobotics.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem extends SubsystemBase {
    public enum State {
        IDLE,
        ACTIVE
    }

    // Hard-coded physical hardware mapping.
    private static final int kMotorID = 8;

    // The physical motor owned by this subsystem.
    private final TalonFX motor = new TalonFX(kMotorID);

    // The state that periodic() uses to decide what the motor should do.
    private State targetState = State.IDLE;

    public ExampleSubsystem() {
        // Configure motors and sensors here.
    }

    @Override
    public void periodic() {
        switch (targetState) {
            case IDLE:
                motor.stopMotor();
                break;

            case ACTIVE:
                // Send the desired motor control here.
                break;
        }
    }

    public void setTargetState(State state) {
        targetState = state;
    }
}
```

### Why use states?

States make mechanism code easier to read and safer to change.

Instead of placing motor-control code in every button binding:

```java
operator.rightBumper().whileTrue(
        Commands.run(() -> motor.set(0.75)));
```

put the motor behavior inside the subsystem:

```java
public enum State {
    IDLE,
    SHOOT
}
```

Then the button binding only selects a state:

```java
operator.rightBumper().whileTrue(
        robot.shooter.commandSetState(
                ExampleShooterSubsystem.State.SHOOT));
```

This keeps all shooter behavior in the shooter file. A programmer can open one file and see its motors, states, setpoints, tuning values, logs, and safety behavior.

---

## <span style="color:#b46cff;">Constants and Tunables</span>

Every number should have a clear purpose. Do not leave unexplained “magic numbers” in motor commands.

### Hard-coded constants

Use `private static final` for numbers or settings that are part of the robot’s physical design.

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
- Brake or coast mode
- Gearbox/mechanism structural settings
- Fixed safety choices

Do not make CAN IDs NetworkTables entries. A live-editable CAN ID can accidentally cause code to control the wrong motor.

### NetworkTables tunables

Use `NTEntry<Double>` when a value may need to change during testing, calibration, or driver practice.

```java
private final NTEntry<Double> kP = new NTDouble("ExampleArm/kP", 2.0).setPersistent();
private final NTEntry<Double> kI = new NTDouble("ExampleArm/kI", 0.0).setPersistent();
private final NTEntry<Double> kD = new NTDouble("ExampleArm/kD", 0.1).setPersistent();
```

Use NetworkTables entries for:

- PID values
- Motion Magic cruise velocity
- Motion Magic acceleration
- Arm positions
- Shooter speed setpoints
- Driver deadband
- Drive speed scaling
- Vision aiming gain
- Tunable current limits

### PID terms

| Term | Name | What it does |
|---|---|---|
| `kP` | Proportional | Responds to the error right now |
| `kI` | Integral | Corrects a small error that stays for a long time |
| `kD` | Derivative | Helps reduce overshoot and oscillation |

A safe beginner starting point:

```java
kP = a small nonzero value
kI = 0.0
kD = 0.0
```

Tune `kP` first. Only add `kI` or `kD` after confirming that `kP` alone cannot solve the problem.

### Example: reading a NetworkTables value

```java
double targetSpeed = shootVelocityRps.get();
```

The `.get()` call retrieves the current dashboard value. Because the entry is persistent, its most recent value can remain saved between robot restarts.

---

## <span style="color:#b46cff;">SmartDashboard Logging</span>

Every subsystem should log useful information in `periodic()`.

Useful logs answer these questions:

- What state does the robot think it is in?
- What target did we request?
- What is the actual sensor value?
- Is the mechanism at its target?
- Is the motor drawing too much current?
- Is the motor overheating?
- Is closed-loop control behaving correctly?

### Recommended logging format

Use a consistent naming pattern:

```java
SmartDashboard.putString("ExampleArm/State", targetState.name());
SmartDashboard.putNumber("ExampleArm/TargetPositionRotations", targetPositionRotations);
SmartDashboard.putNumber("ExampleArm/ActualPositionRotations", armMotor.getPosition().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/MotorVoltage", armMotor.getMotorVoltage().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/SupplyCurrentAmps", armMotor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("ExampleArm/TemperatureCelsius", armMotor.getDeviceTemp().getValueAsDouble());
```

### Suggested motor logs

```java
SmartDashboard.putNumber("Subsystem/PositionRotations", motor.getPosition().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/VelocityRPS", motor.getVelocity().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/MotorVoltage", motor.getMotorVoltage().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/SupplyCurrentAmps", motor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/StatorCurrentAmps", motor.getStatorCurrent().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/TemperatureCelsius", motor.getDeviceTemp().getValueAsDouble());
SmartDashboard.putNumber("Subsystem/ClosedLoopError", motor.getClosedLoopError().getValueAsDouble());
```

Use one predictable path per mechanism:

```text
Drivebase/
ExampleArm/
ExampleShooter/
Intake/
Indexer/
Climber/
Vision/
```

---

## <span style="color:#b46cff;">Adding a New Subsystem</span>

Use this checklist whenever you add a mechanism.

### 1. Create a package and class

For a mechanism named intake:

```text
subsystems/
└── intake/
    └── IntakeSubsystem.java
```

```java
package com.swrobotics.robot.subsystems.intake;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSubsystem extends SubsystemBase {
}
```

### 2. Decide the states

States should describe what the mechanism is intended to do.

```java
public enum State {
    IDLE,
    INTAKE,
    OUTTAKE
}
```

Avoid states that describe button presses or code implementation details.

Good state names:

```java
IDLE
INTAKE
OUTTAKE
HOLD
FEED
STOW
DEPLOY
CLIMB
```

Poor state names:

```java
BUTTON_A
MOTOR_50_PERCENT
RUN_THE_THING
TEST_2
```

### 3. Add hardware constants

```java
private static final int kMotorID = 8;
private static final InvertedValue kMotorInverted = InvertedValue.Clockwise_Positive;
private static final NeutralModeValue kNeutralMode = NeutralModeValue.Coast;
```

### 4. Add NetworkTables values

```java
private final NTEntry<Double> intakeVoltage = new NTDouble("Intake/IntakeVoltage", 6.0).setPersistent();
private final NTEntry<Double> outtakeVoltage = new NTDouble("Intake/OuttakeVoltage", -4.0).setPersistent();
```

### 5. Create/configure hardware

```java
private final TalonFX motor = new TalonFX(kMotorID);
private final VoltageOut voltageControl = new VoltageOut(0.0);
```

Then configure the motor in the constructor.

### 6. Control it in `periodic()`

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

### 7. Add basic logs

```java
SmartDashboard.putString("Intake/State", targetState.name());
SmartDashboard.putNumber("Intake/VelocityRPS", motor.getVelocity().getValueAsDouble());
SmartDashboard.putNumber("Intake/SupplyCurrentAmps", motor.getSupplyCurrent().getValueAsDouble());
SmartDashboard.putNumber("Intake/TemperatureCelsius", motor.getDeviceTemp().getValueAsDouble());
```

### 8. Add a state command

```java
public Command commandSetState(State state) {
    return Commands.run(() -> setTargetState(state), this).withName("Intake " + state.name());
}
```

### 9. Add it to `RobotContainer`

```java
public final IntakeSubsystem intake = new IntakeSubsystem();
```

### 10. Bind a button

```java
operator.rightTrigger().whileTrue(
        robot.intake.commandSetState(IntakeSubsystem.State.INTAKE));
```

When the trigger is released, the command ends. If the intake has a default `IDLE` command, it returns to zero output.

---

## <span style="color:#b46cff;">Command Format</span>

Use commands when you need an action that:

- Changes a subsystem’s state
- Coordinates multiple subsystems
- Runs for a limited time
- Waits for a sensor condition
- Runs during autonomous
- Represents a reusable robot behavior

For simple state changes, use a subsystem command factory:

```java
public Command commandSetState(State state) {
    return Commands.run(() -> setTargetState(state), this).withName("Shooter " + state.name());
}
```

For more involved actions, create a separate command class in `commands/`.

### Common command types

| Pattern | Use it when | Example |
|---|---|---|
| `Commands.run(...)` | The action should continue until interrupted | Keep shooter spinning while a button is held |
| `Commands.runOnce(...)` | Change something once, then finish | Set arm target to `DOWN` |
| `Commands.waitSeconds(...)` | Wait for a fixed time | Delay before starting auto |
| `.until(...)` | End when a condition becomes true | Intake until a sensor sees a game piece |
| `Commands.sequence(...)` | Run steps in order | Spin shooter, wait for speed, feed |
| `Commands.parallel(...)` | Run actions at the same time | Drive while raising arm |

---

## <span style="color:#b46cff;">Example Drive Command</span>

This command drives the robot from two joystick suppliers.

Create this file:

```text
commands/
└── ExampleDriveCommand.java
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

    public ExampleDriveCommand(
            DriveSubsystem drive,
            DoubleSupplier forwardSupplier,
            DoubleSupplier turnSupplier) {
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

For this command to work, your `DriveSubsystem` needs these public methods:

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

Set it as the drivebase default command in `RobotContainer`:

```java
drive.setDefaultCommand(
        new ExampleDriveCommand(
                drive,
                () -> controls.driver.getLeftY(),
                () -> controls.driver.getRightX()));
```

### What this command does

| Method | Meaning |
|---|---|
| Constructor | Saves references to the drivebase and controller suppliers |
| `addRequirements(drive)` | Prevents another command from controlling the drivebase at the same time |
| `execute()` | Runs about every 20 ms while teleop is active |
| `end()` | Stops motors when command is interrupted |
| `isFinished()` | Returns `false`, so joystick drive continues until another drive command interrupts it |

---

## <span style="color:#b46cff;">Example Autonomous Command</span>

This autonomous routine uses command composition rather than a long custom class. It demonstrates a basic sequence:

1. Make sure shooter is idle.
2. Drive forward for 2 seconds.
3. Stop the drivetrain.
4. Spin the shooter for 1.5 seconds.
5. Return shooter to idle.

Create this file:

```text
commands/
└── ExampleAutonomous.java
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

    public static Command create(
            DriveSubsystem drive,
            ExampleShooterSubsystem shooter) {
        return Commands.sequence(
                shooter.commandSetState(
                        ExampleShooterSubsystem.State.IDLE).withTimeout(0.02),

                drive.driveForTime(0.40, 0.0, 2.0),

                drive.stopCommand(),

                shooter.commandSetState(
                        ExampleShooterSubsystem.State.SHOOT).withTimeout(1.5),

                shooter.commandSetState(
                        ExampleShooterSubsystem.State.IDLE).withTimeout(0.02)
        ).withName("Example Autonomous");
    }
}
```

For the autonomous command above to work, add these two methods to `DriveSubsystem`:

```java
public Command driveForTime(
        double forwardOutput,
        double turnOutput,
        double seconds) {
    return run(() -> arcadeDrive(forwardOutput, turnOutput))
            .withTimeout(seconds)
            .finallyDo(interrupted -> stop())
            .withName("Drive For Time");
}

public Command stopCommand() {
    return runOnce(this::stop).withName("Drive Stop");
}
```

Then add this to `RobotContainer.java`:

```java
import com.swrobotics.robot.commands.ExampleAutonomous;

public Command getAutonomousCommand() {
    return ExampleAutonomous.create(drive, exampleShooter);
}
```

WPILib schedules the command returned by `getAutonomousCommand()` when autonomous begins. Autonomous routines can be made from command sequences, parallel groups, timeouts, and sensor-based end conditions. [47][49][51]

---

## <span style="color:#b46cff;">Button Binding Examples</span>

Put button bindings in `ControlBoard.configureControls()`.

```java
private void configureControls() {
    robot.drive.setDefaultCommand(
            new ExampleDriveCommand(
                    robot.drive,
                    () -> driver.getLeftY(),
                    () -> driver.getRightX()));

    operator.a().onTrue(
            robot.exampleArm.commandSetState(
                    ExampleArmSubsystem.State.UP));

    operator.b().onTrue(
            robot.exampleArm.commandSetState(
                    ExampleArmSubsystem.State.DOWN));

    operator.rightBumper().whileTrue(
            robot.exampleShooter.commandSetState(
                    ExampleShooterSubsystem.State.SHOOT));

    operator.leftBumper().whileTrue(
            robot.exampleShooter.commandSetState(
                    ExampleShooterSubsystem.State.REVERSE));
}
```

### `onTrue()` versus `whileTrue()`

| Binding | Use when | Example |
|---|---|---|
| `onTrue()` | A button press should select a persistent state | Arm moves down and stays down |
| `whileTrue()` | The action should only occur while held | Shooter spins only while bumper is held |
| `onFalse()` | Something should happen when a button is released | Stop a mechanism after releasing trigger |
| `toggleOnTrue()` | A press toggles a command on/off | Testing a compressor or a diagnostic command |

Example:

```java
operator.b().onTrue(
        robot.exampleArm.commandSetState(
                ExampleArmSubsystem.State.DOWN));
```

The arm retains `DOWN` because its subsystem stores `targetState`.

Example:

```java
operator.rightBumper().whileTrue(
        robot.exampleShooter.commandSetState(
                ExampleShooterSubsystem.State.SHOOT));
```

When released, the shooter command ends. The shooter’s default `IDLE` command resumes and stops the motors.

---

## <span style="color:#b46cff;">Code Style Rules</span>

Use these rules consistently across robot code.

### Naming

```java
private static final int kMotorID = 5;
private final TalonFX shooterMotor = new TalonFX(kMotorID);
private final NTEntry<Double> shootVelocityRps = new NTDouble("Shooter/ShootVelocityRPS", 60.0).setPersistent();
```

| Item | Style | Example |
|---|---|---|
| Class | PascalCase | `ExampleShooterSubsystem` |
| Method | camelCase | `setTargetState()` |
| Local variable | camelCase | `targetVelocityRps` |
| Hard constant | `k` + PascalCase | `kMotorID` |
| NetworkTables entry | camelCase | `shootVelocityRps` |
| Enum value | UPPER_SNAKE_CASE | `SLOW_SHOOT` |
| Dashboard path | `Subsystem/ValueName` | `ExampleShooter/TopVelocityRPS` |

### Keep subsystems focused

Good:

```text
ExampleShooterSubsystem controls shooter motors.
ExampleArmSubsystem controls arm motors.
DriveSubsystem controls drive motors.
ControlBoard binds controllers to commands.
RobotContainer creates and connects everything.
```

Avoid putting shooter motor commands inside `ControlBoard`. Avoid putting controller-button logic inside a subsystem.

### Keep commands readable

Good:

```java
Commands.sequence(
        drive.driveForTime(0.40, 0.0, 2.0),
        shooter.commandSetState(State.SHOOT).withTimeout(1.5),
        shooter.commandSetState(State.IDLE).withTimeout(0.02));
```

Avoid hiding a full autonomous routine inside a controller binding or inside `Robot.java`.

---

## <span style="color:#b46cff;">Testing Checklist</span>

Before testing a new subsystem:

- Verify the CAN ID is correct.
- Verify motor inversion with wheels/mechanism safely off the ground.
- Start with low voltage or low speed.
- Confirm SmartDashboard values update correctly.
- Confirm the encoder position moves in the expected direction.
- Confirm motor current is reasonable.
- Confirm the motor temperature stays safe.
- Confirm `IDLE` or disabled mode commands zero output when appropriate.
- Tune `kP` first.
- Leave `kI` and `kD` at zero until they are actually needed.
- Test any autonomous routine with the robot elevated or in a safe open area first.

---

## <span style="color:#b46cff;">Common Problems</span>

| Problem | Likely cause | First thing to check |
|---|---|---|
| Motor does not move | Wrong CAN ID, disabled robot, command not scheduled | CAN ID, Driver Station enable state, SmartDashboard state |
| Motor spins backward | Incorrect inversion | `InvertedValue` in subsystem configuration |
| Arm moves to wrong location | Incorrect position units or bad setpoint | Encoder rotations, gear ratio, NetworkTables target |
| Shooter never reaches speed | Wrong PID, too-low voltage, incorrect inversion, mechanical friction | Actual RPS, target RPS, current draw |
| Shooter stays on after button release | Missing `IDLE` default command | Shooter constructor and command requirements |
| Two mechanisms behave strangely | A command controls the wrong subsystem | Check `addRequirements()` or command factory subsystem requirement |
| Tunable changes do nothing | Motor config only applied at startup | Add and run a deliberate “apply tunables” configuration command |
| Robot drives when stick is centered | Deadband too low or joystick drift | Increase joystick deadband |

---

<div align="center">

---

### <span style="color:#b46cff;">Build simple. Log everything. Test safely.</span>

</div>