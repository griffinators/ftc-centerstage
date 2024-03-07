import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="TeleOp Bob", group="Robot")

public class TeleOp_Bob extends LinearOpMode {
private ElapsedTime runtime = new ElapsedTime();
	DcMotor frontLeft;
	DcMotor frontRight;
	DcMotor rearLeft;
	DcMotor rearRight;

	DcMotor armExtendLeft;
	DcMotor armExtendRight;
	DcMotor armControlLeft;
	DcMotor armControlRight;
	
	Servo clawControl;
	Servo clawLeft;
	Servo clawRight;
	Servo launch;
	Servo launchAngle;

	private final int MAX_ARM_EXTENTION = 9999;
	private final int MAX_ARM_ROTATION = 1000;
	
	@Override
	public void runOpMode() {
		telemetry.addData("Status", "Initialized");
		telemetry.update();

		frontLeft = hardwareMap.dcMotor.get("br");
		frontRight = hardwareMap.dcMotor.get("bl");
		rearLeft = hardwareMap.dcMotor.get("fr");
		rearRight = hardwareMap.dcMotor.get("fl");

		armExtendLeft = hardwareMap.dcMotor.get("acr");
		armExtendRight = hardwareMap.dcMotor.get("acl");
		armControlLeft = hardwareMap.dcMotor.get("aer");
		armControlRight = hardwareMap.dcMotor.get("ael");
		
		clawControl = hardwareMap.servo.get("c");
		clawLeft = hardwareMap.servo.get("cr");
		clawRight = hardwareMap.servo.get("cl");
		launch = hardwareMap.servo.get("l");
		launchAngle = hardwareMap.servo.get("lc");

		armControlLeft.setDirection(DcMotor.Direction.REVERSE);
		armExtendLeft.setDirection(DcMotor.Direction.REVERSE);

		

		waitForStart();
		runtime.reset();
		runtime.startTime();
		float basicPower = 0.4f;
		ElapsedTime startTime1 = new ElapsedTime();
		ElapsedTime startTime2 = new ElapsedTime();

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
		
		int lastACLPos = armControlLeft.getCurrentPosition();
		int lastACRPos = armControlRight.getCurrentPosition();
		
		int lastAELPos = armExtendLeft.getCurrentPosition();
		int lastAERPos = armExtendRight.getCurrentPosition();
		
		boolean Lopen = true;
		boolean LclawChangeable = true;
		boolean Ropen = true;
		boolean RclawChangeable = true;
		boolean Cground = true;
		boolean CgroundChangeable = true;
		boolean Aground = true;
		boolean AgroundChangeable = true;
		boolean suspend = false;
		boolean suspendChangeable = true;
		
		boolean teamBlue = false;
		
		while (opModeIsActive()) {
			
		double x = teamBlue ? gamepad1.left_stick_y : -gamepad1.left_stick_y;
		double y = teamBlue ? gamepad1.left_stick_x : -gamepad1.left_stick_x;
			
		double turn = gamepad1.right_stick_x/2;
		
		double theta = Math.atan2(y, x);
		double power = Math.hypot(x, y);
			
		double sin = Math.sin(theta - Math.PI / 4);
		double cos = Math.cos(theta - Math.PI / 4);
		double max = Math.max(Math.abs(sin), Math.abs(cos));
			
		if (gamepad1.dpad_left) {
			startTime1.reset();
			teamBlue = false;
		} else if (gamepad1.dpad_right) {
			startTime1.reset();
			teamBlue = true;
		}
		
		if (startTime1.seconds() < 0.6 && runtime.seconds() > 2) {
			turn = 1;
		}

		double FLpower = power * cos/max + turn;
		double FRpower = power * sin/max - turn;
		double RLpower = power * sin/max + turn;
		double RRpower = power * cos/max - turn;

		if((power + Math.abs(turn)) > 1) {
			FLpower /= power + turn;
			FRpower /= power + turn;
			RLpower /= power + turn;
			RRpower /= power + turn;
		}

		
		frontLeft.setPower(-FLpower);
		frontRight.setPower(FRpower);
		rearLeft.setPower(-RLpower);
		rearRight.setPower(RRpower);		   

		double openPos = 0.2;
		double closedPos = 0.34;
		// double closedPos = 0.36; // too tight
		double leftMax = 0.95;
		
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
		
		
		if (gamepad2.b){
			if(CgroundChangeable){
				Cground = !Cground;
				CgroundChangeable = false;
			}
		} else {
			CgroundChangeable = true;
		}
			
		if (gamepad2.x){
			if(AgroundChangeable){
				Aground = !Aground;
				AgroundChangeable = false;
			}
		} else {
			AgroundChangeable = true;
		}
		
		if (gamepad2.back){
			if(suspendChangeable){
				suspend = !suspend;
				suspendChangeable = false;
			}
		} else {
			suspendChangeable = true;
		}
	
	
		
		if (gamepad2.start) {
			launchSequence();
		}
	   
		clawAngle(0.54, Cground);
		// armAngle(Aground);

		if (suspend) {
			armExtendLeft.setPower(1f);
			armExtendRight.setPower(1f);
			armControlLeft.setPower(1f);
			armControlRight.setPower(1f);
			armExtendLeft.setTargetPosition(-100);
			armExtendRight.setTargetPosition(-100);
			armControlLeft.setTargetPosition(-10);
			armControlRight.setTargetPosition(-10);
			armExtendLeft.setPower(1f);
			armExtendRight.setPower(1f);
			armControlLeft.setPower(1f);
			armControlRight.setPower(1f);
		} else {
			// Arm Movement
			// if (gamepad2.y && armExtendRight.getCurrentPosition() < MAX_ARM_ROTATION) {
			// 	armControlLeft.setTargetPosition(MAX_ARM_ROTATION);
			// 	armControlRight.setTargetPosition(MAX_ARM_ROTATION);
			// 	lastACLPos = armControlLeft.getCurrentPosition();
			// 	lastACRPos = armControlRight.getCurrentPosition();
			// } else if (gamepad2.a && armExtendRight.getCurrentPosition() > 0) {
			// 	armControlLeft.setTargetPosition(0);
			// 	armControlRight.setTargetPosition(0);
			// 	lastACLPos = armControlLeft.getCurrentPosition();
			// 	lastACRPos = armControlRight.getCurrentPosition();
			// } else {
			// 	armControlLeft.setTargetPosition(lastACLPos);
			// 	armControlRight.setTargetPosition(lastACRPos);
			// }
			armControlLeft.setPower(basicPower);
			armControlRight.setPower(basicPower);
			
			// Arm Extension
			// if (gamepad2.dpad_up && armExtendRight.getCurrentPosition() < MAX_ARM_EXTENTION) {
			// 	armExtendLeft.setTargetPosition(MAX_ARM_EXTENTION);
			// 	armExtendRight.setTargetPosition(MAX_ARM_EXTENTION);
			// 	lastAELPos = armExtendLeft.getCurrentPosition();
			// 	lastAERPos = armExtendRight.getCurrentPosition();
			// } else if (gamepad2.dpad_down && armExtendRight.getCurrentPosition() > 0) {
			// 	armExtendLeft.setTargetPosition(0);
			// 	armExtendRight.setTargetPosition(0);
			// 	lastAELPos = armExtendLeft.getCurrentPosition();
			// 	lastAERPos = armExtendRight.getCurrentPosition();
			// } else {
			// 	armExtendLeft.setTargetPosition(lastAELPos);
			// 	armExtendRight.setTargetPosition(lastAERPos);
			// }
			armExtendLeft.setPower(basicPower);
			armExtendRight.setPower(basicPower);
			
			if (gamepad2.dpad_left) {
				startTime2.reset();
				armExtendLeft.setTargetPosition(0);
				armExtendRight.setTargetPosition(0);
			} else if (gamepad2.dpad_up) {
				armControlLeft.setTargetPosition(200);
				armControlRight.setTargetPosition(200);
				armExtendLeft.setTargetPosition(200);
				armExtendRight.setTargetPosition(200);
			} else if (gamepad2.dpad_right) {
				armControlLeft.setTargetPosition(300);
				armControlRight.setTargetPosition(300);
				armExtendLeft.setTargetPosition(300);
				armExtendRight.setTargetPosition(300);
			}
			
			if (startTime2.seconds() < 0.2 && runtime.seconds() > 1) {
				armControlLeft.setTargetPosition(50);
				armControlRight.setTargetPosition(50);
			}
		}
		
		telemetry.addData("RunTime", "time:" + runtime);
		//telemetry.addData("Booleans", "ground: " + ground);	 
		telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
		+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
		telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
		+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
		telemetry.update();
		}
	}
	
	
	private void clawAngle(double initClawAngle, boolean Cground) {
		if (Cground) {
			clawControl.setPosition(initClawAngle); // claw pos when on ground
		} else {
			clawControl.setPosition(initClawAngle + 0.04 );//+ ((double)(armControlLeft.getCurrentPosition()+armControlRight.getCurrentPosition()))*9/40000); // claw pos when in air (0.2 because 60 degrees)
		}
	}
	
	// private void armAngle(boolean Aground) {
	// 	if (Aground) {
	// 		armControlLeft.setTargetPosition(0);
	// 		armControlRight.setTargetPosition(0);
	// 	} else {
	// 		armControlLeft.setTargetPosition(300);
	// 		armControlRight.setTargetPosition(300);
	// 	}
	// }
	
	private void launchSequence() {
		launchAngle.setPosition(37/180f);
		sleep(1000);
		launch.setPosition(0.5f);
		sleep(1000);
		launch.setPosition(-0.5f);
	}
}