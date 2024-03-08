package org.firstinspires.ftc.teamcode.griffinators.Parts;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Config
public final class Claw {
    public static class Params {
        public double CLAW_CONTROL_DETECTION_POSITION = 0.365;
        public double CLAW_CONTROL_GROUND_POS = 0.48;
        public double CLAW_CONTROL_CLOSED_POS = 0.61;
        public double CLAW_CONTROL_BOARD_POS = 0.54;

        public double LEFT_CLAW_REST = 0.75;
        public double RIGHT_CLAW_REST = 0.2;

        public double LEFT_CLAW_HOLD = 0.585;
        public double RIGHT_CLAW_HOLD = 0.34;
    }

    private final Servo clawControl;
    private final Servo clawLeft;
    private final Servo clawRight;

    public static Params PARAMS = new Params();
    public Claw(HardwareMap hardwareMap){
        clawControl = hardwareMap.servo.get("c");
        clawLeft = hardwareMap.servo.get("cr");
        clawRight = hardwareMap.servo.get("cl");
    }

    public void openLeft(){
        clawLeft.setPosition(PARAMS.LEFT_CLAW_REST);
    }
    public void closeLeft(){
        clawLeft.setPosition(PARAMS.LEFT_CLAW_HOLD);
    }

    public void openRight(){
        clawRight.setPosition(PARAMS.RIGHT_CLAW_REST);
    }
    public void closeRight(){
        clawRight.setPosition(PARAMS.RIGHT_CLAW_HOLD);
    }
    public void controlRotation(CLAW_ROTATION rotation){
        switch (rotation){
            case DETECTION: clawControl.setPosition(PARAMS.CLAW_CONTROL_DETECTION_POSITION);
                break;
            case GROUND: clawControl.setPosition(PARAMS.CLAW_CONTROL_GROUND_POS);
                break;
            case HIDDEN: clawControl.setPosition(PARAMS.CLAW_CONTROL_CLOSED_POS);
                break;
            case BOARD:
                clawControl.setPosition(PARAMS.CLAW_CONTROL_BOARD_POS);
                break;
        }
    }
}
