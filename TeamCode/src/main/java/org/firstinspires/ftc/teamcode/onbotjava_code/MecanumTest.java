import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@Disabled
@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name="MecanumTest", group="Robot")
public class MecanumTest extends LinearOpMode  {
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


		waitForStart();
		runtime.reset();
		
		while (opModeIsActive()) {

			double x = gamepad2.left_stick_x;
			double y = -gamepad2.left_stick_y;
			double turn = gamepad2.right_stick_x/3;

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

			frontLeft.setPower(-FLpower);
			frontRight.setPower(FRpower);
			rearLeft.setPower(-RLpower);
			rearRight.setPower(RRpower);

			telemetry.addData("Status", "Run Time: " + runtime.toString());

			telemetry.addData("Input", "gamepad_l_x: " + gamepad2.left_stick_x + "; gamepad_l_y: " + gamepad2.left_stick_y + "gamepad_r_x"
			+ gamepad2.right_stick_x + "gamepad_Y: " + gamepad2.y);
			telemetry.update();
		}
	}
}
