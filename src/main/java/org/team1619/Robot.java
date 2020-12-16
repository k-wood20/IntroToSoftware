package org.team1619;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;

import org.team1619.modelfactory.RobotModelFactory;
import org.team1619.services.logging.LoggingService;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.robot.RobotDashboard;
import org.team1619.state.StateControls;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.services.input.InputService;
import org.uacr.services.output.OutputService;
import org.uacr.services.states.StatesService;
import org.uacr.services.webdashboard.WebDashboardService;
import org.uacr.shared.abstractions.EventBus;
import org.uacr.shared.abstractions.FMS;
import org.uacr.shared.abstractions.HardwareFactory;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.shared.concretions.SharedEventBus;
import org.uacr.shared.concretions.SharedFMS;
import org.uacr.shared.concretions.SharedHardwareFactory;
import org.uacr.shared.concretions.SharedInputValues;
import org.uacr.shared.concretions.SharedObjectsDirectory;
import org.uacr.shared.concretions.SharedOutputValues;
import org.uacr.shared.concretions.SharedRobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledMultiService;
import org.uacr.utilities.services.Scheduler;
import org.uacr.utilities.services.managers.AsyncServiceManager;
import org.uacr.utilities.services.managers.ServiceManager;

public class Robot extends TimedRobot {

	private static final Logger sLogger = LogManager.getLogger(Robot.class);
	private final UACRRobot fUacrRobot;

	public Robot() {
		fUacrRobot = UACRRobot.buildHardwareRobot();
	}

	public static void main(String... args) {
		RobotBase.startRobot(Robot::new);
	}

	@Override
	public void robotInit() {
		sLogger.info("Starting services");
		fUacrRobot.getServiceManager().start();
		fUacrRobot.getServiceManager().awaitHealthy();

		sLogger.info("********************* ALL SERVICES STARTED *******************************");
	}

	@Override
	public void teleopInit() {
		fUacrRobot.getFms().setMode(FMS.Mode.TELEOP);
	}

	@Override
	public void autonomousInit() {
		fUacrRobot.getFms().setMode(FMS.Mode.AUTONOMOUS);
	}

	@Override
	public void disabledInit() {
		fUacrRobot.getFms().setMode(FMS.Mode.DISABLED);
	}

	@Override
	public void testInit() {
		fUacrRobot.getFms().setMode(FMS.Mode.TEST);
	}
}
