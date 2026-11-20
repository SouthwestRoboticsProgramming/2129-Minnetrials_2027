package com.swrobotics.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public interface VisionIO {
    class VisionIOInputs implements LoggableInputs {
        public boolean frontHasTarget = false;
        public int frontTagId = -1;
        public double frontTx = 0.0;
        public Pose2d frontPose = Pose2d.kZero;
        public double frontTimestamp = 0.0;
        public int frontTagCount = 0;
        public double frontAvgDist = 0.0;

        public boolean rearHasTarget = false;
        public int rearTagId = -1;
        public Pose2d rearPose = Pose2d.kZero;
        public double rearTimestamp = 0.0;
        public int rearTagCount = 0;
        public double rearAvgDist = 0.0;

        @Override
        public void toLog(LogTable table) {
            table.put("FrontHasTarget", frontHasTarget);
            table.put("FrontTagId", frontTagId);
            table.put("FrontTx", frontTx);
            table.put("FrontPose", frontPose);
            table.put("FrontTimestamp", frontTimestamp);
            table.put("FrontTagCount", frontTagCount);
            table.put("FrontAvgDist", frontAvgDist);

            table.put("RearHasTarget", rearHasTarget);
            table.put("RearTagId", rearTagId);
            table.put("RearPose", rearPose);
            table.put("RearTimestamp", rearTimestamp);
            table.put("RearTagCount", rearTagCount);
            table.put("RearAvgDist", rearAvgDist);
        }

        @Override
        public void fromLog(LogTable table) {
            frontHasTarget = table.get("FrontHasTarget", frontHasTarget);
            frontTagId = (int) table.get("FrontTagId", (long) frontTagId);
            frontTx = table.get("FrontTx", frontTx);
            frontPose = table.get("FrontPose", frontPose);
            frontTimestamp = table.get("FrontTimestamp", frontTimestamp);
            frontTagCount = (int) table.get("FrontTagCount", (long) frontTagCount);
            frontAvgDist = table.get("FrontAvgDist", frontAvgDist);

            rearHasTarget = table.get("RearHasTarget", rearHasTarget);
            rearTagId = (int) table.get("RearTagId", (long) rearTagId);
            rearPose = table.get("RearPose", rearPose);
            rearTimestamp = table.get("RearTimestamp", rearTimestamp);
            rearTagCount = (int) table.get("RearTagCount", (long) rearTagCount);
            rearAvgDist = table.get("RearAvgDist", rearAvgDist);
        }
    }

    default void updateInputs(VisionIOInputs inputs) {}
}