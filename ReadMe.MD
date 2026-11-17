<div align="center">

# SW Robotics Minnetrials Code

### Command-Based Java Guide For New Code Team Members

</div>

---

> **Purpose:** This repository contains the command-based Java code for the SW Robotics Minnetrials robot. This document explains our code structure, subsystem format, NetworkTables tuning, SmartDashboard logging, commands, controller bindings, autonomous routines, IO Allocation, and how to use them to make your own robot.

---

## Project Structure

```text
src/main/java/com/swrobotics
│
├── lib/
│   ├── ctre/...
│   └── net/...
├── robot/
│   │
│   ├── commands/
│   │   ├── AutonomousCommands.java
│   │   └── DriveCommands.java
│   │
│   ├── config/
│   │   ├── FieldConstants.java
│   │   ├── IOAllocation.java
│   │   └── RobotConstants.java
│   │
│   ├── control/
│   │   └── ControlBoard.java
│   │
│   ├── logging/
│   │   └── FieldView.java
│   │
│   ├── subsystems/
│   │   ├── scoringArm/
│   │   |   └── ScoringArmSubsystem.java
│   │   └── drive/
│   │       └── DriveSubsystem.java
│   ├── Main.java
│   ├── Robot.java
│   └── RobotContainer.java
```

| Location | Responsibility |
| :--- | :--- |
| `lib/` | Contains information that allows network tables to run. Rarely needs editing. |
| `commands/` | Contains reusable actions and autonomous sequences (e.g., auto-align). **Edit this.** |
| `config/` | Contains global constants for field positions, **IO Allocation (CAN/Ports)**, and Robot constants. **Edit this.** |
| `control/` | Maps controls, default commands, and button bindings. **Edit this.** |
| `logging/` | Used to visualize where your robot is on the field in simulation and real matches. |
| `subsystems/` | Controls individual physical mechanisms. **Create new ones here.** |
| `Main.java` | Starts the robot program. Rarely needs editing. |
| `Robot.java` | Runs robot lifecycle methods and the command scheduler. Rarely needs editing. |
| `RobotContainer.java` | Creates subsystems and autonomous routines. **You will use this often.** |

---

## Command-Based Overview

This project uses the WPILib command-based framework.

A **subsystem** represents a physical robot mechanism. A **command** represents an action the robot can perform with one or more mechanisms.

| Type | Description | Examples |
| :--- | :--- | :--- |
| **Subsystem** | Owns hardware and mechanism behavior | Drivebase, arm, shooter, intake |
| **Command** | Requests a robot action | Drive with joystick, move arm down, spin shooter |
| **State** | Describes the intended mode of a subsystem | `IDLE`, `SHOOT`, `UP`, `DOWN` |

**The expected flow:**
1. A subsystem owns its motors, sensors, configuration, states, NetworkTables entries, and SmartDashboard logs.
2. A command requests a subsystem behavior. It should not contain random motor-control code that belongs inside a subsystem.

---

## IO Allocation (Hardware Mapping)

To prevent conflicts where two motors accidentally share the same CAN ID or sensor port, **ALL hardware IDs must be defined in `config/IOAllocation.java`**. Do not hard-code CAN IDs directly inside your subsystem classes.

### Example `IOAllocation.java` structure:

```java
public final class IOAllocation {
    public static final class CAN {
        public static final int DRIVE_LEFT_LEADER = 1;
        public static final int DRIVE_LEFT_FOLLOWER = 2;
        public static final int DRIVE_RIGHT_LEADER = 3;
        public static final int DRIVE_RIGHT_FOLLOWER = 4;
        public static final int INTAKE_MOTOR = 5;
    }
    
    public static final class DIO {
        public static final int INTAKE_BEAM_BREAK = 0;
    }
}
```

---

## Subsystem Format

Every physical robot mechanism should have a subsystem. Examples include the Drivetrain, Intake, Shooter, or Climber. A subsystem should usually follow this organization:

### Basic Subsystem Template

```java
public class ExampleSubsystem extends SubsystemBase {
    
    public enum State {
        IDLE,
        ACTIVE
    }

    private State currentState = State.IDLE;
    private final TalonFX motor;

    public ExampleSubsystem() {
        motor = new TalonFX(IOAllocation.CAN.EXAMPLE_MOTOR);
        // Configuration logic here
    }

    public void setState(State newState) {
        this.currentState = newState;
    }

    @Override
    public void periodic() {
        switch (currentState) {
            case ACTIVE:
                motor.set(1.0);
                break;
            case IDLE:
            default:
                motor.set(0.0);
                break;
        }
        
        SmartDashboard.putString("ExampleSubsystem/State", currentState.name());
    }
}
```

### State Rules

States should describe a real mechanism behavior. 

*   **Good state names:** `IDLE`, `INTAKING`, `OUTTAKING`, `STOWED`, `DEPLOYED`, `SPINNING_UP`
*   **Avoid state names based on code details or controller buttons:** `BUTTON_A_PRESSED`, `SET_MOTOR_TO_HALF`, `RUNNING_WHILE_TRUE`

---

