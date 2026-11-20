package com.swrobotics.robot.subsystems.scoringArm;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismRoot2d;

public class ScoringArmSubsystem extends SubsystemBase {

    public enum State {
        SCORE_MILK,
        SCORE_COOKIE,
        STOWING,
        HOMING
    }

    private final ScoringArmIO io;
    private final ScoringArmIO.ScoringArmIOInputs inputs = new ScoringArmIO.ScoringArmIOInputs();

    // Tunables in NetworkTables / Elastic
    private final NTEntry<Double> zeroOffsetDegrees =
            new NTDouble("ScoringArm/ZeroOffsetDeg", 0.0).setPersistent();
    private final NTEntry<Double> toleranceDegrees =
            new NTDouble("ScoringArm/ToleranceDeg", 2.0).setPersistent();
    private final NTEntry<Double> milkTargetDegrees =
            new NTDouble("ScoringArm/MilkTargetDeg", 45.0).setPersistent();
    private final NTEntry<Double> cookieTargetDegrees =
            new NTDouble("ScoringArm/CookieTargetDeg", 75.0).setPersistent();
    private final NTEntry<Double> stowTargetDegrees =
            new NTDouble("ScoringArm/StowTargetDeg", 0.0).setPersistent();

    // LoggedMechanism2d works with both AdvantageKit Logger and Elastic/Glass SmartDashboard
    private final LoggedMechanism2d mech2d = new LoggedMechanism2d(60, 60);
    private final LoggedMechanismLigament2d armLigament;

    private final Timer homingTimer = new Timer();
    private State targetState = State.STOWING;
    private boolean wasEnabled = false;

    public ScoringArmSubsystem(ScoringArmIO io) {
        this.io = io;

        LoggedMechanismRoot2d pivot = mech2d.getRoot("ArmPivot", 20, 15);
        pivot.append(new LoggedMechanismLigament2d("Mast", 15, 90, 6, new Color8Bit(Color.kGray)));
        armLigament = pivot.append(
                new LoggedMechanismLigament2d("ScoringArm", 28, 0, 6, new Color8Bit(Color.kOrange))
        );

        SmartDashboard.putData("ScoringArm/Mechanism", mech2d);
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("ScoringArm", inputs);

        boolean isEnabled = DriverStation.isEnabled();
        if (isEnabled != wasEnabled) {
            wasEnabled = isEnabled;
            io.setBrakeMode(isEnabled);
        }

        double targetDeg = switch (targetState) {
            case SCORE_MILK -> milkTargetDegrees.get();
            case SCORE_COOKIE -> cookieTargetDegrees.get();
            case STOWING, HOMING -> stowTargetDegrees.get();
        };

        switch (targetState) {
            case SCORE_MILK:
            case SCORE_COOKIE:
            case STOWING:
                double targetMotorRotations = Units.degreesToRotations(targetDeg - zeroOffsetDegrees.get());
                io.setPositionRotations(targetMotorRotations);
                break;

            case HOMING:
                io.setVoltage(-3.0);
                boolean bypassedInrush = homingTimer.hasElapsed(0.25);
                boolean isStalled = inputs.statorCurrentAmps > 20.0;

                if (RobotBase.isSimulation() || (bypassedInrush && isStalled) || homingTimer.hasElapsed(2.0)) {
                    io.resetEncoderPosition(0.0);
                    targetState = State.STOWING;
                }
                break;
        }

        double trueAngleDeg = getTrueAngleDegrees();
        boolean atTarget = isAtTarget();

        armLigament.setAngle(trueAngleDeg);

        // Log to AdvantageKit & SmartDashboard (Elastic)
        Logger.recordOutput("ScoringArm/State", targetState.name());
        Logger.recordOutput("ScoringArm/TrueAngleDeg", trueAngleDeg);
        Logger.recordOutput("ScoringArm/TargetAngleDeg", targetDeg);
        Logger.recordOutput("ScoringArm/AtTarget", atTarget);
        Logger.recordOutput("ScoringArm/Mechanism2d", mech2d);

        SmartDashboard.putString("ScoringArm/State", targetState.name());
        SmartDashboard.putNumber("ScoringArm/TrueAngleDeg", trueAngleDeg);
        SmartDashboard.putNumber("ScoringArm/TargetAngleDeg", targetDeg);
        SmartDashboard.putBoolean("ScoringArm/AtTarget", atTarget);
        SmartDashboard.putNumber("ScoringArm/CurrentAmps", inputs.statorCurrentAmps);
        SmartDashboard.putNumber("ScoringArm/TempCelsius", inputs.tempCelsius);
    }

    /** Returns the arm angle in degrees after applying your physical zero-point offset. */
    public double getTrueAngleDegrees() {
        return Units.rotationsToDegrees(inputs.rawRotations) + zeroOffsetDegrees.get();
    }

    /** Call this when the arm is physically at your desired 0-degree position to calibrate it immediately. */
    public void calibrateCurrentPositionAsZero() {
        double currentRawDeg = Units.rotationsToDegrees(inputs.rawRotations);
        zeroOffsetDegrees.set(-currentRawDeg);
    }

    public boolean isAtTarget() {
        double targetDeg = switch (targetState) {
            case SCORE_MILK -> milkTargetDegrees.get();
            case SCORE_COOKIE -> cookieTargetDegrees.get();
            case STOWING, HOMING -> stowTargetDegrees.get();
        };
        return Math.abs(targetDeg - getTrueAngleDegrees()) <= toleranceDegrees.get();
    }

    public void setTargetState(State state) {
        if (state == State.HOMING && targetState != State.HOMING) {
            homingTimer.restart();
        }
        targetState = state;
    }

    public Command commandSetState(State state) {
        return Commands.runOnce(() -> setTargetState(state), this)
                .withName("Arm " + state.name());
    }
}