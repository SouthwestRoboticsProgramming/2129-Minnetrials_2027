package com.swrobotics.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Robot extends LoggedRobot {
    private static final Queue<Runnable> mainThreadOperations = new ConcurrentLinkedQueue<>();

    public static void runOnMainThread(Runnable runnable) {
        mainThreadOperations.add(runnable);
    }

    private Command m_autonomousCommand;
    private final RobotContainer m_robotContainer;

    public Robot() {
        Logger.recordMetadata("ProjectName", "SWRobotics-MinneTrials-2026");

        if (isReal()) {
            Logger.addDataReceiver(new WPILOGWriter()); // Saves .wpilog to USB drive
        }
        // Always stream live NT4 data so Elastic & AdvantageScope work in both Real and Sim
        Logger.addDataReceiver(new NT4Publisher());
        Logger.start();

        m_robotContainer = new RobotContainer();
    }

    @Override
    public void robotPeriodic() {
        while (!mainThreadOperations.isEmpty()) {
            mainThreadOperations.poll().run();
        }
        CommandScheduler.getInstance().run();
    }

    @Override
    public void disabledInit() {
        m_robotContainer.disabledInit();
    }

    @Override
    public void autonomousInit() {
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();
        if (m_autonomousCommand != null) {
            CommandScheduler.getInstance().schedule(m_autonomousCommand);
        }
    }

    @Override
    public void teleopInit() {
        if (m_autonomousCommand != null) {
            m_autonomousCommand.cancel();
        }
    }

    @Override
    public void testInit() {
        CommandScheduler.getInstance().cancelAll();
    }
}