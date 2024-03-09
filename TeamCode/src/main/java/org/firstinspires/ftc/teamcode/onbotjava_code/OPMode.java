import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Disabled
@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="OPMode", group="Robot")
public class OPMode extends LinearOpMode  {
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

	private final int MAX_ARM_EXTENTION = 9999;
	private final int MAX_ARM_ROTATION = 1000;
	
	@Override
	public void runOpMode() {
		telemetry.addData("Status", "Initialized");
		telemetry.update();

		frontLeft = hardwareMap.dcMotor.get("fl");
		frontRight = hardwareMap.dcMotor.get("fr");
		rearLeft = hardwareMap.dcMotor.get("bl");
		rearRight = hardwareMap.dcMotor.get("br");

		armExtendLeft = hardwareMap.dcMotor.get("acl");
		armExtendRight = hardwareMap.dcMotor.get("acr");
		armControlLeft = hardwareMap.dcMotor.get("ael");
		armControlRight = hardwareMap.dcMotor.get("aer");
		
		clawControl = hardwareMap.servo.get("c");
		clawLeft = hardwareMap.servo.get("cl");
		clawRight = hardwareMap.servo.get("cr");
		launch = hardwareMap.servo.get("l");

		armControlRight.setDirection(DcMotor.Direction.REVERSE);
		armExtendRight.setDirection(DcMotor.Direction.REVERSE);

		waitForStart();
		runtime.reset();
		runtime.startTime();
		float basicPower = 0.3f;
		
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
		//armControlLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		//armControlRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		
		boolean teamRed = true;
		boolean up = false;
		boolean down = false;
		
		int lastRPosL = armControlLeft.getCurrentPosition();
		int lastRPosR = armControlRight.getCurrentPosition();
		
		int lastEPosL = armControlLeft.getCurrentPosition();
		int lastEPosR = armControlRight.getCurrentPosition();
		
		while (opModeIsActive()) {
			// Chassis movement
			// double x = teamRed ? gamepad2.left_stick_y : -gamepad2.left_stick_y;
			// double y = teamRed ? gamepad2.left_stick_x : -gamepad2.left_stick_x;
			double x = gamepad2.left_stick_x;
			double y = -gamepad2.left_stick_y;
			double turn = gamepad2.right_stick_x/3;
			
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

			// Arm Movement
			
			if (gamepad2.y) {
				up = true;
			} else if (gamepad2.a) {
				up = false;
			}

			if (up) {
				// armControlLeft.setTargetPosition(MAX_ARM_ROTATION);
				// armControlRight.setTargetPosition(MAX_ARM_ROTATION);
				//armControlLeft.setTargetPosition(200);
				//armControlRight.setTargetPosition(200);
				// armControlLeft.setPower(basicPower);
				// armControlRight.setPower(basicPower);
				// clawControl.setPosition(armControlLeft.g;
			} else {
				//armControlLeft.setTargetPosition(0);
				//armControlRight.setTargetPosition(0); 
				clawControl.setPosition(0.375+((double)(armControlLeft.getCurrentPosition()+armControlRight.getCurrentPosition()))/4000); // claw pos when on ground
			}
			armControlLeft.setPower(basicPower);
			armControlRight.setPower(basicPower);
			
			// Arm Extension
			
			if (gamepad2.x && armExtendRight.getCurrentPosition() < MAX_ARM_EXTENTION) {
				armExtendLeft.setTargetPosition(MAX_ARM_EXTENTION);
				armExtendRight.setTargetPosition(MAX_ARM_EXTENTION);
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
				lastEPosL = armExtendLeft.getCurrentPosition();
				lastEPosR = armExtendRight.getCurrentPosition();
			} else if (gamepad2.b && armExtendRight.getCurrentPosition() > 0) {
				armExtendLeft.setTargetPosition(0);
				armExtendRight.setTargetPosition(0);
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
				lastEPosL = armExtendLeft.getCurrentPosition();
				lastEPosR = armExtendRight.getCurrentPosition();
			} else {
				armExtendLeft.setTargetPosition(lastEPosL);
				armExtendRight.setTargetPosition(lastEPosR);
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
			}

			/* claw */
			// double openPos = 0.2;
			// double closedPos = 0.33;
			// double medPos = 0.308;
			// double rightMax = 0.97;
			double openPos = 0.2;
			double closedPos = 0.34;
			// double closedPos = 0.36; // too tight
			double rightMax = 0.973;
			if (gamepad2.left_trigger > 0.5) {
				clawLeft.setPosition(openPos);
			} else if (gamepad2.left_bumper) {
				clawLeft.setPosition(closedPos);
			} 
			if (gamepad2.right_trigger > 0.5) {
				clawRight.setPosition(rightMax-openPos);			   
			} else if (gamepad2.right_bumper) {
				clawRight.setPosition(rightMax-closedPos);
			} 
			
				
			
			if (gamepad2.dpad_up) {
				launch.setPosition(1);
			}
				 
			telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
			+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.update();
		}
	}
	private float getNextRotationSpeed(int currentPostition){
		return 1f;
	}
}