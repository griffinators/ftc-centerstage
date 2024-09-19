package org.firstinspires.ftc.teamcode.griffinators.Parts;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Actions;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.ArrayList;

public final class Detection
{
    private static final String TFOD_MODEL_FILE = "/sdcard/FIRST/tflitemodels/Griffinators_v2.tflite";
    // Define the labels recognized in the model for TFOD (must be in training order!)
    private static final String[] LABELS = {
            "B",
            "R"
    };

    private final String correctLabel;
    private TfodProcessor tfod;
    private VisionPortal visionPortal;
    public Detection(HardwareMap hardwareMap, String correctLabel)
    {
        this.correctLabel = correctLabel;
        initRecognition(hardwareMap);

    }

    private void initRecognition(HardwareMap hardwareMap)
    {
        tfod = new TfodProcessor.Builder()
                .setModelFileName(TFOD_MODEL_FILE)
                .setModelLabels(LABELS)
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "main camera"))
                .enableLiveView(true)
                .addProcessor(tfod)
                .build();

        tfod.setMinResultConfidence(0.7f);
    }

    public void stream()
    {
        visionPortal.resumeStreaming();
    }
    public Action getRecognition(ArrayList<Recognition> recognitions, double timeOut)
    {
        return new GetRecognitions(recognitions, timeOut);
    }

    public class GetRecognitions implements Action
    {
        private final ArrayList<Recognition> recognitions;
        private final double timeOut;
        private double startTime = 0;

        public GetRecognitions(ArrayList<Recognition> recognitions, double timeOut)
        {
            this.recognitions = recognitions;
            this.timeOut = timeOut;
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket)
        {
            if (startTime == 0){
                startTime = Actions.now();
            }
            double t = Actions.now() - startTime;
            ArrayList<Recognition> currentRecognitions = (ArrayList<Recognition>) tfod.getRecognitions();

            if (currentRecognitions.size() > 0)
            {
                for (Recognition recognition : currentRecognitions)
                {
                    if (recognition.getLabel().equals(correctLabel))
                    {
                     recognitions.add(recognition);
                     return false;
                    }
                }
            }
            if (t > timeOut)
            {
                return false;
            }
            if (currentRecognitions.size() > 1)
            {
                telemetryPacket.put("Detection Status", "More than one object");
                return true;
            }
            currentRecognitions.size();
            telemetryPacket.put("Detection Status", "Cannot find any object");
            return true;
        }
    }
}
