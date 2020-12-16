package org.team1619;

import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

public class Sim {

	private static final Logger sLogger = LogManager.getLogger(Sim.class);

	public static void main(String[] args) {
		UACRRobot uacrRobot = UACRRobot.buildSimRobot();

		sLogger.info("Starting services");
		uacrRobot.getServiceManager().start();
		uacrRobot.getServiceManager().awaitHealthy();
		sLogger.info("********************* ALL SERVICES STARTED *******************************");

		uacrRobot.getServiceManager().awaitStopped();
		sLogger.info("All Services stopped");
	}

}