## Constants and Tunables

Every number in robot code should have a name and a reason. Don't leave unexplained numbers directly in motor-control calls.

**Bad:**
```java
motor.set(0.75);
```

**Better:**
```java
motor.set(kIntakeSpeed);
```

### Hard-Coded Configuration Constants
Use hard-coded values for fixed design decisions that are not related to IO mapping:
*   Motor inversion
*   Brake/coast mode
*   Fixed physical configuration (gear ratios)
*   Static safety choices

### NetworkTables Tunables
Use NetworkTables entries for numbers that may change during robot testing, calibration, driver practice, or mechanism tuning:
*   PID values
*   Arm setpoints
*   Shooter speed targets
*   Drive speed scaling
*   Joystick deadband
*   Motion Magic velocity and acceleration
*   Current limits that may need testing adjustments
*   Vision aiming constants

### PID Overview

| Term | Full Name | Purpose |
| :--- | :--- | :--- |
| `kP` | Proportional | Responds to the current error |
| `kI` | Integral | Corrects error that remains for a long time |
| `kD` | Derivative | Reduces overshoot and oscillation |

**Safe tuning order:**
1. Start with `kI = 0.0` and `kD = 0.0`.
2. Increase `kP` until the mechanism responds well.
3. Add a small `kD` only if the mechanism overshoots or oscillates.
4. Add a very small `kI` only if a consistent remaining error cannot be fixed another way.

---

## SmartDashboard Logging

Every subsystem should report useful information to SmartDashboard to aid in debugging. 

**The goal of dashboard logging is to answer:**
*   What state is the subsystem in?
*   What target did the code request?
*   What does the sensor actually read?
*   Is the mechanism at the target?
*   Is the motor drawing too much current?
*   Is the motor getting too hot?

### Log Naming Format
Use this naming pattern: `SubsystemName/ValueName`

**Examples:**
*   `Shooter/CurrentState`
*   `Shooter/TargetVelocityRPM`
*   `Shooter/ActualVelocityRPM`
*   `Shooter/AtTarget`

### Recommended TalonFX Logs
*   Position (Rotations or Degrees)
*   Velocity (RPM or RPS)
*   Applied Motor Output (%)
*   Stator Current (Amps)
*   Device Temperature (Celsius)

---

## Adding a New Subsystem

Follow this exact sequence whenever you add a new mechanism:

1.  **Create the file:** Create `IntakeSubsystem.java` in the `subsystems/intake` folder.
2.  **Add states:** Define an `enum State { IDLE, INTAKING, OUTTAKING }` inside the class.
3.  **Add hardware mapping:** Open `config/IOAllocation.java` and add a constant for your subsystem's CAN ID.
4.  **Add constants:** Add `kIntakeSpeed` and other necessary constants to the subsystem.
5.  **Create motor hardware:** Instantiate your motor controller using the ID from `IOAllocation`.
6.  **Control mechanism:** Write a `switch` statement in `periodic()` that checks the current state and applies the correct motor output.
7.  **Add logs:** Publish the current state, motor RPM, and applied output to SmartDashboard in `periodic()`.
8.  **Add state commands:** Create a method `public void setState(State state)` and a WPILib command factory method.
9.  **Register it:** Instantiate `IntakeSubsystem intake = new IntakeSubsystem();` in `RobotContainer`.
10. **Bind controls:** In `ControlBoard`, bind a button to trigger the intake command.

---

## Command Format

Commands represent robot actions. Use a command when you need to:
*   Change a subsystem state
*   Coordinate multiple subsystems
*   Run a mechanism for a limited time
*   Wait for a sensor
*   Write an autonomous routine
*   Build a reusable robot action

### Command Patterns

| Pattern | Use when | Example |
| :--- | :--- | :--- |
| `Commands.run(...)` | The action continues until interrupted | Keep shooter spinning while held |
| `Commands.runOnce(...)` | The action occurs once and then finishes | Set arm state to `DOWN` |
| `.withTimeout(...)` | Stop after a fixed amount of time | Drive forward for 2 seconds |
| `.until(...)` | Stop when a condition becomes true | Intake until a sensor detects a note |
| `Commands.sequence(...)` | Run actions in order | Drive, shoot, then stop |
| `Commands.parallel(...)` | Run actions at the same time | Drive while raising an arm |

### Simple State Command

Subsystem commands should usually be short. The best way is to return a command directly from the subsystem:

```java
public Command setStateCommand(State newState) {
    return Commands.runOnce(() -> this.setState(newState), this);
}
```

---

## Example Drive Command

Create `DriveCommands.java` or add a factory method in your subsystem. Your `DriveSubsystem` needs a public method to apply output:

```java
public void drive(double forwardSpeed, double rotationSpeed) {
    // Math to apply speed to left and right motors
}
```

Set it as the default drive command in `RobotContainer`:

```java
driveSubsystem.setDefaultCommand(
    Commands.run(() -> {
        double fwd = controlBoard.getDriveForward();
        double rot = controlBoard.getDriveRotation();
        driveSubsystem.drive(fwd, rot);
    }, driveSubsystem)
);
```

