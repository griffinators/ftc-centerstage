package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "Position Debug", group = "Auto")
public class PositionTest extends LinearOpMode {
    private Pose2d pose = new Pose2d(0,0,0);
    @Override
    public void runOpMode() throws InterruptedException {
        Localizer localizer = new ThreeDeadWheelLocalizer(hardwareMap, MecanumDrive.PARAMS.inPerTick);
        waitForStart();
        while (opModeIsActive()){
            pose = pose.plus(localizer.update().value());
            telemetry.addData("Position", "x: " + pose.position.x + ";  y: " + pose.position.y + "  rot: " + pose.heading.log());
            telemetry.update();
            // to get rotation u need to use pose.heading.log()
        }
    }
}
