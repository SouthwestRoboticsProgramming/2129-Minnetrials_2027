<div align="center">

# SW Robotics Minnetrials Code

### Command-Based Java Guide For New Code Team Members

</div>

---

> **Purpose:** This repository contains the command-based Java code for the SW Robotics Minnetrials robot. This document explains our code structure, subsystem format, NetworkTables tuning, SmartDashboard logging, commands, controller bindings, autonomous routines, IO Allocation, and how to use them to make your own robot.

---

# Project Structure

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
│   
└──
<table>
    <thead>
        <tr>
            <th>Location</th>
            <th>Responsibility</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code>lib/</code></td>
            <td>This has information that allows your network tables to run. Don't really need to edit.</td>
        </tr>
        <tr>
            <td><code>commands/</code></td>
            <td>Contains reusable actions and autonomous sequences (for example, autoalign). Edit this.</td>
        </tr>
        <tr>
            <td><code>config/</code></td>
            <td>Contains global constants for field positions, <strong>IO Allocation (CAN/Ports)</strong>, and Robot constants. Edit this.</td>
        </tr>
        <tr>
            <td><code>control/</code></td>
            <td>Maps controls, default commands, and button bindings. Edit this.</td>
        </tr>
        <tr>
            <td><code>logging/</code></td>
            <td>Used so you can see where your robot is on the field in sim and real game.</td>
        </tr>
        <tr>
            <td><code>subsystems/</code></td>
            <td>Controls individual physical mechanisms. Create new ones here.</td>
        </tr>
        <tr>
            <td><code>Main.java</code></td>
            <td>Starts the robot program. No need to mess with.</td>
        </tr>
        <tr>
            <td><code>Robot.java</code></td>
            <td>Runs robot lifecycle methods and the command scheduler. Not really needed to mess with.</td>
        </tr>
        <tr>
            <td><code>RobotContainer.java</code></td>
            <td>Creates subsystems and autonomous routines. You need to use this.</td>
        </tr>
    </tbody>
</table>

<h1>Command-Based Overview</h1>

<p>This project uses the WPILib command-based framework.</p>

<p>A <strong>subsystem</strong> represents a physical robot mechanism. A <strong>command</strong> represents an action the robot can perform with one or more mechanisms.</p>

<table>
    <thead>
        <tr>
            <th>Type</th>
            <th>Description</th>
            <th>Examples</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Subsystem</td>
            <td>Owns hardware and mechanism behavior</td>
            <td>Drivebase, arm, shooter, intake</td>
        </tr>
        <tr>
            <td>Command</td>
            <td>Requests a robot action</td>
            <td>Drive with joystick, move arm down, spin shooter</td>
        </tr>
        <tr>
            <td>State</td>
            <td>Describes the intended mode of a subsystem</td>
            <td><code>IDLE</code>, <code>SHOOT</code>, <code>UP</code>, <code>DOWN</code></td>
        </tr>
    </tbody>
</table>

<p>The expected flow is:</p>
<p>A subsystem owns its motors, sensors, configuration, states, NetworkTables entries, and SmartDashboard logs.</p>

<p>A command should request a subsystem behavior. It should not contain random motor-control code that belongs inside a subsystem.</p>

<hr>

<h1>IO Allocation (Hardware Mapping)</h1>

<p>To prevent conflicts where two motors accidentally share the same CAN ID or sensor port, <strong>ALL hardware IDs must be defined in <code>config/IOAllocation.java</code></strong>. Do not hard-code CAN IDs directly inside your subsystem classes.</p>

<p>Example <code>IOAllocation.java</code> structure:</p>
<hr>

<h1>Subsystem Format</h1>

<p>Every physical robot mechanism should have a subsystem.</p>

<p>Examples:</p>
<p>A subsystem should usually follow this organization:</p>
<h2>Basic subsystem template</h2>
<h2>State rules</h2>

<p>States should describe a real mechanism behavior.</p>

<p>Good state names:</p>
<p>Avoid state names based on code details or controller buttons.</p>

<p>Avoid:</p>
<hr>

<h1>Constants and Tunables</h1>

<p>Every number in robot code should have a name and a reason.</p>

<p>Don't leave unexplained numbers directly in motor-control calls; otherwise, it will be hard to tell what they mean or are.</p>