This command runs continuously during teleoperated driving. It reads joystick values, applies deadband, and sends arcade-drive outputs to the drivetrain.

---

## Example Autonomous Command

Create an autonomous sequence inside `AutonomousCommands.java` or `RobotContainer`. 

Add these methods to `DriveSubsystem`:
```java
public void setDriveOutput(double percent) {
    // Applies raw percent output to both sides
}

public void stopDrive() {
    setDriveOutput(0.0);
}
```

Add this to `RobotContainer.java` to build the sequence:
```java
public Command getSimpleAutonomous() {
    return Commands.sequence(
        // 1. Make sure the shooter is idle
        shooter.setStateCommand(Shooter.State.IDLE),
        
        // 2. Drive forward at 40% output for 2 seconds
        Commands.run(() -> drive.setDriveOutput(0.4), drive).withTimeout(2.0),
        
        // 3. Stop the drivetrain
        Commands.runOnce(() -> drive.stopDrive(), drive),
        
        // 4. Spin the shooter for 1.5 seconds
        shooter.setStateCommand(Shooter.State.SHOOTING).withTimeout(1.5),
        
        // 5. Return the shooter to idle
        shooter.setStateCommand(Shooter.State.IDLE)
    );
}
```

---

## Controller Bindings

Controller bindings belong in: `control/ControlBoard.java`. 

**Example:**
```java
driverController.a().whileTrue(intake.setStateCommand(Intake.State.INTAKING));
driverController.b().onTrue(arm.setStateCommand(Arm.State.UP));
```

### Button Binding Choices

| Binding | Use case |
| :--- | :--- |
| `onTrue()` | Change a state once when pressed (e.g., Arm positioning) |
| `whileTrue()` | Run a command only while a button is held (e.g., Shooter activation) |
| `onFalse()` | Run a command when a button is released |
| `toggleOnTrue()` | Toggle a command on and off |

---

## Code Style Rules

### Naming Conventions

| Item | Style | Example |
| :--- | :--- | :--- |
| Class | PascalCase | `ExampleShooterSubsystem` |
| Method | camelCase | `setTargetState()` |
| Variable | camelCase | `targetVelocityRps` |
| IOAllocation Constant | UPPER_SNAKE_CASE | `MOTOR_ID` |
| Hard constant | `k` + PascalCase | `kMotorInverted` |
| NetworkTables entry | camelCase | `shootVelocityRps` |
| Enum value | UPPER_SNAKE_CASE | `SLOW_SHOOT` |
| Dashboard key | `Subsystem/ValueName` | `ExampleShooter/TopVelocityRPS` |

### Keep Responsibilities Separate

| Location | Should contain |
| :--- | :--- |
| **IOAllocation** | Mapping of CAN IDs, DIO ports, PWM channels, and analog inputs |
| **Subsystem** | Motors, sensors, states, configuration, periodic control, dashboard logs |
| **Command** | A requested action, command sequence, timing, multi-subsystem coordination |
| **ControlBoard** | Controller mappings and button bindings |
| **RobotContainer** | Subsystem construction, control construction, autonomous selection |
| **Robot** | Robot lifecycle methods and command scheduler |

*   **Do not** put button logic inside a subsystem.
*   **Do not** put direct motor-control code inside `ControlBoard`.
*   **Do not** put entire autonomous routines inside `Robot.java`.

---

## Testing Checklist

Before testing a new subsystem on the real robot:
*   [ ] Verify the CAN ID in `IOAllocation.java`.
*   [ ] Verify motor inversion.
*   [ ] Keep mechanisms safely elevated or disconnected when possible.
*   [ ] Start with low voltage, low percent output, or low target speed.
*   [ ] Confirm the desired state appears in SmartDashboard.
*   [ ] Confirm sensor position and velocity update correctly.
*   [ ] Confirm current draw is reasonable.
*   [ ] Watch motor temperature.
*   [ ] Confirm `IDLE` stops motors that should not remain powered.
*   [ ] Tune `kP` before adjusting `kI` and `kD`.
*   [ ] Test autonomous routines in a clear, safe area.

---

## Common Problems

| Problem | Likely Cause | First Check |
| :--- | :--- | :--- |
| **Motor does not move** | Wrong CAN ID, disabled robot, command not scheduled | CAN ID in IOAllocation, robot enabled state, subsystem dashboard state |
| **Motor moves backward** | Incorrect inversion | `InvertedValue` in subsystem configuration |
| **Arm targets wrong position** | Encoder units or setpoints are incorrect | Actual position dashboard value vs target position |
| **Shooter does not reach target speed**| Incorrect PID, friction, battery voltage, inversion | Actual velocity, target velocity, current draw |
| **Shooter remains on** | Missing or broken `IDLE` behavior | Shooter default command and `IDLE` state |
| **Two commands fight each other** | Commands have incorrect subsystem requirements | Command construction and `addRequirements()` |
| **NetworkTables update has no effect** | Motor config applied only in constructor | Add an intentional config-apply method/command |
| **Joystick drifts** | Deadband too small | Driver deadband NetworkTables entry |


