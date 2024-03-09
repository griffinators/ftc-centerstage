import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;

@Disabled
@Deprecated
/*
 IMPORTANT: Motor names are reconfigured during the migration to switch orientation.
 Affected code includes `hardwareMap.*.get` statements and which DcMotors are reversed
*/

@Autonomous(name = "Autonomous Close Right", group = "Main")
public class AutonomousMainCloseRight extends LinearOpMode {
	private TfodProcessor tfod;
	private VisionPortal visionPortal;
	private AprilTagProcessor aprilTag;

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


	@Override
	public void runOpMode() {
		int pixelPosition;
		initControls();
		initRecognition();

		// Wait for the DS start button to be touched.
		telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
		telemetry.addData(">", "Touch Play to start OpMode");
		telemetry.update();
		clawState(1, 0);
		clawState(1, 1);
		clawArmAngle(CLAW_CONTROL_CLOSED_POS);
		waitForStart();
		if (opModeIsActive()) {
			clawState(1, 0);
			clawState(1, 1);
			visionPortal.resumeStreaming();
			fullMove(2, 500);
			sleep(1000);
			//waitForWheelsToFinish();

			// pixelPosition = 1;
			pixelPosition = putYellow();
			clawArmAngle(CLAW_CONTROL_CLOSED_POS);
			sleep(400);
			returnToStart(pixelPosition);
			park();
			//putPurple(pixelPosition);
			telemetry.addData("Status", "Sleeping");
			UpdateTelemetryPosition();
			telemetry.update();
			sleep(10000);
		}
		visionPortal.close();
	}

	private void park() {
		fullMove(2, 80);
		waitForFinish();
		fullMove(3, 2000);
		waitForFinish();
	}

	private void putPurple(int pixelPos){
		returnToStart(pixelPos);
		fullMove(2, 80);
		waitForFinish();
		fullRotation(84);
		waitForFinish();
		fullMove(2, 2500);
		waitForFinish();
		fullRotation(3);
		fullMove(3, 1100);
		clawArmAngle(CLAW_CONTROL_GROUND_POS);
		waitForFinish();
		alignWithCorrectAprilTag(pixelPos);
	}
	private void alignWithCorrectAprilTag(int index){
		AprilTagDetection detection = getATRecognition(index, 40, 120);
		if (detection == null){
			telemetry.addData("Status", "Cannot get AprilTag");
			UpdateTelemetryPosition();
			telemetry.update();
			sleep(4000);
			return;
		}
		while(detection.center.x < -0.7 || detection.center.x > 0.7){
			telemetry.addData("Status", "Moving To April Tag");
			fullMove(3, (int) (detection.center.x) * 5);
			waitForFinish();
			detection = getATRecognition(index, 40, 120);
		}
		telemetry.addData("Status", "Aligned with AprilTag");
		UpdateTelemetryPosition();
		telemetry.update();
		sleep(2000);
	}

	private AprilTagDetection getATRecognition(int index, int times, int delay){
		List<AprilTagDetection> currentDetections = aprilTag.getDetections();
		StringBuilder builder = new StringBuilder();
		AprilTagDetection correctDetection = null;
		int i = 0;
		while(correctDetection == null && i < times){
			for (AprilTagDetection detection : currentDetections){
				builder.append(detection.id);
				if (detection.id == index){
					correctDetection = detection;
					break;
				}
			}
			sleep(delay);
			i++;
		}
		return correctDetection;
	}
	private void returnToStart(int index){
		if(index == 1){
			telemetry.addData("Rotation:", "" + currentRot);
			fullRotation(-currentRot);
			waitForFinish();
			fullMove(0, 650);
			waitForFinish();
			resetWheels();
		}else {
			fl = 0;
			fr = 0;
			rl = 0;
			rr = 0;
			setTargetPos();
		}
		waitForFinish();
	}

	private int putYellow(){
		if(checkFront()){
			telemetry.addData("Status", "Found Front");
			UpdateTelemetryPosition();
			telemetry.update();
			moveFront();
			putPixel();
			sleep(400);
			return 2;
		}
		if(checkRight()){
			telemetry.addData("Status", "Found Right");
			UpdateTelemetryPosition();
			telemetry.update();
			moveRight();
			putPixel();
			sleep(400);
			return 3;
		}
		moveLeft();
		putPixel();
		sleep(400);
		return 1;
	}


	private int SETUP_ARM_ROTATION = 500;
	private int SETUP_ARM_EXTENSION = 0;
	private double SETUP_CLAW_ROTATION_FRONT = 0.415;
	private double SETUP_CLAW_ROTATION_RIGHT = 0.405;
	private double CLAW_CONTROL_GROUND_POS = 0.54;
	private double CLAW_CONTROL_CLOSED_POS = 0.65;