<p>Bad:</p>
<p>Better:</p>
<h2>Hard-coded configuration constants</h2>

<p>Use hard-coded values for fixed design decisions that are not related to IO mapping.</p>
<p>Use hard-coded constants for:</p>

<ul>
    <li>Motor inversion</li>
    <li>Brake/coast mode</li>
    <li>Fixed physical configuration (gear ratios)</li>
    <li>Static safety choices</li>
</ul>

<h2>NetworkTables tunables</h2>

<p>Use NetworkTables entries for numbers that may change during robot testing, calibration, driver practice, or mechanism tuning.</p>
<p>Use NetworkTables values for:</p>

<ul>
    <li>PID values</li>
    <li>Arm setpoints</li>
    <li>Shooter speed targets</li>
    <li>Drive speed scaling</li>
    <li>Joystick deadband</li>
    <li>Motion Magic velocity and acceleration</li>
    <li>Current limits that may need testing adjustments</li>
    <li>Vision aiming constants</li>
</ul>

<h2>PID overview</h2>

<table>
    <thead>
        <tr>
            <th>Term</th>
            <th>Full Name</th>
            <th>Purpose</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code>kP</code></td>
            <td>Proportional</td>
            <td>Responds to the current error</td>
        </tr>
        <tr>
            <td><code>kI</code></td>
            <td>Integral</td>
            <td>Corrects error that remains for a long time</td>
        </tr>
        <tr>
            <td><code>kD</code></td>
            <td>Derivative</td>
            <td>Reduces overshoot and oscillation</td>
        </tr>
    </tbody>
</table>

<p>Safe tuning order:</p>

<ol>
    <li>Start with <code>kI = 0.0</code> and <code>kD = 0.0</code>.</li>
    <li>Increase <code>kP</code> until the mechanism responds well.</li>
    <li>Add a small <code>kD</code> only if the mechanism overshoots or oscillates.</li>
    <li>Add a very small <code>kI</code> only if a consistent remaining error cannot be fixed another way.</li>
</ol>

<hr>

<h1>SmartDashboard Logging</h1>

<p>Every subsystem should report useful information to SmartDashboard.</p>

<p>The goal of dashboard logging is to answer:</p>

<ul>
    <li>What state is the subsystem in?</li>
    <li>What target did the code request?</li>
    <li>What does the sensor actually read?</li>
    <li>Is the mechanism at the target?</li>
    <li>Is the motor drawing too much current?</li>
    <li>Is the motor getting too hot?</li>
</ul>

<h2>Log naming format</h2>

<p>Use this naming pattern:</p>
<p>Examples:</p>
<h2>Recommended TalonFX logs</h2>
<hr>

<h1>Adding a New Subsystem</h1>

<p>Follow this process whenever you add a new mechanism.</p>

<h2>1. Create the file</h2>

<p>For an intake:</p>
<h2>2. Add states</h2>
<h2>3. Add hardware mapping to IOAllocation</h2>

<p>Open <code>config/IOAllocation.java</code> and add a class for your subsystem:</p>
<h2>4. Add tunable values and configuration constants</h2>
<h2>5. Create motor hardware using IOAllocation</h2>
<h2>6. Control the mechanism in <code>periodic()</code></h2>
<h2>7. Add SmartDashboard logs</h2>
<h2>8. Add state methods and a command</h2>
<h2>9. Create it in <code>RobotContainer</code></h2>
<h2>10. Bind a controller input</h2>
<hr>

<h1>Command Format</h1>

<p>Commands represent robot actions.</p>

<p>Use a command when you need to:</p>

<ul>
    <li>Change a subsystem state</li>
    <li>Coordinate multiple subsystems</li>
    <li>Run a mechanism for a limited time</li>
    <li>Wait for a sensor</li>
    <li>Write an autonomous routine</li>
    <li>Build a reusable robot action</li>
</ul>

<h2>Command patterns</h2>

