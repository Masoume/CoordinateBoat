package sami.service.pathplanning.testing;

import sami.environment.Continuous3DEnvWithDiscreteObstacles;
import sami.environment.Continuous3DEnvironment;
import sami.environment.RectangularObstacle;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class EnvironmentFactory {

    static Random rand = new Random();

    public static Continuous3DEnvironment createContinuous3DEnvironment() {
        return createContinuous3DEnvironment(2 + rand.nextInt(20));
    }

    public static Continuous3DEnvironment createContinuous3DEnvironment(int rectObsCount) {

        // double w = rand.nextDouble() * 200.0 + 10.0;
        // double l = rand.nextDouble() * 200.0 + 10.0;
        // double h = rand.nextDouble() * 200.0 + 10.0;
        // temp

        double h = 0.0;
        double w = 200.0;
        double l = 200.0;
        Continuous3DEnvWithDiscreteObstacles e = new Continuous3DEnvWithDiscreteObstacles(w, l, h);

        for (int i = 0; i < rectObsCount; i++) {

            double ow = rand.nextDouble() * 0.4 * w + 0.05 * w;
            double ol = rand.nextDouble() * 0.4 * l + 0.05 * l;
            double oh = rand.nextDouble() * 0.4 * h + 0.05 * h;

            double sx = rand.nextDouble() * w;
            double sy = rand.nextDouble() * l;
            double sz = rand.nextDouble() * h;

            double cost = rand.nextDouble() * 100;

            e.addRect(new RectangularObstacle(sx, sy, sz, sx + ow, sy + ol, sz + oh, cost));
        }

        return e;
    }
}
