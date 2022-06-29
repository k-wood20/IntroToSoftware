package org.team1619.behavior;

import org.uacr.models.behavior.Behavior;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.Timer;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * Drives the swerve robot, based on the joystick values
 */

public class Drivetrain implements Behavior {

	private static final Logger logger = LogManager.getLogger(Drivetrain.class);
	private static final Set<String> subsystems = Set.of("ss_drivetrain");

	private final InputValues sharedInputValues;
	private final OutputValues sharedOutputValues;

	// declare the drive axis here
	private final String driveAxis;
	// declare the rotation axis here
	private final String rotateAxis;

	public Drivetrain(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		sharedInputValues = inputValues;
		sharedOutputValues = outputValues;

		// initialize the drive axis here
		driveAxis = robotConfiguration.getString("global_drivetrain", "drive_axis");
		// initialize the rotation axis here
		rotateAxis = robotConfiguration.getString("global_drivetrain", "rotate_axis");
	}

	@Override
	public void initialize(String stateName, Config config) {
		logger.debug("Entering state {}", stateName);
	}

	@Override
	public void update() {
		// access the joystick value of the drive axis here
		double driveAxis = sharedInputValues.getNumeric(this.driveAxis);
		// access the joystick value of the rotation axis here
		double rotateAxis = sharedInputValues.getNumeric(this.rotateAxis);

		// set the drive motors here
		sharedOutputValues.setNumeric("opn_drivetrain_front_right_speed", "percent", driveAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_front_left_speed", "percent", driveAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_back_left_speed", "percent", driveAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_back_right_speed", "percent", driveAxis);

		// set the rotation motors here

		sharedOutputValues.setNumeric("opn_drivetrain_front_right_angle_motor", "percent", rotateAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_front_left_angle_motor", "percent", rotateAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_back_left_angle_motor", "percent", rotateAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_back_right_angle_motor", "percent", rotateAxis);
	}

	@Override
	public void dispose() {

		// set the drive motors to 0 here

		sharedOutputValues.setNumeric("opn_drivetrain_front_right_speed", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_front_left_speed", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_back_left_speed", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_back_right_speed", "percent", 0.0);

		// set the rotation motors here

		sharedOutputValues.setNumeric("opn_drivetrain_front_right_angle_motor", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_front_left_angle_motor", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_back_left_angle_motor", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_back_right_angle_motor", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public Set<String> getSubsystems() {
		return subsystems;
	}
}