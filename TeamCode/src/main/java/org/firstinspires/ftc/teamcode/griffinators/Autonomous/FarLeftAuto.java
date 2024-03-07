package org.firstinspires.ftc.teamcode.griffinators.Autonomous;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.griffinators.Parts.ARM_POSITIONS;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Arm;
import org.firstinspires.ftc.teamcode.griffinators.Parts.CLAW_ROTATION;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Claw;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Detection;

import java.util.ArrayList;

@Config
@Autonomous(name = "Far Left", group = "Auto")
public class FarLeftAuto extends LinearOpMode {
    public static class Params{
        public double _0initX = 24;
        public double _0initY = 60;
        public double _0initRot = Math.PI / 2;

        public double _1DetectionStrafeX = 3;

        public double _2strafeFrontY = 23.5;
        public double _2strafeFrontX = 3.5;

        public double _2strafeRightY = 12.5;
        public double _2strafeRightX = 9.5;


        public double _2splineLeftY = 24;
        public double _2splineLeftX = -0.5;
        public double _2splineLeftRot = 3;
    }
    public static Params params = new Params();
    private MecanumDrive drive;
    private Detection detection;
    private Claw claw;
    private Arm arm;
    @Override
    public void runOpMode() throws InterruptedException {
        drive = new MecanumDrive(hardwareMap, new Pose2d(params._0initX, params._0initY, params._0initRot));
        detection = new Detection(hardwareMap);
        claw = new Claw(hardwareMap);
        arm = new Arm(hardwareMap);

        Action moveToRecognitionPosition = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._1DetectionStrafeX, 0)))
                .build();


        Action moveToLeftPixelPos = drive.actionBuilder(drive.pose)
                .splineTo(drive.pose.position.plus(new Vector2d(params._2splineLeftX, params._2splineLeftY)), params._2splineLeftRot)
                .build();
        Action moveToCenterPixelPos = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._2strafeFrontX, params._2strafeFrontY)))
                .build();
        Action moveToRightPixelPos = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._2strafeRightX, params._2strafeRightY)))
                .build();

        claw.closeLeft();
        claw.closeRight();
        claw.controlRotation(CLAW_ROTATION.HIDDEN);
        arm.setPosition(ARM_POSITIONS.GROUND, false);
        waitForStart();
        Actions.runBlocking(moveToRecognitionPosition);
        recognitionPos();

        ArrayList<Recognition> recognitions = new ArrayList<>();
        Actions.runBlocking(detection.getRecognition(recognitions, 5));
        int pixelPos = parsePixelPos(recognitions);
        telemetry.addData("Pixel pos", "" + pixelPos);
        telemetry.update();
        claw.controlRotation(CLAW_ROTATION.GROUND);
        sleep(900);
        arm.setPosition(ARM_POSITIONS.GROUND, true);
        switch (pixelPos){
            case 0:
                Actions.runBlocking(moveToLeftPixelPos);
                break;
            case 1:
                Actions.runBlocking(moveToCenterPixelPos);
                break;
            case 2:
                Actions.runBlocking(moveToRightPixelPos);
                break;
        }
        claw.openLeft();
        sleep(500);

    }

    private int parsePixelPos(ArrayList<Recognition> recognitions) {
        if (recognitions.size() == 0) {
            return 0;
        } else {
            if ((recognitions.get(0).getRight() + recognitions.get(0).getLeft()) / 2 > 320) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    private void recognitionPos(){
        claw.controlRotation(CLAW_ROTATION.DETECTION);
        arm.setPosition(ARM_POSITIONS.DETECTION, true);
        sleep(300);
    }
}
