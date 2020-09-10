package org.team1619.behavior;

import org.uacr.models.behavior.Behavior;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * Allows the robot to be driven via the xbox controllers
 */

public class Drivetrain_Percent implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Drivetrain_Percent.class);
	private static final Set<String> sSubsystems = Set.of("ss_drivetrain");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final String fXAxisID;
	private final String fYAxisID;
	private final String fGearID;

	public Drivetrain_Percent(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fXAxisID = robotConfiguration.getString("global_drivetrain", "x");
		fYAxisID = robotConfiguration.getString("global_drivetrain", "y");
		fGearID = robotConfiguration.getString("global_drivetrain", "gear_shift");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

	}

	@Override
	public void update() {
		double x = fSharedInputValues.getNumeric(fXAxisID);
		double y = fSharedInputValues.getNumeric(fYAxisID);
		boolean gearShift = fSharedInputValues.getBoolean(fGearID);

		double leftMotorSpeed = y + x;
		double rightMotorSpeed = y - x;

		// Scale motor speeds to be between -1 and 1
		if(leftMotorSpeed > 1){
			rightMotorSpeed = rightMotorSpeed - (leftMotorSpeed - 1);
			leftMotorSpeed = 1;
		}
		if(rightMotorSpeed > 1){
			leftMotorSpeed = leftMotorSpeed - (rightMotorSpeed - 1);
			rightMotorSpeed = 1;
		}
		if(leftMotorSpeed < -1){
			rightMotorSpeed = rightMotorSpeed - (leftMotorSpeed + 1);
			leftMotorSpeed = -1;
		}
		if(rightMotorSpeed < -1) {
			leftMotorSpeed = leftMotorSpeed - (rightMotorSpeed + 1);
		}

		fSharedOutputValues.setNumeric("mo_drivetrain_left_primary", "percent", leftMotorSpeed);
		fSharedOutputValues.setNumeric("mo_drivetrain_right_primary", "percent", rightMotorSpeed);
		fSharedOutputValues.setBoolean("so_drivetrain_shifter", gearShift);

		fSharedInputValues.setBoolean("ipb_is_low_gear", gearShift);
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("mo_drivetrain_left_primary", "percent", 0);
		fSharedOutputValues.setNumeric("mo_drivetrain_right_primary", "percent", 0);
		fSharedOutputValues.setBoolean("so_drivetrain_shifter", false);
		fSharedInputValues.setBoolean("ipb_drivetrain_shifter", false);
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}