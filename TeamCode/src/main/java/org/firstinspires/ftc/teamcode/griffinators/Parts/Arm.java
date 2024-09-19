package org.firstinspires.ftc.teamcode.griffinators.Parts;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public final class Arm
{
    public static class Params
    {
        public int DETECTION_ARM_ROTATION = 500;
        public int DETECTION_ARM_EXTENSION = 0;
        public int GROUND_ARM_ROTATION = 125;
        public int GROUND_ARM_EXTENSION = 0;
        public int BOARD_ARM_ROTATION = 215;
        public int BOARD_ARM_EXTENSION = 340;
    }

    DcMotor armExtendLeft;
    DcMotor armExtendRight;
    DcMotor armControlLeft;
    DcMotor armControlRight;

    public static Params PARAMS = new Params();

    public Arm(HardwareMap hardwareMap)
    {
        armExtendLeft = hardwareMap.dcMotor.get("acl");
        armExtendRight = hardwareMap.dcMotor.get("acr");
        armControlLeft = hardwareMap.dcMotor.get("ael");
        armControlRight = hardwareMap.dcMotor.get("aer");

        armControlLeft.setDirection(DcMotor.Direction.REVERSE);
        armExtendLeft.setDirection(DcMotor.Direction.REVERSE);

        armExtendLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armExtendRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armControlLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armControlRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        armControlLeft.setTargetPosition(0);
        armControlRight.setTargetPosition(0);
        armExtendLeft.setTargetPosition(0);
        armExtendRight.setTargetPosition(0);

        armControlLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armControlRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armExtendLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armExtendRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        armExtendLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armExtendRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armControlLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armControlRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armExtendLeft.setPower(1f);
        armExtendRight.setPower(1f);
        armControlLeft.setPower(1f);
        armControlRight.setPower(1f);
    }
    public void setPosition(ARM_POSITIONS armPosition, boolean blocking)
    {
        switch (armPosition)
        {
            case GROUND:
                armControlLeft.setPower(1);
                armControlRight.setPower(1);
                setExtension(PARAMS.GROUND_ARM_EXTENSION, false);
                setRotation(PARAMS.GROUND_ARM_ROTATION, blocking);
                break;
            case BOARD:
                setExtension(PARAMS.BOARD_ARM_EXTENSION, false);
                setRotation(PARAMS.BOARD_ARM_ROTATION, blocking);
                break;
            case DETECTION:
                setExtension(PARAMS.DETECTION_ARM_EXTENSION, false);
                setRotation(PARAMS.DETECTION_ARM_ROTATION, blocking);
                break;
        }
    }

    public void setExtension(int amount, boolean blocking)
    {
        armExtendLeft.setTargetPosition(amount);
        armExtendRight.setTargetPosition(amount);
        if (blocking) waitForArmsToFinish();
    }

    public void setRotation(int amount, boolean blocking)
    {
        armControlLeft.setTargetPosition(amount);
        armControlRight.setTargetPosition(amount);
        if (blocking) waitForArmsToFinish();
    }
    private void waitForArmsToFinish()
    {
        while ((armControlLeft.isBusy() || armControlRight.isBusy()) && (armExtendLeft.isBusy() || armExtendRight.isBusy()));
    }
}
