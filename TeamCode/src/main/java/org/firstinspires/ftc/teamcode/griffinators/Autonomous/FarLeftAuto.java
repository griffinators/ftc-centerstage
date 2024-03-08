package org.firstinspires.ftc.teamcode.griffinators.Autonomous;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

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
        public double _0initX = -24;
        public double _0initY = 60;
        public double _0initRot = -Math.PI / 2;

        public double _1DetectionStrafeX = -3;

        public double _2strafeFrontY = -23.5;
        public double _2strafeFrontX = -3.5;

        public double _2strafeRightY = -12.5;
        public double _2strafeRightX = -9.5;


        public double _2splineLeftY = -24;
        public double _2splineLeftX = 0;
        public double _2splineLeftRot = 3;


        public double _3splineMiddleY = -56;
        public double _3splineMiddleX = -8;
        public double _3splineMiddleRot = 0;

        public double _3splineBoardY = -24;
        public double _3splineBoardX = 72;
        public double _3splineBoardRot = 0;

        public double _4strafeBoardFrontY = 0;
        public double _4strafeBoardFrontX = 13;

        public double _4strafeBoardRightY = 8;
        public double _4strafeBoardRightX = 13;

        public double _4strafeBoardLeftY = 8;
        public double _4strafeBoardLeftX = 13;

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



        Pose2d nearBoardPos = new Pose2d(drive.pose.position.plus(new Vector2d(params._3splineBoardX, params._3splineBoardY)), params._3splineBoardRot);

        Action moveToBoard = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(-4, 0)))
                .splineToLinearHeading(new Pose2d(drive.pose.position.plus(new Vector2d(params._3splineMiddleX, params._3splineMiddleY)), 0), params._3splineMiddleRot, drive.defaultVelConstraint, drive.defaultAccelConstraint)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._3splineMiddleX + 42, params._3splineMiddleY)), drive.defaultVelConstraint, drive.defaultAccelConstraint)
                .splineToLinearHeading(new Pose2d(nearBoardPos.position, 0), params._3splineBoardRot, drive.defaultVelConstraint, drive.defaultAccelConstraint)
                .build();

        Action moveToLeftBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardLeftX, params._4strafeBoardLeftY)))
                .build();
        Action moveToCenterBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardFrontX, params._4strafeBoardFrontY)))
                .build();
        Action moveToRightBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardRightX, params._4strafeBoardRightY)))
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
        sleep(400);
        claw.controlRotation(CLAW_ROTATION.HIDDEN);
        sleep(400);
        claw.closeLeft();
        Actions.runBlocking(moveToBoard);
        sleep(1000);
        switch (pixelPos){
            case 0:
                Actions.runBlocking(moveToLeftBoard);
                break;
            case 1:
                Actions.runBlocking(moveToCenterBoard);
                break;
            case 2:
                Actions.runBlocking(moveToRightBoard);
                break;
        }


        claw.controlRotation(CLAW_ROTATION.BOARD);
        sleep(700);
        arm.setPosition(ARM_POSITIONS.BOARD, true);
        sleep(400);
        claw.openRight();

        Action returnToBoard = drive.actionBuilder(drive.pose)
                .strafeTo(nearBoardPos.position)
                .build();

        Actions.runBlocking(returnToBoard);
        sleep(10000);
        //go for white
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
