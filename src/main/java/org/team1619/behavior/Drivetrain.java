package org.team1619.behavior;

import org.uacr.models.behavior.Behavior;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.Timer;

import java.util.Set;

/**
 * Example behavior to copy for other behaviors
 */

public class Drivetrain implements Behavior {

	private static final Logger logger = LogManager.getLogger(Drivetrain.class);
	private static final Set<String> subsystems = Set.of("ss_drivetrain");

	private final InputValues sharedInputValues;
	private final OutputValues sharedOutputValues;

	private final String left_driveAxis;
	private final String right_driveAxis;

	private Timer timer;

	private int configurationValue;

	public Drivetrain(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		sharedInputValues = inputValues;
		sharedOutputValues = outputValues;

		left_driveAxis = robotConfiguration.getString("global_drivetrain", "left_drive_axis");
        right_driveAxis = robotConfiguration.getString("global_drivetrain", "right_drive_axis");

		configurationValue = 0;
		timer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		logger.debug("Entering state {}", stateName);

		configurationValue = config.getInt("config_key", 0);
		timer.start(configurationValue);
	}

	@Override
	public void update() {
		double left_driveAxis = sharedInputValues.getNumeric(this.left_driveAxis);
		double right_driveAxis = sharedInputValues.getNumeric(this.right_driveAxis);

		sharedOutputValues.setNumeric("opn_drivetrain_left", "percent", left_driveAxis);
		sharedOutputValues.setNumeric("opn_drivetrain_right", "percent", right_driveAxis);

	}

	@Override
	public void dispose() {

		sharedOutputValues.setNumeric("opn_drivetrain_left", "percent", 0.0);
		sharedOutputValues.setNumeric("opn_drivetrain_right", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		return timer.isDone();
	}

	@Override
	public Set<String> getSubsystems() {
		return subsystems;
	}
}