	private boolean checkFront(){
		telemetry.addData("Status", "Searching Front");
		UpdateTelemetryPosition();
		telemetry.update();

		setExtentionAndRotation(SETUP_ARM_EXTENSION, SETUP_ARM_ROTATION);
		clawArmAngle(SETUP_CLAW_ROTATION_FRONT);
		waitForArmsToFinish();
		return tryTFRecognition(40, 120);
	}

	private boolean checkRight(){
		telemetry.addData("Status", "Searching Right");
		UpdateTelemetryPosition();
		telemetry.update();
		fullMove(1, 630);
		clawArmAngle(SETUP_CLAW_ROTATION_RIGHT);
		return tryTFRecognition(40, 120);
	}

	private boolean tryTFRecognition(int times, int delay){
		Recognition recognition = getTFRecognition();
		int i = 0;
		while(recognition == null && i < times){
			recognition = getTFRecognition();
			sleep(delay);
			i++;
		}
		return i != times;
	}

	private void moveFront(){
		clawArmAngle(CLAW_CONTROL_GROUND_POS);
		fullMove(1, 250);
		waitForWheelsToFinish();
		setExtentionAndRotation(5, 100);
		while ((armControlLeft.isBusy() || armControlRight.isBusy()) && (armExtendLeft.isBusy() || armExtendRight.isBusy())) {
		}
		fullMove(2, 550);
		waitForWheelsToFinish();
	}

	private void moveRight(){
		clawArmAngle(CLAW_CONTROL_GROUND_POS);
		sleep(700);
		setExtentionAndRotation(5, 85);
		fullMove(2, 200);
		waitForFinish();
	}

	private void moveLeft(){
		clawArmAngle(CLAW_CONTROL_GROUND_POS);
		fullRotation(-52);
		waitForFinish();
		setExtentionAndRotation(5, 85);
		fullMove(2, 690);
		waitForFinish();
	}


	private void putPixel(){
		clawArmAngle(CLAW_CONTROL_GROUND_POS);
		clawState(0, 0);
	}

	private double LEFT_CLAW_REST = 0.75;
	private double RIGHT_CLAW_REST = 0.2;

	private double LEFT_CLAW_HOLD = 0.595;
	private double RIGHT_CLAW_HOLD = 0.34;

	private void clawState(int state, int claw){
		if(claw == 0){
			clawLeft.setPosition(state == 0 ? LEFT_CLAW_REST : LEFT_CLAW_HOLD);
		}else if(claw == 1){
			clawRight.setPosition(state == 0 ? RIGHT_CLAW_REST : RIGHT_CLAW_HOLD);
		}else{
			throw new RuntimeException("Cannot get claw: " + claw);
		}

	}


	private void clawArmAngle(double initClawAngle) {
		clawControl.setPosition(initClawAngle);
	}

	int fl = 0;
	int fr = 0;
	int rl = 0;
	int rr = 0;
	int currentRot = 0;

	int DEGREE_TO_MOTOR_RATIO = 12;

	private void fullRotation(int angle){
		int rotation = (angle * DEGREE_TO_MOTOR_RATIO);
		fl += rotation;
		fr -= rotation;
		rl += rotation;
		rr -= rotation;
		currentRot += angle;
		setTargetPos();
	}

	private void fullMove(int dir, int distance){
		switch(dir){
			case 0:
				fl += distance;
				fr += distance;
				rl += distance;
				rr += distance;
				break;
			case 1:
				fl += distance;
				fr -= distance;
				rl -= distance;
				rr += distance;
				break;
			case 2:
				fl -= distance;
				fr -= distance;
				rl -= distance;
				rr -= distance;
				break;
			case 3:
				fl -= distance;
				fr += distance;
				rl += distance;
				rr -= distance;
				break;

		}
		telemetry.addData("Wheels:", fl + " " + fr + " " + rl + " " + rr + " ");
		setTargetPos();
	}
	private void setTargetPos(){
		frontLeft.setTargetPosition(fl);
		frontRight.setTargetPosition(fr);
		rearLeft.setTargetPosition(rl);
		rearRight.setTargetPosition(rr);
	}

	private void setExtentionAndRotation(int extention, int rotation){
		setExtention(extention);
		setRotation(rotation);
	}

	private void setExtention(int amount){
		armExtendLeft.setTargetPosition(amount);
		armExtendRight.setTargetPosition(amount);
	}

	private void setRotation(int amount){
		armControlLeft.setTargetPosition(amount);
		armControlRight.setTargetPosition(amount);
	}

