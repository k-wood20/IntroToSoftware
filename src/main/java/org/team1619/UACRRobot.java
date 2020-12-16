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
import org.uacr.utilities.services.ScheduledMultiService;
import org.uacr.utilities.services.Scheduler;
import org.uacr.utilities.services.managers.AsyncServiceManager;
import org.uacr.utilities.services.managers.ServiceManager;

/**
 * TODO rename UACRRobot to something nicer and move into frc-core-plugin and/or uacr-robot-core.
 * TODO deduplicate UACRRobot buildHardwareRobot() and buildSimRobot()
 */
public class UACRRobot {
  private final ServiceManager serviceManager;
  private final FMS fms;

  public UACRRobot(ServiceManager serviceManager, FMS fms) {
    this.serviceManager = serviceManager;
    this.fms = fms;
  }

  public ServiceManager getServiceManager() {
    return serviceManager;
  }

  public FMS getFms() {
    return fms;
  }

  public static UACRRobot buildHardwareRobot() {
    YamlConfigParser parser = new YamlConfigParser();
    parser.load("general.yaml");

    Config loggerConfig = parser.getConfig("logger");
    if (loggerConfig.contains("log_level")) {
      LogManager.setLogLevel(loggerConfig.getEnum("log_level", LogManager.Level.class));
    }

    // Move to shared static builder method
    FMS fms = new SharedFMS();
    RobotConfiguration robotConfiguration = new SharedRobotConfiguration();
    InputValues inputValues = new SharedInputValues();
    OutputValues outputValues = new SharedOutputValues();
    HardwareFactory hardwareFactory = new SharedHardwareFactory();
    EventBus eventBus = new SharedEventBus();
    ObjectsDirectory objectsDirectory = new SharedObjectsDirectory();

    AbstractModelFactory modelFactory = new RobotModelFactory(hardwareFactory, inputValues, outputValues, robotConfiguration, objectsDirectory);
    StateControls stateControls = new StateControls(inputValues, robotConfiguration);

    StatesService statesService = new StatesService(modelFactory, inputValues, fms, robotConfiguration, objectsDirectory, stateControls);
    InputService inputService = new InputService(modelFactory, inputValues, robotConfiguration, objectsDirectory);
    Dashboard dashboard = new RobotDashboard(inputValues);
    OutputService outputService = new OutputService(modelFactory, fms, inputValues, outputValues, robotConfiguration, objectsDirectory);

    LoggingService loggingService = new LoggingService(inputValues, outputValues, robotConfiguration, dashboard);
    WebDashboardService webDashboardService = new WebDashboardService(eventBus, fms, inputValues, outputValues, robotConfiguration);

    ScheduledMultiService coreService = new ScheduledMultiService(new Scheduler(10), inputService, statesService, outputService);
    ScheduledMultiService infoService = new ScheduledMultiService(new Scheduler(30), loggingService, webDashboardService);

    ServiceManager serviceManager = new AsyncServiceManager(coreService, infoService);

    return new UACRRobot(serviceManager, fms);
  }

  public static UACRRobot buildSimRobot() {
    YamlConfigParser parser = new YamlConfigParser();
    parser.load("general.yaml");

    Config loggerConfig = parser.getConfig("logger");
    if (loggerConfig.contains("log_level")) {
      LogManager.setLogLevel(loggerConfig.getEnum("log_level", LogManager.Level.class));
    }

    RobotConfiguration robotConfiguration = new SharedRobotConfiguration();
    FMS fms = new SharedFMS();
    InputValues inputValues = new SharedInputValues();
    OutputValues outputValues = new SharedOutputValues();
    HardwareFactory hardwareFactory = new SharedHardwareFactory();
    EventBus eventBus = new SharedEventBus();
    ObjectsDirectory objectsDirectory = new SharedObjectsDirectory();

    AbstractModelFactory modelFactory = new SimModelFactory(hardwareFactory, eventBus, inputValues, outputValues, robotConfiguration, objectsDirectory);
    StateControls stateControls = new StateControls(inputValues, robotConfiguration);

    StatesService statesService = new StatesService(modelFactory, inputValues, fms, robotConfiguration, objectsDirectory, stateControls);
    InputService inputService = new InputService(modelFactory, inputValues, robotConfiguration, objectsDirectory);
    OutputService outputService = new OutputService(modelFactory, fms, inputValues, outputValues, robotConfiguration, objectsDirectory);

    Dashboard dashboard = new SimDashboard();
    LoggingService loggingService = new LoggingService(inputValues, outputValues, robotConfiguration, dashboard);
    WebDashboardService webDashboardService = new WebDashboardService(eventBus, fms, inputValues, outputValues, robotConfiguration);

    ScheduledMultiService coreService = new ScheduledMultiService(new Scheduler(10), inputService, statesService, outputService);
    ScheduledMultiService infoService = new ScheduledMultiService(new Scheduler(30), loggingService, webDashboardService);

    ServiceManager serviceManager = new AsyncServiceManager(coreService, infoService);
    return new UACRRobot(serviceManager, fms);
  }

}
