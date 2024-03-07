import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="TeleOp Ken", group="Robot")

public class TeleOp_Ken extends LinearOpMode {
	DcMotor frontLeft;
	DcMotor frontRight;
	DcMotor rearLeft;
	DcMotor rearRight;

	DcMotorEx armExtendLeft;
	DcMotorEx armExtendRight;
	DcMotorEx armControlLeft;
	DcMotorEx armControlRight;
	
	Servo clawControl;
	Servo clawLeft;
	Servo clawRight;
	Servo launch;
	Servo launchAngle;

	private ElapsedTime runtime = new ElapsedTime();
	
	final int MAX_ARM_ROTATION = 1000;
	final int MAX_ARM_EXTENTION = 9999;

	@Override
	public void runOpMode() {
		telemetry.addData("Status", "Initialized");
		telemetry.update();
		
		frontLeft = hardwareMap.get(DcMotor.class, "br");
		frontRight = hardwareMap.get(DcMotor.class, "bl");
		rearLeft = hardwareMap.get(DcMotor.class, "fr");
		rearRight = hardwareMap.get(DcMotor.class, "fl");

		armExtendLeft = hardwareMap.get(DcMotorEx.class, "acr");
		armExtendRight = hardwareMap.get(DcMotorEx.class, "acl");
		armControlLeft = hardwareMap.get(DcMotorEx.class, "aer");
		armControlRight = hardwareMap.get(DcMotorEx.class, "ael");
		
		clawControl = hardwareMap.get(Servo.class, "c");
		clawLeft = hardwareMap.get(Servo.class, "cr");
		clawRight = hardwareMap.get(Servo.class, "cl");
		launch = hardwareMap.get(Servo.class, "l");
		launchAngle = hardwareMap.get(Servo.class, "lc");

		armControlLeft.setDirection(DcMotorEx.Direction.REVERSE);
		armExtendLeft.setDirection(DcMotorEx.Direction.REVERSE);

		waitForStart();
		runtime.reset();
		float basicPower = 0.4f;
		
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
		
		boolean ground = true;
		boolean groundChangeable = true;
		boolean suspend = false;
		boolean suspendChangeable = true;
		
		while (opModeIsActive()) {
			if (gamepad2.b){
				if(groundChangeable){
					ground = !ground;
					groundChangeable = false;
				}
			} else {
				groundChangeable = true;
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
			
			drive();	// Chassis movement
			clawGrab();	 // Operates the claw's grabbing
			// clawArmAngle(0.37, ground);
			clawArmAngle(0.54, ground);
			
			if (suspend) {
				armExtendLeft.setPower(1f);
				armExtendRight.setPower(1f);
				armControlLeft.setPower(1f);
				armControlRight.setPower(1f);
				armExtendLeft.setTargetPosition(0);
				armExtendRight.setTargetPosition(0);
				armControlLeft.setTargetPosition(0);
				armControlRight.setTargetPosition(0);
				armExtendLeft.setPower(1f);
				armExtendRight.setPower(1f);
				armControlLeft.setPower(1f);
				armControlRight.setPower(1f);
			} else {
				// Arm Movement
				if (gamepad2.y && armExtendRight.getCurrentPosition() < MAX_ARM_ROTATION) {
					armControlLeft.setTargetPosition(MAX_ARM_ROTATION);
					armControlRight.setTargetPosition(MAX_ARM_ROTATION);
					lastACLPos = armControlLeft.getCurrentPosition();
					lastACRPos = armControlRight.getCurrentPosition();
				} else if (gamepad2.a && armExtendRight.getCurrentPosition() > 0) {
					armControlLeft.setTargetPosition(0);
					armControlRight.setTargetPosition(0);
					lastACLPos = armControlLeft.getCurrentPosition();
					lastACRPos = armControlRight.getCurrentPosition();
				} else {
					armControlLeft.setTargetPosition(lastACLPos);
					armControlRight.setTargetPosition(lastACRPos);
				}
				armControlLeft.setPower(basicPower);
				armControlRight.setPower(basicPower);
				
				// Arm Extension
				if (gamepad2.dpad_up && armExtendRight.getCurrentPosition() < MAX_ARM_EXTENTION) {
					armExtendLeft.setTargetPosition(MAX_ARM_EXTENTION);
					armExtendRight.setTargetPosition(MAX_ARM_EXTENTION);
					lastAELPos = armExtendLeft.getCurrentPosition();
					lastAERPos = armExtendRight.getCurrentPosition();
				} else if (gamepad2.dpad_down && armExtendRight.getCurrentPosition() > 0) {
					armExtendLeft.setTargetPosition(0);
					armExtendRight.setTargetPosition(0);
					lastAELPos = armExtendLeft.getCurrentPosition();
					lastAERPos = armExtendRight.getCurrentPosition();
				} else {
					armExtendLeft.setTargetPosition(lastAELPos);
					armExtendRight.setTargetPosition(lastAERPos);
				}
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
			}
			
			telemetry.addData("Booleans", "ground = " + ground + ", suspend = " + suspend);	 
			telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
			+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.addData("Runtime", runtime.seconds() + "s");	 
			telemetry.update();
		}
	}
	
	private void drive() {  // Wraps the chassis movement in a method
		double x = gamepad2.left_stick_x;
		double y = -gamepad2.left_stick_y;
		double turn = gamepad2.right_stick_x/2;
		
		double theta = Math.atan2(y, x);
		double power = Math.hypot(x, y);
			
		double sin = Math.sin(theta - Math.PI / 4);
		double cos = Math.cos(theta - Math.PI / 4);
		double max = Math.max(Math.abs(sin), Math.abs(cos));
			
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
	}
	
	private void clawGrab() {   // Wraps the claw's grabbing in a method
		double openPos = 0.2;
		double closedPos = 0.34;
		// double closedPos = 0.36; // too tight
		double leftMax = 0.973;
		if (gamepad2.right_trigger > 0.5) {
			clawRight.setPosition(openPos);
		} else if (gamepad2.right_bumper) {
			clawRight.setPosition(closedPos);
		} 
		if (gamepad2.left_trigger > 0.5) {
			clawLeft.setPosition(leftMax-openPos);			   
		} else if (gamepad2.left_bumper) {
			clawLeft.setPosition(leftMax-closedPos);
		}
	}
	
	private void clawArmAngle(double initClawAngle, boolean ground) {
		if (ground) {
			clawControl.setPosition(initClawAngle); // claw pos when on ground
		} else {
			clawControl.setPosition(initClawAngle + 0.05);
			// clawControl.setPosition(initClawAngle - 0.21 + ((double)(armControlLeft.getCurrentPosition()+armControlRight.getCurrentPosition()))*9/40000); // claw pos when in air (0.2 because 60 degrees)
		}
	}
	
	private void launchSequence() {
		launchAngle.setPosition(37/180f);
		sleep(1000);
		launch.setPosition(0.5f);
		sleep(1000);
		launch.setPosition(-0.5f);
	}
}