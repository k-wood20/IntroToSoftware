package org.team1619;

import org.team1619.services.logging.LoggingService;
import org.team1619.shared.abstractions.Dashboard;
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

abstract class RobotCore {

    private static final Logger sLogger = LogManager.getLogger(RobotCore.class);

    protected final FMS fFms;
    protected final RobotConfiguration fRobotConfiguration;
    protected final InputValues fInputValues;
    protected final OutputValues fOutputValues;
    protected final HardwareFactory fHardwareFactory;
    protected final EventBus fEventBus;
    protected final ObjectsDirectory fObjectsDirectory;
    protected final StateControls fStateControls;
    protected final ServiceManager fServiceManager;
    protected final AbstractModelFactory fModelFactory;
    protected final LoggingService fLoggingService;

    protected RobotCore() {
        YamlConfigParser parser = new YamlConfigParser();
        parser.load("general.yaml");

        Config loggerConfig = parser.getConfig("logger");
        if (loggerConfig.contains("log_level")) {
            LogManager.setLogLevel(loggerConfig.getEnum("log_level", LogManager.Level.class));
        }

        fFms = new SharedFMS();
        fRobotConfiguration = new SharedRobotConfiguration();
        fInputValues = new SharedInputValues();
        fOutputValues = new SharedOutputValues();
        fHardwareFactory = new SharedHardwareFactory();
        fEventBus = new SharedEventBus();
        fObjectsDirectory = new SharedObjectsDirectory();
        fStateControls = new StateControls(fInputValues, fRobotConfiguration);

        fModelFactory = createModelFactory();

        fLoggingService = new LoggingService(fInputValues, fOutputValues, fRobotConfiguration, createDashboard());

        StatesService statesService = new StatesService(fModelFactory, fInputValues, fFms, fRobotConfiguration,
                fObjectsDirectory, fStateControls);

        InputService inputService = new InputService(fModelFactory, fInputValues, fRobotConfiguration,
                fObjectsDirectory);

        OutputService outputService = new OutputService(fModelFactory, fFms, fInputValues,
                fOutputValues, fRobotConfiguration, fObjectsDirectory);

        WebDashboardService webDashboardService = new WebDashboardService(fEventBus, fFms,
                fInputValues, fOutputValues, fRobotConfiguration);

        fServiceManager = new AsyncServiceManager(
                new ScheduledMultiService(new Scheduler(10), inputService, statesService, outputService),
                new ScheduledMultiService(new Scheduler(30), fLoggingService, webDashboardService));
    }

    protected abstract Dashboard createDashboard();

    protected abstract AbstractModelFactory createModelFactory();

    public void start() {
        sLogger.info("Starting services");
        fServiceManager.start();
        fServiceManager.awaitHealthy();
        sLogger.info("********************* ALL SERVICES STARTED *******************************");
    }

    public FMS getFms() {
        return fFms;
    }
}