	private void setRotationSpeed(float speed){
		armControlLeft.setPower(speed);
		armControlRight.setPower(speed);
	}

	private void setExtentionSpeed(float speed){
		armExtendLeft.setPower(speed);
		armExtendRight.setPower(speed);

	}

	private void waitForFinish(){
		waitForArmsToFinish();
		waitForWheelsToFinish();
	}

	private void waitForArmsToFinish(){
		while ((armControlLeft.isBusy() || armControlRight.isBusy()) && (armExtendLeft.isBusy() || armExtendRight.isBusy())) {
		}
	}

	private void waitForWheelsToFinish(){
		while ((frontLeft.isBusy() || frontRight.isBusy()) || (rearLeft.isBusy() || rearRight.isBusy())) {
		}
	}
	/**
	 * Initialize the TensorFlow Object Detection processor.
	 */
	private void initRecognition() {

		// Create the TensorFlow processor by using a builder.
		tfod = new TfodProcessor.Builder()

				// With the following lines commented out, the default TfodProcessor Builder
				// will load the default model for the season. To define a custom model to load,
				// choose one of the following:
				//   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
				//   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
				//.setModelAssetName(TFOD_MODEL_ASSET)
				//.setModelFileName(TFOD_MODEL_FILE)

				// The following default settings are available to un-comment and edit as needed to
				// set parameters for custom models.
				//.setModelLabels(LABELS)
				//.setIsModelTensorFlow2(true)
				//.setIsModelQuantized(true)
				//.setModelInputSize(300)
				//.setModelAspectRatio(16.0 / 9.0)

				.build();

		aprilTag = AprilTagProcessor.easyCreateWithDefaults();

		// Create the vision portal by using a builder.
		VisionPortal.Builder builder = new VisionPortal.Builder();

		// Set the camera (webcam vs. built-in RC phone camera).

		builder.setCamera(hardwareMap.get(WebcamName.class, "main camera"));


		// Choose a camera resolution. Not all cameras support all resolutions.
		//builder.setCameraResolution(new Size(640, 480));

		// Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
		builder.enableLiveView(true);

		// Set the stream format; MJPEG uses less bandwidth than default YUY2.
		//builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

		// Choose whether or not LiveView stops if no processors are enabled.
		// If set "true", monitor shows solid orange screen if no processors enabled.
		// If set "false", monitor shows camera view without annotations.
		//builder.setAutoStopLiveView(false);

		// Set and enable the processor.
		builder.addProcessor(tfod);
		builder.addProcessor(aprilTag);

		// Build the Vision Portal, using the above settings.
		visionPortal = builder.build();

		// Set confidence threshold for TFOD recognitions, at any time.
		tfod.setMinResultConfidence(0.6f);

		// Disable or re-enable the TFOD processor at any time.
		//visionPortal.setProcessorEnabled(tfod, true);

	}   // end method initTfod()

	private Recognition getTFRecognition(){
		List<Recognition> currentRecognitions = tfod.getRecognitions();
		if (currentRecognitions.size() != 1){
			return null;
		}
		Recognition recognition = currentRecognitions.get(0);
		return recognition;
	}

	private void UpdateTelemetryPosition(){
		telemetry.addData("Wheels:", "fl: " +  fl + "; fr: " + fr + "; rl: " +  rl + "; rr: " + rr);
		telemetry.addData("Arm:", "ael: " + armExtendLeft.getCurrentPosition() + "; aer: " + armExtendRight.getCurrentPosition()
				+ "; acl: " + armControlLeft.getCurrentPosition() + "; acr: " + armControlRight.getCurrentPosition());

	}

	private void resetWheels(){
		frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		frontLeft.setTargetPosition(0);
		frontRight.setTargetPosition(0);
		rearLeft.setTargetPosition(0);
		rearRight.setTargetPosition(0);

		frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		rearRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

		fl = 0;
		fr = 0;
		rl = 0;
		rr = 0;
		currentRot = 0;
	}

	private void initControls(){
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
		frontRight.setDirection(DcMotor.Direction.REVERSE);
		rearRight.setDirection(DcMotor.Direction.REVERSE);

		frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		frontLeft.setTargetPosition(0);
		frontRight.setTargetPosition(0);
		rearLeft.setTargetPosition(0);
		rearRight.setTargetPosition(0);

		frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		rearRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

		frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

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

		frontLeft.setPower(0.5f);
		frontRight.setPower(0.5f);
		rearLeft.setPower(0.5f);
		rearRight.setPower(0.5f);

		armExtendLeft.setPower(0.5f);
		armExtendRight.setPower(0.5f);
		armControlLeft.setPower(0.5f);
		armControlRight.setPower(0.5f);
	}
}

