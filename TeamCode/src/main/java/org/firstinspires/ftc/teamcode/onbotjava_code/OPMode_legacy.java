import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="OPMode Legacy", group="Robot")
public class OPMode_legacy extends LinearOpMode  {
	private ElapsedTime runtime = new ElapsedTime();

	DcMotor frontLeft;
	DcMotor frontRight;
	DcMotor rearLeft;
	DcMotor rearRight;

	DcMotor armExtendLeft;
	DcMotor armExtendRight;
	DcMotor armControlLeft;
	DcMotor armControlRight;

	@Override
	public void runOpMode() {
		telemetry.addData("Status", "Initialized");
		telemetry.update();

		frontLeft = hardwareMap.dcMotor.get("fl");
		frontRight = hardwareMap.dcMotor.get("fr");
		rearLeft = hardwareMap.dcMotor.get("bl");
		rearRight = hardwareMap.dcMotor.get("br");

		armExtendLeft = hardwareMap.dcMotor.get("ael");
		armExtendRight = hardwareMap.dcMotor.get("aer");
		armControlLeft = hardwareMap.dcMotor.get("acl");
		armControlRight = hardwareMap.dcMotor.get("acr");

		armExtendLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armExtendRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armControlLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		armControlRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		
		armControlRight.setDirection(DcMotor.Direction.REVERSE);
		armExtendRight.setDirection(DcMotor.Direction.REVERSE);


		waitForStart();
		runtime.reset();
		
		float armRotation = 0;
		float armExtension = 0;
		float basicPower = 1f;
		
		while (opModeIsActive()) {

			double x = gamepad2.left_stick_x;
			double y = -gamepad2.left_stick_y;
			double turn = gamepad2.right_stick_x;

			double theta = Math.atan2(y, x);

			double power = Math.hypot(x, y);

			double sin = Math.sin(theta -Math.PI/4);
			double cos = Math.cos(theta -Math.PI/4);
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

			frontLeft.setPower(FLpower);
			frontRight.setPower(FRpower);
			rearLeft.setPower(RLpower);
			rearRight.setPower(RRpower);

			telemetry.addData("Status", "Run Time: " + runtime.toString());

			/** Arm Control Part*/
			/** Arm Movement*/
			armControlLeft.setPower(0.0);
			armControlRight.setPower(0.0);
			
			if (gamepad2.y){
				armControlLeft.setPower(basicPower);
				armControlRight.setPower(basicPower);
				armRotation += basicPower;
			}
			if (gamepad2.a){
				armControlLeft.setPower(-1 * basicPower);
				armControlRight.setPower(-1 * basicPower);
				armRotation -= basicPower;
			}
			
			/** Arm Extension*/
			armExtendLeft.setPower(0.0);
			armExtendRight.setPower(0.0);
			
			if (gamepad2.x){
				armExtendLeft.setPower(basicPower);
				armExtendRight.setPower(basicPower);
				armExtension += basicPower;
			}
			if (gamepad2.b){
				armExtendLeft.setPower(-1 * basicPower);
				armExtendRight.setPower(-1 * basicPower);
				armExtension -= basicPower;
			}

			telemetry.addData("Position", "Extension: " + armExtension + "; Rotation: " + armRotation);
			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.update();
		}
	}
}