<table>
    <thead>
        <tr>
            <th>Pattern</th>
            <th>Use when</th>
            <th>Example</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code>Commands.run(...)</code></td>
            <td>The action continues until interrupted</td>
            <td>Keep shooter spinning while held</td>
        </tr>
        <tr>
            <td><code>Commands.runOnce(...)</code></td>
            <td>The action occurs once and then finishes</td>
            <td>Set arm state to <code>DOWN</code></td>
        </tr>
        <tr>
            <td><code>.withTimeout(...)</code></td>
            <td>Stop after a fixed amount of time</td>
            <td>Drive forward for 2 seconds</td>
        </tr>
        <tr>
            <td><code>.until(...)</code></td>
            <td>Stop when a condition becomes true</td>
            <td>Intake until a sensor detects a note</td>
        </tr>
        <tr>
            <td><code>Commands.sequence(...)</code></td>
            <td>Run actions in order</td>
            <td>Drive, shoot, then stop</td>
        </tr>
        <tr>
            <td><code>Commands.parallel(...)</code></td>
            <td>Run actions at the same time</td>
            <td>Drive while raising an arm</td>
        </tr>
    </tbody>
</table>

<h2>Simple state command</h2>

<p>Subsystem commands should usually be short:</p>
<hr>

<h1>Example Drive Command</h1>

<p>Create:</p>
<p>Your <code>DriveSubsystem</code> needs these public methods:</p>
<p>Set it as the default drive command:</p>
<p>This command runs continuously during teleoperated driving. It reads joystick values, applies deadband, and sends arcade-drive outputs to the drivetrain.</p>

<hr>

<h1>Example Autonomous Command</h1>

<p>Create:</p>
<p>The routine does this:</p>

<table>
    <thead>
        <tr>
            <th>Step</th>
            <th>Action</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>1</td>
            <td>Make sure the shooter is idle</td>
        </tr>
        <tr>
            <td>2</td>
            <td>Drive forward at 40% output for 2 seconds</td>
        </tr>
        <tr>
            <td>3</td>
            <td>Stop the drivetrain</td>
        </tr>
        <tr>
            <td>4</td>
            <td>Spin the shooter for 1.5 seconds</td>
        </tr>
        <tr>
            <td>5</td>
            <td>Return the shooter to idle</td>
        </tr>
    </tbody>
</table>

<p>Add these methods to <code>DriveSubsystem</code>:</p>
<p>Add this to <code>RobotContainer.java</code>:</p>
<hr>

<h1>Controller Bindings</h1>

<p>Controller bindings belong in:</p>
<p>Example:</p>
<h2>Button binding choices</h2>

<table>
    <thead>
        <tr>
            <th>Binding</th>
            <th>Use case</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code>onTrue()</code></td>
            <td>Change a state once when pressed</td>
        </tr>
        <tr>
            <td><code>whileTrue()</code></td>
            <td>Run a command only while a button is held</td>
        </tr>
        <tr>
            <td><code>onFalse()</code></td>
            <td>Run a command when a button is released</td>
        </tr>
        <tr>
            <td><code>toggleOnTrue()</code></td>
            <td>Toggle a command on and off</td>
        </tr>
    </tbody>
</table>

<p>Use <code>onTrue()</code> for arm positions because the arm should stay at the requested position.</p>

<p>Use <code>whileTrue()</code> for a shooter when the shooter should stop immediately after the driver releases the button.</p>

<hr>

<h1>Code Style Rules</h1>

<h2>Naming</h2>
<table>
    <thead>
        <tr>
            <th>Item</th>
            <th>Style</th>
            <th>Example</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Class</td>
            <td>PascalCase</td>
            <td><code>ExampleShooterSubsystem</code></td>
        </tr>
        <tr>
            <td>Method</td>
            <td>camelCase</td>
            <td><code>setTargetState()</code></td>
        </tr>
        <tr>
            <td>Variable</td>
            <td>camelCase</td>
            <td><code>targetVelocityRps</code></td>
        </tr>
        <tr>
            <td>IOAllocation Constant</td>
            <td>UPPER_SNAKE_CASE</td>
            <td><code>MOTOR_ID</code></td>
        </tr>
        <tr>
            <td>Hard constant</td>
            <td><code>k</code> + PascalCase</td>
            <td><code>kMotorInverted</code></td>
        </tr>
        <tr>
            <td>NetworkTables entry</td>
            <td>camelCase</td>
            <td><code>shootVelocityRps</code></td>
        </tr>
        <tr>
            <td>Enum value</td>
            <td>UPPER_SNAKE_CASE</td>
            <td><code>SLOW_SHOOT</code></td>
        </tr>
        <tr>
            <td>Dashboard key</td>
            <td><code>Subsystem/ValueName</code></td>
            <td><code>ExampleShooter/TopVelocityRPS</code></td>
        </tr>
    </tbody>
