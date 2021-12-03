package org.team1619.state;

import org.team1619.state.modelogic.*;
import org.uacr.robot.AbstractStateControls;
import org.uacr.robot.ControlMode;
import org.uacr.shared.abstractions.FMS;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Timer;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

/**
 * Selects the robot status and control modes to be used by the RobotManager for competition bot
 */

public class StateControls extends AbstractStateControls {

	private static final Logger sLogger = LogManager.getLogger(StateControls.class);

	private final Timer timerMode;
	private final Timer timerEndgame;
	private final boolean initialIsManualMode;

	private boolean mIsEndgameMode;
	private boolean mIsManualMode;

	@Inject
	public StateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
		super(inputValues, robotConfiguration);

		registerRobotStatus(new RobotStatus(inputValues, robotConfiguration));
		registerModeLogic(ControlMode.AUTONOMOUS, new AutonomousModeLogic(inputValues, robotConfiguration));
		registerModeLogic(ControlMode.TELEOP, new TeleopModeLogic(inputValues, robotConfiguration));
		registerModeLogic(ControlMode.MANUAL_TELEOP, new ManualTeleopModeLogic(inputValues, robotConfiguration));
		registerModeLogic(ControlMode.ENDGAME, new EndgameModeLogic(inputValues, robotConfiguration));
		registerModeLogic(ControlMode.MANUAL_ENDGAME, new ManualEndgameModeLogic(inputValues, robotConfiguration));

		//Modes
		mIsEndgameMode = false;
		mIsManualMode = false;

		timerMode = new Timer();
		mFmsMode = FMS.Mode.DISABLED;
		//Climb
		timerEndgame = new Timer();
		if (robotConfiguration.contains("general", "initial_teleop_mode")) {
			switch (robotConfiguration.getString("general", "initial_teleop_mode")) {
				case "teleop_mode":
					initialIsManualMode = false;
					break;
				case "manual_mode":
					initialIsManualMode = true;
					break;
				default:
					initialIsManualMode = false;
					sLogger.error("Mode specified does not exist");
			}
		} else {
			initialIsManualMode = false;
		}
	}

	@Override
	public void initialize(FMS.Mode currentFmsMode) {
		mFmsMode = currentFmsMode;

		mIsEndgameMode = false;
		mIsManualMode = initialIsManualMode;

		sharedInputValues.setBoolean("ipb_endgame_enabled", false);
	}

	@Override
	public void update() {

		if (mFmsMode == FMS.Mode.AUTONOMOUS) {
			setCurrentControlMode(ControlMode.AUTONOMOUS);
		} else {
			if (!timerMode.isStarted() && sharedInputValues.getBooleanRisingEdge("ipb_operator_start")) {
				timerMode.start(1000);
			} else if (timerMode.isDone()) {
				mIsManualMode = !mIsManualMode;
				timerMode.reset();
			} else if (!sharedInputValues.getBoolean("ipb_operator_start")) {
				timerMode.reset();
			}

			if (!timerEndgame.isStarted() && sharedInputValues.getBooleanRisingEdge("ipb_operator_back")) {
				timerEndgame.start(1000);
			} else if (timerEndgame.isDone()) {
				mIsEndgameMode = !mIsEndgameMode;
				timerEndgame.reset();
			} else if (!sharedInputValues.getBoolean("ipb_operator_back")) {
				timerEndgame.reset();
			}

			if (mIsManualMode) {
				if (mIsEndgameMode) {
					setCurrentControlMode(ControlMode.MANUAL_ENDGAME);
				} else {
					setCurrentControlMode(ControlMode.MANUAL_TELEOP);
				}
			} else {
				if (mIsEndgameMode) {
					setCurrentControlMode(ControlMode.ENDGAME);
				} else {
					setCurrentControlMode(ControlMode.TELEOP);
				}
			}

			sharedInputValues.setBoolean("ipb_endgame_enabled", mIsEndgameMode);
		}
		sharedInputValues.setString("ips_mode", getCurrentControlMode().toString());
	}

	@Override
	public void dispose() {
		sharedInputValues.setString("ips_mode", "DISABLED");
	}
}
