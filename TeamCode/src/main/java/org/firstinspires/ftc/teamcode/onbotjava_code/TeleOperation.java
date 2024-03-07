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

@TeleOp(name="TeleOperation", group="Robot")

public class TeleOperation extends LinearOpMode {
	
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
		ElapsedTime time1 = new ElapsedTime();
		ElapsedTime time2 = new ElapsedTime();

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
		boolean Cground = true;
		boolean CgroundChangeable = true;
		boolean Aground = true;
		boolean suspend = false;
		boolean suspendChangeable = true;
		
		boolean teamBlue = false;
		
		while (opModeIsActive()) {




		
		////////	MOVEMENT	///////		
		
		double x = teamBlue ? gamepad1.left_stick_y + gamepad2.left_stick_y / 5: -gamepad1.left_stick_y - gamepad2.left_stick_y / 5;
		double y = teamBlue ? gamepad1.left_stick_x + gamepad2.left_stick_x / 5: -gamepad1.left_stick_x - gamepad2.left_stick_x / 5;
		double turn = gamepad1.right_stick_x/2 + gamepad2.right_stick_x/5;
		double theta = Math.atan2(y, x);
		double power = Math.hypot(x, y);
		double sin = Math.sin(theta - Math.PI / 4);
		double cos = Math.cos(theta - Math.PI / 4);
		double max = Math.max(Math.abs(sin), Math.abs(cos));
			
		if (gamepad1.dpad_left) {
			time1.reset();
			teamBlue = false;
		} else if (gamepad1.dpad_right) {
			time1.reset();
			teamBlue = true;
		}
		
		if (time1.seconds() < 0.6 && runtime.seconds() > 2) {
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





		///////		CLAW	///////

		double openPos = 0.15;
		double closedPos = 0.34;
		// double closedPos = 0.36; // too tight
		double leftMax = 0.92;
		
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
				clawControl.setPosition(0.535);
			} else {
				clawControl.setPosition(0.535 + 0.04);
			}	
		} else if (gamepad2.a) {
			clawControl.setPosition(0.51);
		}





		///////		ARM		///////
		
		armControlLeft.setPower(basicPower);
		armControlRight.setPower(basicPower);
		armExtendLeft.setPower(basicPower);
		armExtendRight.setPower(basicPower);
		
		if (gamepad2.dpad_left) {
			time2.reset();
			setExtention(0);
			sleep(200);
			setRotation(50);
			sleep(200);
			setRotation(00);
			Aground = true;
			Cground = false;
		} else if (gamepad2.dpad_up) {
			setRotation(250);
			setExtention(400);
			clawControl.setPosition(0.58);
			Aground = false;
			Cground = false;
		} else if (gamepad2.dpad_right) {
			setRotation(350);
			setExtention(700);
			clawControl.setPosition(0.56);
			Aground = false;
			Cground = false;
		// } else if (gamepad2.dpad_down) {
		// 	setRotation(350);
		// 	setExtention(700);
		// 	clawControl.setPosition(0.56);
		// 	Aground = false;
		// 	Cground = false;
		} else if (gamepad1.b) {
			setRotation(800);
			setExtention(400);	
			Aground = false;
			Cground = false;
		} else if (gamepad1.y) {
			setExtention(0);
			setRotation(400);
			Aground = false;
			Cground = false;
		}



		
		
		///////		LAUNCH		///////

		if (gamepad1.x) {
		launchAngle.setPosition(43/180f);
		sleep(1000);
		launch.setPosition(0.5f);
		sleep(1000);
		launch.setPosition(-0.5f);
		sleep(500);
		launchAngle.setPosition(15/180f);
		}





		///////		DATA		///////

		telemetry.addData("RunTime", "time:" + runtime);
		//telemetry.addData("Booleans", "ground: " + ground);	 
		telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
		+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
		telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
		+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
		telemetry.update();
		}
	}
	
	
	
	
	
	
	private void setExtention(int amount){
		armExtendLeft.setTargetPosition(amount);
		armExtendRight.setTargetPosition(amount);
	}
	
	private void setRotation(int amount){
		armControlLeft.setTargetPosition(amount);
		armControlRight.setTargetPosition(amount);
	}
}