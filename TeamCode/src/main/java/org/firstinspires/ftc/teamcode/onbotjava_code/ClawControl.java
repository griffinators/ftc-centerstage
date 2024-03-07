package org.firstinspires.ftc.teamcode.onbotjava_code;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@TeleOp(name = "Servo Test GRiFFTY", group = "Robot")

public class ClawControl extends LinearOpMode {

	// Define class members
	Servo servo;


	@Override
	public void runOpMode() {

		servo = hardwareMap.get(Servo.class, "c");

		// Wait for the start button
		telemetry.addData(">", "Press Start to start." );
		telemetry.update();
		waitForStart();
		while(opModeIsActive()) {
			servo.setPosition(-0.3);
		}
	}
}
