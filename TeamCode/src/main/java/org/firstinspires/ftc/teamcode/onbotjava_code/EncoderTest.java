/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;

@Disabled
@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

/*
 * This OpMode illustrates the basics of TensorFlow Object Detection,
 * including Java Builder structures for specifying Vision parameters.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
@TeleOp(name = "Encoder TEST", group = "Test")

public class EncoderTest extends LinearOpMode {
	private static final String TFOD_MODEL_FILE = "/sdcard/FIRST/tflitemodels/Griffinators.tflite";
	// Define the labels recognized in the model for TFOD (must be in training order!)
	private static final String[] LABELS = {
	   "B",
	   "R"
	};
	
	DcMotor armControlLeft;
	DcMotor armControlRight;
	DcMotor armExtendLeft;
	DcMotor armExtendRight;
	
	Servo clawControl;
	Servo clawLeft;
	Servo clawRight;

	private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

	/**
	 * The variable to store our instance of the TensorFlow Object Detection processor.
	 */
	private TfodProcessor tfod;

	/**
	 * The variable to store our instance of the vision portal.
	 */
	private VisionPortal visionPortal;

	@Override
	public void runOpMode() {
		armExtendLeft = hardwareMap.dcMotor.get("acr");
		armExtendRight = hardwareMap.dcMotor.get("acl");
		armControlLeft = hardwareMap.dcMotor.get("aer");
		armControlRight = hardwareMap.dcMotor.get("ael");
		clawControl = hardwareMap.servo.get("c");
		
		clawLeft = hardwareMap.servo.get("cr");
		clawRight = hardwareMap.servo.get("cl");
		
		armControlLeft.setDirection(DcMotor.Direction.REVERSE);
		armExtendLeft.setDirection(DcMotor.Direction.REVERSE);
		
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
		
		initTfod();

		// Wait for the DS start button to be touched.
		telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
		telemetry.addData(">", "Touch Play to start OpMode");
		telemetry.update();
		waitForStart();

		if (opModeIsActive()) {
			while (opModeIsActive()) {
				telemetry.addData("ARMS: ", armControlLeft.getCurrentPosition() + " " +
				armControlRight.getCurrentPosition() + " " + clawControl.getPosition() + " " +
				armExtendLeft.getCurrentPosition() + " " + armExtendRight.getCurrentPosition());
				
				clawControl.setPosition(0.535);
				
				telemetryTfod();

				// Push telemetry to the Driver Station.
				telemetry.update();

				// Save CPU resources; can resume streaming when needed.
				if (gamepad1.dpad_down) {
					visionPortal.stopStreaming();
				} else if (gamepad1.dpad_up) {
					visionPortal.resumeStreaming();
				}

				// Share the CPU.
				sleep(20);
			}
		}

		// Save more CPU resources when camera is no longer needed.
		visionPortal.close();

	}   // end runOpMode()

	/**
	 * Initialize the TensorFlow Object Detection processor.
	 */
	private void initTfod() {

		// Create the TensorFlow processor by using a builder.
		tfod = new TfodProcessor.Builder()

			// With the following lines commented out, the default TfodProcessor Builder
			// will load the default model for the season. To define a custom model to load, 
			// choose one of the following:
			//   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
			//   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
			//.setModelAssetName(TFOD_MODEL_ASSET)
			.setModelFileName(TFOD_MODEL_FILE)

			// The following default settings are available to un-comment and edit as needed to 
			// set parameters for custom models.
			.setModelLabels(LABELS)
			//.setIsModelTensorFlow2(true)
			//.setIsModelQuantized(true)
			//.setModelInputSize(300)
			//.setModelAspectRatio(16.0 / 9.0)

			.build();

		// Create the vision portal by using a builder.
		VisionPortal.Builder builder = new VisionPortal.Builder();

		// Set the camera (webcam vs. built-in RC phone camera).
		if (USE_WEBCAM) {
			builder.setCamera(hardwareMap.get(WebcamName.class, "main camera"));
		} else {
			builder.setCamera(BuiltinCameraDirection.BACK);
		}

		// Choose a camera resolution. Not all cameras support all resolutions.
		//builder.setCameraResolution(new Size(640, 480));

		// Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
		//builder.enableLiveView(true);

		// Set the stream format; MJPEG uses less bandwidth than default YUY2.
		//builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

		// Choose whether or not LiveView stops if no processors are enabled.
		// If set "true", monitor shows solid orange screen if no processors enabled.
		// If set "false", monitor shows camera view without annotations.
		//builder.setAutoStopLiveView(false);

		// Set and enable the processor.
		builder.addProcessor(tfod);

		// Build the Vision Portal, using the above settings.
		visionPortal = builder.build();

		// Set confidence threshold for TFOD recognitions, at any time.
		//tfod.setMinResultConfidence(0.75f);

		// Disable or re-enable the TFOD processor at any time.
		//visionPortal.setProcessorEnabled(tfod, true);

	}   // end method initTfod()

	/**
	 * Add telemetry about TensorFlow Object Detection (TFOD) recognitions.
	 */
	private void telemetryTfod() {

		List<Recognition> currentRecognitions = tfod.getRecognitions();
		telemetry.addData("# Objects Detected", currentRecognitions.size());

		// Step through the list of recognitions and display info for each one.
		for (Recognition recognition : currentRecognitions) {
			double x = (recognition.getLeft() + recognition.getRight()) / 2 ;
			double y = (recognition.getTop()  + recognition.getBottom()) / 2 ;

			telemetry.addData(""," ");
			telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
			telemetry.addData("- Position", "%.0f / %.0f", x, y);
			telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
		}   // end for() loop

	}   // end method telemetryTfod()

}   // end class
