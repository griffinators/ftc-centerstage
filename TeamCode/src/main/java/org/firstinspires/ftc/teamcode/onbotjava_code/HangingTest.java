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

@TeleOp(name="Hanging Test", group="Robot")
public class HangingTest extends LinearOpMode  {
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
		
		armExtendLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armExtendRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armControlLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armControlRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		
 
		while (opModeIsActive()) {
			// Arm Movement
			
	
			
			if (gamepad2.left_bumper) {
				armExtendLeft.setPower(-1f);
				armExtendRight.setPower(-1f);
			} else {
				if (gamepad2.y) {
				armExtendLeft.setPower(0.6f);
				armExtendRight.setPower(0.6f);
				} else if (gamepad2.a) {
				armExtendLeft.setPower(-0.6f);
				armExtendRight.setPower(-0.6f);
				} else {
				armExtendLeft.setPower(0f);
				armExtendRight.setPower(0f);
				}
			}
			
			if (gamepad2.right_bumper) {
				armControlLeft.setPower(-1f);
				armControlRight.setPower(-1f);
			} else {
				if (gamepad2.x) {
				armControlLeft.setPower(0.6f);
				armControlRight.setPower(0.6f);
				} else if (gamepad2.b) {
				armControlLeft.setPower(-0.6f);
				armControlRight.setPower(-0.6f);
				} else {
				armControlLeft.setPower(0f);
				armControlRight.setPower(0f);
				}
			}

			telemetry.addData("Position", "Extension: " + armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition()
			+ "; Rotation: " + armControlLeft.getCurrentPosition() + " " + armControlRight.getCurrentPosition());
			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.update();
		}
	}
}