</table>

<h2>Keep responsibilities separate</h2>

<table>
    <thead>
        <tr>
            <th>Location</th>
            <th>Should contain</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>IOAllocation</td>
            <td>Mapping of CAN IDs, DIO ports, PWM channels, and analog inputs</td>
        </tr>
        <tr>
            <td>Subsystem</td>
            <td>Motors, sensors, subsystem states, motor configuration, periodic control, dashboard logs</td>
        </tr>
        <tr>
            <td>Command</td>
            <td>A requested action, command sequence, timing, multi-subsystem coordination</td>
        </tr>
        <tr>
            <td>ControlBoard</td>
            <td>Controller mappings and button bindings</td>
        </tr>
        <tr>
            <td>RobotContainer</td>
            <td>Subsystem construction, control construction, autonomous selection</td>
        </tr>
        <tr>
            <td>Robot</td>
            <td>Robot lifecycle methods and command scheduler</td>
        </tr>
    </tbody>
</table>

<p>Do not put button logic inside a subsystem.</p>

<p>Do not put direct motor-control code inside <code>ControlBoard</code>.</p>

<p>Do not put entire autonomous routines inside <code>Robot.java</code>.</p>

<hr>

<h1>Testing Checklist</h1>

<p>Before testing a new subsystem:</p>

<ul>
    <li>Verify the CAN ID in <code>IOAllocation.java</code>.</li>
    <li>Verify motor inversion.</li>
    <li>Keep mechanisms safely elevated or disconnected when possible.</li>
    <li>Start with low voltage, low percent output, or low target speed.</li>
    <li>Confirm the desired state appears in SmartDashboard.</li>
    <li>Confirm sensor position and velocity update correctly.</li>
    <li>Confirm current draw is reasonable.</li>
    <li>Watch motor temperature.</li>
    <li>Confirm <code>IDLE</code> stops motors that should not remain powered.</li>
    <li>Tune <code>kP</code> before adjusting <code>kI</code> and <code>kD</code>.</li>
    <li>Test autonomous routines in a clear area.</li>
</ul>

<hr>

<h1>Common Problems</h1>

<table>
    <thead>
        <tr>
            <th>Problem</th>
            <th>Likely Cause</th>
            <th>First Check</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Motor does not move</td>
            <td>Wrong CAN ID, disabled robot, command not scheduled</td>
            <td>CAN ID in IOAllocation, robot enabled state, subsystem dashboard state</td>
        </tr>
        <tr>
            <td>Motor moves backward</td>
            <td>Incorrect inversion</td>
            <td><code>InvertedValue</code> in subsystem configuration</td>
        </tr>
        <tr>
            <td>Arm targets wrong position</td>
            <td>Encoder units or setpoints are incorrect</td>
            <td>Actual position dashboard value and target position</td>
        </tr>
        <tr>
            <td>Shooter does not reach target speed</td>
            <td>Incorrect PID, friction, battery voltage, inversion</td>
            <td>Actual velocity, target velocity, current draw</td>
        </tr>
        <tr>
            <td>Shooter remains on</td>
            <td>Missing or broken <code>IDLE</code> behavior</td>
            <td>Shooter default command and <code>IDLE</code> state</td>
        </tr>
        <tr>
            <td>Two commands fight each other</td>
            <td>Commands have incorrect subsystem requirements</td>
            <td>Command construction and <code>addRequirements()</code></td>
        </tr>
        <tr>
            <td>NetworkTables update has no effect</td>
            <td>Motor configuration was applied only in constructor</td>
            <td>Add an intentional config-apply command</td>
        </tr>
        <tr>
            <td>Joystick drifts</td>
            <td>Deadband too small</td>
            <td>Driver deadband NetworkTables entry</td>
        </tr>
    </tbody>
</table>

<hr>

<div class="center">
    <h2>SW Robotics Code Standard</h2>
    <p><strong>Build clearly. Log useful data. Tune carefully. Test safely.</strong></p>
</div>