package org.firstinspires.ftc.teamcode.onbotjava_code;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="DCMotorTest")

public class DCMotorTest extends LinearOpMode{
	DcMotorEx armExtendLeft;
	DcMotorEx armExtendRight;
	DcMotorEx armControlLeft;
	DcMotorEx armControlRight;
		
	private ElapsedTime runtime = new ElapsedTime();
	
	@Override
	public void runOpMode() {
		telemetry.addData("Status", "Initialized");
		telemetry.update();
		armExtendLeft = hardwareMap.get(DcMotorEx.class, "acr");
		armExtendRight = hardwareMap.get(DcMotorEx.class, "acl");
		armControlLeft = hardwareMap.get(DcMotorEx.class, "aer");
		armControlRight = hardwareMap.get(DcMotorEx.class, "ael");
		armControlLeft.setDirection(DcMotorEx.Direction.REVERSE);
		armExtendLeft.setDirection(DcMotorEx.Direction.REVERSE);
		waitForStart();
		armExtendLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
		armExtendRight.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
		armControlLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
		armControlRight.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
		armExtendLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
		armExtendRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
		armControlLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
		armControlRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
		runtime.reset();
		
		double basicPower = 1;
		
		while (opModeIsActive()) {
			armControlLeft.setPower(0.0);
			armControlRight.setPower(0.0);
			if (gamepad2.y){
				armControlLeft.setPower(0.8);
				armControlRight.setPower(0.8);
			}
			if (gamepad2.a){
				armControlLeft.setPower(-0.8);
				armControlRight.setPower(-0.8);
			}
		
			armExtendLeft.setPower(0.0);
			armExtendRight.setPower(0.0);
			if (gamepad2.x){
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
			}
			if (gamepad2.b){
				armExtendLeft.setPower(-basicPower);
				armExtendRight.setPower(-basicPower);
			}
			
			telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
			+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
		}
	}
	
}