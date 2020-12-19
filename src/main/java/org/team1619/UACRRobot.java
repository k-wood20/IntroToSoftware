package org.team1619;

import org.team1619.modelfactory.RobotModelFactory;
import org.team1619.modelfactory.SimModelFactory;
import org.team1619.services.logging.LoggingService;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.robot.RobotDashboard;
import org.team1619.shared.concretions.sim.SimDashboard;
import org.team1619.state.StateControls;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.services.input.InputService;
import org.uacr.services.output.OutputService;
import org.uacr.services.states.StatesService;
import org.uacr.services.webdashboard.WebDashboardService;
import org.uacr.shared.abstractions.*;
import org.uacr.shared.concretions.*;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledMultiService;
import org.uacr.utilities.services.Scheduler;
import org.uacr.utilities.services.managers.AsyncServiceManager;
import org.uacr.utilities.services.managers.ServiceManager;

/**
 * TODO rename UACRRobot to something nicer and move into frc-core-plugin and/or uacr-robot-core.
 * TODO deduplicate UACRRobot buildHardwareRobot() and buildSimRobot()
 */
public class UACRRobot {

    private static final Logger sLogger = LogManager.getLogger(UACRRobot.class);

    private ServiceManager mServiceManager;
    private FMS mFms;
    private RobotConfiguration mRobotConfiguration;
    private InputValues mInputValues;
    private OutputValues mOutputValues;
    private HardwareFactory mHardwareFactory;
    private EventBus mEventBus;
    private ObjectsDirectory mObjectsDirectory;
    private StateControls mStateControls;
    private AbstractModelFactory mModelFactory;
    private LoggingService mLoggingService;

    public void start() {
        sLogger.info("Starting services");
        mServiceManager.start();
        mServiceManager.awaitHealthy();
        sLogger.info("********************* ALL SERVICES STARTED *******************************");
    }

    private static UACRRobot buildCommonRobot() {
        YamlConfigParser parser = new YamlConfigParser();
        parser.load("general.yaml");

        Config loggerConfig = parser.getConfig("logger");
        if (loggerConfig.contains("log_level")) {
            LogManager.setLogLevel(loggerConfig.getEnum("log_level", LogManager.Level.class));
        }

        UACRRobot robot = new UACRRobot();

        robot.mFms = new SharedFMS();
        robot.mRobotConfiguration = new SharedRobotConfiguration();
        robot.mInputValues = new SharedInputValues();
        robot.mOutputValues = new SharedOutputValues();
        robot.mHardwareFactory = new SharedHardwareFactory();
        robot.mEventBus = new SharedEventBus();
        robot.mObjectsDirectory = new SharedObjectsDirectory();
        robot.mStateControls = new StateControls(robot.mInputValues, robot.mRobotConfiguration);

        return robot;
    }

    private static void buildServiceManager(UACRRobot robot) {
        StatesService statesService = new StatesService(robot.mModelFactory, robot.mInputValues, robot.mFms,
                robot.mRobotConfiguration, robot.mObjectsDirectory, robot.mStateControls);

        InputService inputService = new InputService(robot.mModelFactory, robot.mInputValues, robot.mRobotConfiguration,
                robot.mObjectsDirectory);

        OutputService outputService = new OutputService(robot.mModelFactory, robot.mFms, robot.mInputValues,
                robot.mOutputValues, robot.mRobotConfiguration, robot.mObjectsDirectory);

        WebDashboardService webDashboardService = new WebDashboardService(robot.mEventBus, robot.mFms,
                robot.mInputValues, robot.mOutputValues, robot.mRobotConfiguration);

        robot.mServiceManager = new AsyncServiceManager(
                new ScheduledMultiService(new Scheduler(10), inputService, statesService, outputService),
                new ScheduledMultiService(new Scheduler(30), robot.mLoggingService, webDashboardService));
    }

    public static UACRRobot buildHardwareRobot() {

        UACRRobot robot = buildCommonRobot();

        robot.mModelFactory = new RobotModelFactory(robot.mHardwareFactory, robot.mInputValues, robot.mOutputValues,
                robot.mRobotConfiguration, robot.mObjectsDirectory);

        robot.mLoggingService = new LoggingService(robot.mInputValues, robot.mOutputValues,
                robot.mRobotConfiguration, new RobotDashboard(robot.mInputValues));

        buildServiceManager(robot);

        return robot;
    }

    public static UACRRobot buildSimRobot() {

        UACRRobot robot = buildCommonRobot();

        robot.mModelFactory = new SimModelFactory(robot.mHardwareFactory, robot.mEventBus, robot.mInputValues,
                robot.mOutputValues, robot.mRobotConfiguration, robot.mObjectsDirectory);

        robot.mLoggingService = new LoggingService(robot.mInputValues, robot.mOutputValues,
                robot.mRobotConfiguration, new SimDashboard());

        buildServiceManager(robot);

        return robot;
    }

    public FMS getFms() {
        return mFms;
    }
}
