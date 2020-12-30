package org.team1619;

import org.team1619.modelfactory.RobotModelFactory;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.robot.RobotDashboard;
import org.uacr.robot.AbstractModelFactory;

public class HardwareRobot extends RobotCore {

    @Override
    protected Dashboard createDashboard() {
        return new RobotDashboard(fInputValues);
    }

    @Override
    protected AbstractModelFactory createModelFactory() {
        return new RobotModelFactory(fHardwareFactory, fInputValues, fOutputValues, fRobotConfiguration,
                fObjectsDirectory);
    }
}
