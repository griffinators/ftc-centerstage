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

@TeleOp(name = "Launcher Test", group = "Robot")

public class LauncherTest extends LinearOpMode {

	// Define class members
	Servo   servo;
	Servo   angleControl;


	@Override
	public void runOpMode() {

		servo = hardwareMap.get(Servo.class, "l");
		angleControl = hardwareMap.get(Servo.class, "lc");
		// Wait for the start button
		telemetry.addData(">", "Press Start to start." );
		telemetry.update();
		waitForStart();
		// angleControl.setPosition(5/180f);

		// Scan servo till stop pressed.
		while(opModeIsActive()){
			if(gamepad2.y){
				servo.setPosition(0.5); // -1 to 1
				try{
					Thread.sleep(1000);
				}catch(Exception e){
					//_
				}
				servo.setPosition(-0.5);
			}
			if(gamepad2.a){
				angleControl.setPosition(37/180f);
			}
			telemetry.addData("Input: ", gamepad2.x);
		}
	}
}
