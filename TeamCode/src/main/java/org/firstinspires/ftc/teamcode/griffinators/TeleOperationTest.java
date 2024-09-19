/*
 * @author:
 * Cloned from 'TeleOperation.java'
 * Created by: Tand (SC120755) 9/19/2024
 *
 * @description:
 * This class implements a state-driven teleoperation mode for a robot using the FTC SDK.
 * It uses a combination of PID controllers for movement and arm control, as well as timers for launcher control.
 *
 * @constants:
 *  - CLAW_OPEN_POS, CLAW_CLOSED_POS: Defines the open and closed positions for the robot claw.
 *  - ARM_EXTENSION_UP, ARM_ROTATION_UP: Target positions for arm extension and rotation when moving up.
 *  - ARM_EXTENSION_DOWN, ARM_ROTATION_DOWN: Target positions for arm extension and rotation when moving down.
 *  - ARM_PID_DEADZONE: Defines the threshold for the arm's PID controller to avoid jittering around the target position.
 *  - ARM_PID_INTEGRAL_LIMIT: Limits the integral term in the PID controller to prevent windup.
 *
 * @methods:
 *  - useMovement()
 *  - useArm()
 *  - useClaw()
 *  - useLauncher()
 *
 * @TODO:
 *  - Handling motor constraints, if the robot has one.
 *
 * @Notes:
 * This class assumes accurate hardware configuration.
 */

package org.firstinspires.ftc.teamcode.griffinators;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Localizer;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.ThreeDeadWheelLocalizer;
import org.firstinspires.ftc.teamcode.griffinators.Parts.PIDController;

@TeleOp(name = "TeleOperation", group = "Robot")
public class TeleOperationTest extends LinearOpMode
{
    // These can be tweaked
    private static final double CLAW_OPEN_POS = 0.12;
    private static final double CLAW_CLOSED_POS = 0.32;
    private static final double LAUNCH_ANGLE_LOW = 40 / 180f;
    private static final double LAUNCH_ANGLE_HIGH = 60 / 180f;
    private static final double MOVEMENT_DEADZONE = 0.1;
    private static final int ARM_EXTENSION_UP = 200;
    private static final int ARM_ROTATION_UP = 170;
    private static final int ARM_EXTENSION_DOWN = 100;
    private static final int ARM_ROTATION_DOWN = 90;
    private static final double ARM_PID_DEADZONE = 0.1;
    private static final double ARM_PID_INTEGRAL_LIMIT = 50;

    private enum ROBOT_STATE
    {
        IDLE, MOVING, CLAW_CONTROL, ARM_CONTROL, LAUNCHING
    }
    private ROBOT_STATE currentState = ROBOT_STATE.IDLE;

    private PIDController movementPID;
    private PIDController armPID;

    DcMotor frontLeft, frontRight, backLeft, backRight, armExtendLeft, armExtendRight, armControlLeft, armControlRight;
    Servo clawLeft, clawRight, launch, launchAngle;

    private Pose2d pose = new Pose2d(0, 0, 0);
    Localizer localizer;

    final ElapsedTime debounceTimer = new ElapsedTime();
    private static final double DEBOUNCE_THRESHOLD = 0.2;
    private final ElapsedTime launchTimer = new ElapsedTime();
    private boolean isLaunching = false;

    @Override
    public void runOpMode() throws InterruptedException
    {
        frontLeft = hardwareMap.dcMotor.get("leftFront");
        frontRight = hardwareMap.dcMotor.get("rightFront");
        backLeft = hardwareMap.dcMotor.get("leftBack");
        backRight = hardwareMap.dcMotor.get("rightBack");
        armExtendLeft = hardwareMap.dcMotor.get("acl");
        armExtendRight = hardwareMap.dcMotor.get("acr");
        armControlLeft = hardwareMap.dcMotor.get("ael");
        armControlRight = hardwareMap.dcMotor.get("aer");

        clawLeft = hardwareMap.servo.get("cl");
        clawRight = hardwareMap.servo.get("cr");
        launch = hardwareMap.servo.get("l");
        launchAngle = hardwareMap.servo.get("lc");

        armControlLeft.setDirection(DcMotor.Direction.REVERSE);
        armExtendLeft.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        localizer = new ThreeDeadWheelLocalizer(hardwareMap, MecanumDrive.PARAMS.inPerTick);
        movementPID = new PIDController(0.1, 0.01, 0.05, ARM_PID_DEADZONE, ARM_PID_INTEGRAL_LIMIT);
        armPID = new PIDController(0.2, 0.01, 0.05, ARM_PID_DEADZONE, ARM_PID_INTEGRAL_LIMIT);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        while (opModeIsActive())
        {
            switch (currentState)
            {
                case IDLE:
                    useIdle();
                    break;
                case MOVING:
                    useMovement();
                    break;
                case CLAW_CONTROL:
                    useClaw();
                    break;
                case ARM_CONTROL:
                    useArm();
                    break;
                case LAUNCHING:
                    useLauncher();
                    break;
            }
        }
    }

