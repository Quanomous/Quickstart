package org.firstinspires.ftc.teamcode;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * QuanomousCommand

 * Extend this class and implement each method using your robot's code.
 * Then call execute() with a loaded JSON script to run your autonomous.

 * No external dependencies required — works with any FTC framework
 * (FTCLib, RoadRunner, raw LinearOpMode, etc.)

 * Quickstart:
 *   1. Extend this class
 *   2. Implement each abstract method with your robot's code
 *   3. Call execute(QuanomousLoader.load("myAuto.json")) in your OpMode

 * JSON Command Reference:
 *   { "cmd": "delay",       "seconds": 1.5 }
 *   { "cmd": "intake",      "spike": 1 }
 *   { "cmd": "intake_gate" }
 *   { "cmd": "deposit",     "locale": "near", "txo": 0.5, "tyo": 0.5 }
 *   { "cmd": "release" }
 *   { "cmd": "chase",       "cycles": 3 }
 *   { "cmd": "park",        "axial": "front", "lateral": "left", "gate": false }
 *   { "cmd": "drive",       "tx": 1.0, "ty": 2.0, "h": 90,
 *                            "axial": "center", "lateral": "center" }
 */
public abstract class QuanomousCommand {

    // Abstract methods — implement these with your robot's code

    /**
     * Intake a sample from the given spike position.
     * @param spike  1, 2, or 3 — whatever you call them
     */
    public abstract void intake(int spike);

    /**
     * Run intake from the gate position.
     */
    public abstract void intakeGate();

    /**
     * Drive to the deposit position and deposit the sample.
     * @param locale  "near" or "far" — which basket/backdrop side
     * @param txo     X tile offset from the deposit position
     * @param tyo     Y tile offset from the deposit position
     */
    public abstract void deposit(String locale, double txo, double tyo);

    /**
     * Release the gate
     */
    public abstract void release();

    /**
     * Chase and intake samples from the field for a number of cycles.
     * @param cycles  number of cycles to run (Integer.MAX_VALUE = run until timeout)
     */
    public abstract void chase(int cycles);

    /**
     * Drive to the parking zone.
     * @param axial    "front", "back", or "center"
     * @param lateral  "left", "right", or "center"
     * @param gate     whether to close the gate before parking
     */
    public abstract void park(String axial, String lateral, boolean gate);

    /**
     * Drive to an absolute field position.
     * @param tx       X position in tiles
     * @param ty       Y position in tiles (always positive — direction handled internally)
     * @param heading  target heading in degrees
     * @param axial    "front", "back", or "center" — which part of robot to align
     * @param lateral  "left", "right", or "center" — which part of robot to align
     */
    public abstract void drive(double tx, double ty, double heading,
                                String axial, String lateral);

    // Built-in commands — override if needed

    /**
     * Wait for a given number of seconds.
     * Override this if your framework has a preferred sleep/wait mechanism.
     * @param seconds  how long to wait
     */
    public void delay(double seconds) {
        try {
            Thread.sleep((long)(seconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    // Runner — do not override
    /**
     * Execute a parsed JSON script sequentially.
     * Commands run in order, blocking until each one finishes.
     * @param script  JSONArray loaded from your autonomous JSON file
     */
    public final void execute(JSONArray script) {
        try {
            for (int i = 0; i < script.length(); i++) {
                JSONObject obj = script.getJSONObject(i);
                String cmd = obj.getString("cmd");

                switch (cmd) {
                    case "delay":
                        delay(obj.getDouble("seconds"));
                        break;

                    case "intake":
                        intake(obj.getInt("spike"));
                        break;

                    case "intake_gate":
                        intakeGate();
                        break;

                    case "deposit":
                        deposit(
                            obj.getString("locale"),
                            obj.getDouble("txo"),
                            obj.getDouble("tyo")
                        );
                        break;

                    case "release":
                        release();
                        break;

                    case "chase":
                        int cycles = obj.getInt("cycles");
                        chase(cycles == 0 ? Integer.MAX_VALUE : cycles);
                        break;

                    case "park":
                        park(
                            obj.optString("axial", "center"),
                            obj.optString("lateral", "center"),
                            obj.optBoolean("gate", false)
                        );
                        break;

                    case "drive":
                        drive(
                            obj.getDouble("tx"),
                            obj.getDouble("ty"),
                            obj.getDouble("h"),
                            obj.optString("axial", "center"),
                            obj.optString("lateral", "center")
                        );
                        break;

                    default:
                        onUnknownCommand(cmd, obj);
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Quanomous execution failed", e);
        }
    }

    /**
     * Called when an unrecognized command is encountered.
     * Override to handle custom commands for your team.
     * Default behavior: throws an exception.
     *
     * @param cmd  the unrecognized command string
     * @param obj  the full JSON object for that command
     */
    public void onUnknownCommand(String cmd, JSONObject obj) {
        throw new IllegalArgumentException("Unknown Quanomous command: \"" + cmd + "\"");
    }
}