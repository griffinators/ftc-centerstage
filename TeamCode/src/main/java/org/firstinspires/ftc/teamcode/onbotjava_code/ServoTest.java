import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Disabled
@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp

public class ServoTest extends LinearOpMode {
	
	Servo servo;
	

	@Override
	public void runOpMode() {

		telemetry.addData("Status", "Initialized");
		telemetry.update();
		
		waitForStart();
		
		servo = hardwareMap.servo.get("servo");

		while (opModeIsActive()) {
			telemetry.addData("Status", "Running");
			telemetry.update();
			
			servo.setPosition(1);
			

		}
	}
}