    private void useIdle()
    {
        if (Math.abs(gamepad1.left_stick_x) > MOVEMENT_DEADZONE || Math.abs(gamepad1.left_stick_y) > MOVEMENT_DEADZONE)
        {
            currentState = ROBOT_STATE.MOVING;
        }
        else if (gamepad2.left_bumper || gamepad2.right_bumper)
        {
            currentState = ROBOT_STATE.CLAW_CONTROL;
        }
        else if (gamepad2.dpad_up || gamepad2.dpad_down || gamepad2.dpad_left || gamepad2.dpad_right)
        {
            currentState = ROBOT_STATE.ARM_CONTROL;
        }
        else if (gamepad1.x && debounceTimer.seconds() > DEBOUNCE_THRESHOLD)
        {
            debounceTimer.reset();
            currentState = ROBOT_STATE.LAUNCHING;
        }
    }

    private void useMovement()
    {
        pose = pose.plus(localizer.update().value());

        double raw1 = gamepad1.left_stick_x;
        double x = Math.copySign(Math.min(Math.pow(Math.abs(raw1) / 0.9, 3), 1), raw1);
        double raw = -gamepad1.left_stick_y;
        double y = Math.copySign(Math.min(Math.pow(Math.abs(raw) / 0.9, 3), 1), raw);
        double theta = Math.atan2(y, x);
        double power = Math.hypot(x, y);
        double turn = gamepad1.right_stick_x;

        double sin = Math.sin(theta - Math.PI / 4);
        double cos = Math.cos(theta - Math.PI / 4);
        double max = Math.max(Math.abs(sin), Math.abs(cos));

        double sinMax = power * sin / max;
        double cosMax = power * cos / max;

        double frontLeftPower = movementPID.calculate(cosMax + turn, frontLeft.getPower());
        double frontRightPower = movementPID.calculate(sinMax - turn, frontRight.getPower());
        double backLeftPower = movementPID.calculate(sinMax + turn, backLeft.getPower());
        double backRightPower = movementPID.calculate(cosMax - turn, backRight.getPower());

        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);

        if (Math.abs(gamepad1.left_stick_x) < MOVEMENT_DEADZONE && Math.abs(gamepad1.left_stick_y) < MOVEMENT_DEADZONE)
        {
            currentState = ROBOT_STATE.IDLE;
        }
    }

    private void useClaw()
    {
        if (gamepad2.left_bumper && debounceTimer.seconds() > DEBOUNCE_THRESHOLD)
        {
            debounceTimer.reset();
            toggleClaw(clawLeft);
        }
        if (gamepad2.right_bumper && debounceTimer.seconds() > DEBOUNCE_THRESHOLD)
        {
            debounceTimer.reset();
            toggleClaw(clawRight);
        }
        currentState = ROBOT_STATE.IDLE;
    }

    private void toggleClaw(Servo claw)
    {
        claw.setPosition(claw.getPosition() == CLAW_CLOSED_POS ? CLAW_OPEN_POS : CLAW_CLOSED_POS);
    }

    private void useArm()
    {
        if (gamepad2.dpad_up)
        {
            armPIDControl(ARM_EXTENSION_UP, ARM_ROTATION_UP);
        }
        else if (gamepad2.dpad_down)
        {
            armPIDControl(ARM_EXTENSION_DOWN, ARM_ROTATION_DOWN);
        }

        currentState = ROBOT_STATE.IDLE;
    }

    private void armPIDControl(int targetExtensionPosition, int targetRotationPosition)
    {
        double currentExtensionPosition = armExtendLeft.getCurrentPosition();
        double currentRotationPosition = armControlLeft.getCurrentPosition();

        double adjustedExtensionPower = armPID.calculate(targetExtensionPosition, currentExtensionPosition);
        armExtendLeft.setPower(adjustedExtensionPower);
        armExtendRight.setPower(adjustedExtensionPower);

        double adjustedRotationPower = armPID.calculate(targetRotationPosition, currentRotationPosition);
        armControlLeft.setPower(adjustedRotationPower);
        armControlRight.setPower(adjustedRotationPower);
    }

    private void useLauncher()
    {
        telemetry.addData("State", "LAUNCHING");

        if (!isLaunching)
        {
            launchAngle.setPosition(LAUNCH_ANGLE_LOW);
            launchTimer.reset();
            isLaunching = true;
        }
        else
        {
            if (launchTimer.seconds() >= 1.0 && launchTimer.seconds() < 2.0)
            {
                launch.setPosition(0.5f);
            }
            else if (launchTimer.seconds() >= 2.0 && launchTimer.seconds() < 2.5)
            {
                launch.setPosition(-0.5f);
            }
            else if (launchTimer.seconds() >= 2.5)
            {
                launchAngle.setPosition(LAUNCH_ANGLE_HIGH);
                isLaunching = false;
                currentState = ROBOT_STATE.IDLE;
            }
        }
    }
}