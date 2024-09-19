package org.firstinspires.ftc.teamcode.griffinators;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Localizer;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.ThreeDeadWheelLocalizer;

@TeleOp(name="TeleOperation", group="Robot")

public class TeleOperation extends LinearOpMode {
	
	private final ElapsedTime runtime = new ElapsedTime();
	DcMotor frontLeft, frontRight, backLeft, backRight, armExtendLeft, armExtendRight, armControlLeft, armControlRight;
	Servo clawControl, clawLeft, clawRight, launch, launchAngle;

	private Pose2d pose = new Pose2d(0,0,0);
	@Override
	public void runOpMode() throws InterruptedException{
		
		telemetry.addData("Status", "Initialized");
		telemetry.update();

		frontLeft = hardwareMap.dcMotor.get("leftFront");
		frontRight = hardwareMap.dcMotor.get("rightFront");
		backLeft = hardwareMap.dcMotor.get("leftBack");
		backRight = hardwareMap.dcMotor.get("rightBack");
		armExtendLeft = hardwareMap.dcMotor.get("acl");
		armExtendRight = hardwareMap.dcMotor.get("acr");
		armControlLeft = hardwareMap.dcMotor.get("ael");
		armControlRight = hardwareMap.dcMotor.get("aer");
		clawControl = hardwareMap.servo.get("c");
		clawLeft = hardwareMap.servo.get("cl");
		clawRight = hardwareMap.servo.get("cr");
		launch = hardwareMap.servo.get("l");
		launchAngle = hardwareMap.servo.get("lc");

		armControlLeft.setDirection(DcMotor.Direction.REVERSE);
		armExtendLeft.setDirection(DcMotor.Direction.REVERSE);
		frontLeft.setDirection(DcMotor.Direction.REVERSE);
		backLeft.setDirection(DcMotor.Direction.REVERSE);

		waitForStart();
		runtime.reset();
		runtime.startTime();
		float basicPower = 0.4f;
		ElapsedTime time1 = new ElapsedTime();
		ElapsedTime time2 = new ElapsedTime();
		Localizer localizer = new ThreeDeadWheelLocalizer(hardwareMap, MecanumDrive.PARAMS.inPerTick);

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
		
		boolean Lopen = false;
		boolean LclawChangeable = true;
		boolean Ropen = false;
		boolean RclawChangeable = true;
		boolean Cground = false;
		boolean CgroundChangeable = true;
		boolean Aground = true;

		while (opModeIsActive())
		{

		////////	MOVEMENT	///////		

		pose = pose.plus(localizer.update().value());
		if (gamepad1.right_bumper) {
			pose = new Pose2d(0,0,-Math.PI/2);
		}
		if (gamepad1.left_bumper) {
			pose = new Pose2d(0,0,Math.PI/2);
		}

		double x = responseCurve(gamepad1.left_stick_x,	3,0.9);
		double y = responseCurve(-gamepad1.left_stick_y, 3, 0.9);
//		double turn = responseCurve(gamepad1.right_stick_x, 5, 0.95);
//		if (Math.hypot(-gamepad1.left_stick_y,-gamepad1.left_stick_x) >= 0.95) {
//			x = -gamepad1.left_stick_y - gamepad2.left_stick_y / 5;
//			y = -gamepad1.left_stick_x - gamepad2.left_stick_x / 5;
//		} else {
//			x = -gamepad1.left_stick_y/2 - gamepad2.left_stick_y / 5;
//			y = -gamepad1.left_stick_x/2 - gamepad2.left_stick_x / 5;
//		}
		double theta = Math.atan2(y, x);
		double power = Math.hypot(x, y);
		double turn;
		if (Math.abs(gamepad1.right_stick_x) >= 0.95) {
			turn = gamepad1.right_stick_x/2 + gamepad2.right_stick_x/4;
		} else {
			turn = gamepad1.right_stick_x/4 + gamepad2.right_stick_x/4;
		}
		double sin = Math.sin(theta - Math.PI/4 - pose.heading.log());
		double cos = Math.cos(theta - Math.PI/4 - pose.heading.log());
		double max = Math.max(Math.abs(sin), Math.abs(cos));
			
		if (gamepad1.dpad_left) {
			time1.reset();
		} else if (gamepad1.dpad_right) {
			time1.reset();
		}

		if (time1.seconds() < 0.6 && runtime.seconds() > 2) {
			turn = 1;
		}

		double FLpower = power * cos/max + turn;
		double FRpower = power * sin/max - turn;
		double BLpower = power * sin/max + turn;
		double BRpower = power * cos/max - turn;

		if((power + Math.abs(turn)) > 1) {
			FLpower /= power + turn;
			FRpower /= power + turn;
			BLpower /= power + turn;
			BRpower /= power + turn;
		}

		frontLeft.setPower(FLpower);
		frontRight.setPower(FRpower);
		backLeft.setPower(BLpower);
		backRight.setPower(BRpower);





		///////		CLAW	///////

		double openPos = 0.12;
		double closedPos = 0.32;
		// double closedPos = 0.36; // too tight
		double leftMax = 0.89;
		
		if (gamepad2.left_bumper) {
			if (LclawChangeable) {
				LclawChangeable = false;
				Lopen = !Lopen;
			}
		} else {
			LclawChangeable = true;
		}
		if (gamepad2.right_bumper) {
			if (RclawChangeable) {
				RclawChangeable = false;
				Ropen = !Ropen;
			}
		} else {
			RclawChangeable = true;
		}
		
		if (Ropen) {
			clawRight.setPosition(closedPos);
		} else {
			clawRight.setPosition(openPos);
		}
		if (Lopen) {
			clawLeft.setPosition(leftMax - closedPos);
		} else {
			clawLeft.setPosition(leftMax - openPos);
		}
		
		if (Aground) {	
			if (gamepad2.a){
				if(CgroundChangeable){
					Cground = !Cground;
					CgroundChangeable = false;
				}
			} else {
				CgroundChangeable = true;
			}
	   
			if (Cground) {
				clawControl.setPosition(0.472);
			} else {
				clawControl.setPosition(0.472 + 0.04);
			}	
		} else if (gamepad2.a) {
			clawControl.setPosition(0.465);
		}





		///////		ARM		///////
		
		armControlLeft.setPower(basicPower);
		armControlRight.setPower(basicPower);
		armExtendLeft.setPower(basicPower);
		armExtendRight.setPower(basicPower);
		
		if (gamepad2.dpad_left) {
			time2.reset();
			setExtension(0);
			sleep(200);
			setRotation(50);
			sleep(200);
			setRotation(0);
			Aground = true;
			Cground = false;
		} else if (gamepad2.dpad_up) {
			setRotation(170);
			setExtension(200);
			clawControl.setPosition(0.54);
			Aground = false;
			Cground = false;
		} else if (gamepad2.dpad_right) {
			setRotation(230);
			setExtension(400);
			clawControl.setPosition(0.53);
			Aground = false;
			Cground = false;
		} else if (gamepad2.dpad_down) {
			setRotation(280);
			setExtension(500);
			clawControl.setPosition(0.52);
			Aground = false;
			Cground = false;
		} else if (gamepad2.x) {
		 	setRotation(320);
		 	setExtension(600);
		 	clawControl.setPosition(0.52);
		 	Aground = false;
		 	Cground = false;
		} else if (gamepad2.y) {
			setRotation(360);
			setExtension(700);
			clawControl.setPosition(0.52);
			Aground = false;
			Cground = false;
		} else if (gamepad2.b) {
			setRotation(390);
			setExtension(800);
			clawControl.setPosition(0.52);
			Aground = false;
			Cground = false;
		} else if (gamepad1.b) {
			setRotation(800);
			setExtension(400);
			Aground = false;
			Cground = false;
		} else if (gamepad1.y) {
			setExtension(0);
			setRotation(400);
			Aground = false;
			Cground = false;
		}



		
		
		///////		LAUNCH		///////

		if (gamepad1.x) {
		launchAngle.setPosition(40/180f);
		sleep(1000);
		launch.setPosition(0.5f);
		sleep(1000);
		launch.setPosition(-0.5f);
		sleep(500);
		launchAngle.setPosition(60/180f);
		}





		///////		DATA		///////

		telemetry.addData("RunTime", "time:" + runtime);
		//telemetry.addData("Booleans", "ground: " + ground);	 
		telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
		+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition() + "; rot: " + " " + pose.heading.log());
		telemetry.addData("Gamepad 1", "gamepad1_l_x: " + gamepad1.left_stick_x + "; gamepad1_l_y: " + gamepad1.left_stick_y + "gamepad1_r_x" + gamepad1.right_stick_x + "gamepad1_Y: " + gamepad1.y);
		telemetry.addData("Gamepad 2", "gamepad2_l_x: " + gamepad2.left_stick_x + "; gamepad2_l_y: " + gamepad2.left_stick_y + "gamepad2_r_x" + gamepad2.right_stick_x + "gamepad2_Y: " + gamepad2.y);
		telemetry.update();
		}
	}
	
	
	
	
	
	
	private void setExtension(int amount){
		armExtendLeft.setTargetPosition(amount);
		armExtendRight.setTargetPosition(amount);
	}
	
	private void setRotation(int amount){
		armControlLeft.setTargetPosition(amount);
		armControlRight.setTargetPosition(amount);
	}

	private double responseCurve (double raw, int curvePower, double maxThreshold) {
		return Math.copySign(Math.min(Math.pow(Math.abs(raw)/maxThreshold, curvePower), 1), raw);
	}
}