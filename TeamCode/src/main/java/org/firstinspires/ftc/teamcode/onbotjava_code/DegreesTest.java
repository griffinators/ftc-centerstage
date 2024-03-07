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

@TeleOp(name = "Degrees Test", group = "Robot")
public class DegreesTest extends LinearOpMode {
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
		
		
		int lastRPosL = armControlLeft.getCurrentPosition();
		int lastRPosR = armControlRight.getCurrentPosition();
		
		int lastEPosL = armControlLeft.getCurrentPosition();
		int lastEPosR = armControlRight.getCurrentPosition();
		
		boolean ground = false;
		
		while (opModeIsActive()) {
			if (ground) {
				clawControl.setPosition(0.375+((double)(armControlLeft.getCurrentPosition()+armControlRight.getCurrentPosition()))/4000/9*10); // claw pos when on ground
			} else {
				
				clawControl.setPosition(0.275+((double)(armControlLeft.getCurrentPosition()+armControlRight.getCurrentPosition()))/4000/9*10); // claw pos when lifted
			}
			
			telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
			+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.update();
		}
	}
}