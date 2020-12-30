package org.team1619;

import org.team1619.modelfactory.SimModelFactory;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.sim.SimDashboard;
import org.uacr.robot.AbstractModelFactory;

public class SimRobot extends RobotCore {

    @Override
    protected Dashboard createDashboard() {
        return new SimDashboard();
    }

    @Override
    protected AbstractModelFactory createModelFactory() {
        return new SimModelFactory(fHardwareFactory, fEventBus, fInputValues, fOutputValues, fRobotConfiguration,
                fObjectsDirectory);
    }
}
