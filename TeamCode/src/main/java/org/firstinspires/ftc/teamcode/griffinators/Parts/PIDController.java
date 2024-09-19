package org.firstinspires.ftc.teamcode.griffinators.Parts;

public class PIDController
{
    // kp: Proportional
    // ki: Integral
    // kd: Derivative
    private final double kp;
    private final double ki;
    private final double kd;

    private final double deadzone;
    private final double maxIntegral;

    private double previousError = 0;
    private double integral = 0;

    public PIDController(double kp, double ki, double kd, double deadzone, double maxIntegral)
    {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.deadzone = deadzone;
        this.maxIntegral = maxIntegral;
    }

    public double calculate(double target, double current)
    {
        double error = target - current;
        if (Math.abs(error) < deadzone)
        {
            return 0;
        }

        double proportional = kp * error;

        integral += error;
        double min = -maxIntegral;
        integral = Math.max(min, Math.min(integral, maxIntegral));
        double integralTerm = ki * integral;

        double derivative = kd * (error - previousError);
        previousError = error;

        return proportional + integralTerm + derivative;
    }

    public void reset()
    {
        integral = 0;
        previousError = 0;
    }
}
