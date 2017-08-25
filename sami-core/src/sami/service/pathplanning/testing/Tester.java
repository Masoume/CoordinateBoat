package sami.service.pathplanning.testing;

import sami.environment.Continuous3DEnvironment;
import sami.environment.EnvironmentModel;
import sami.environment.GridEnvironment;
import sami.path.DestinationObjective;
import sami.path.ObjectiveFunction;
import sami.path.Path;
import sami.path.VehicleModel;
import sami.service.pathplanning.PlanningService;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceRequest;
import sami.service.pathplanning.PlanningServiceResponse;
import sami.service.pathplanning.algorithm.PRM;
import sami.service.pathplanning.algorithm.PotentialField;
import sami.service.pathplanning.algorithm.RRT;
import sami.service.pathplanning.vehicle.SimpleVehicle;

/**
 *
 * @author pscerri
 */
public class Tester {

    private static PlanningServiceListenerInt l = new PlanningServiceListenerInt() {
        @Override
        public void responseRecieved(PlanningServiceResponse response) {
            System.out.println("Cost was: " + response.getPath().getCost());
        }
    };

    public static void main(String[] argv) {
        System.out.println("Starting tester");

        potentialFieldTest();

        // PRMTest();
        // contEnvTest();
        //comparisonTest();
    }

    private static void comparisonTest() {



        EnvironmentModel e = EnvironmentFactory.createContinuous3DEnvironment();
        VehicleModel v = new SimpleVehicle(1.0);
        DestinationObjective of = new DestinationObjective(10, 10, 50, 50);

        System.out.println("Running RRT");
        RRT rrt = new RRT(e, v, of, l, 1);

        System.out.println("Running PRM");
        PRM prm = new PRM(e, v, of, l, 1, 200, 0.6);
    }

    private static void potentialFieldTest() {
        EnvironmentModel e = EnvironmentFactory.createContinuous3DEnvironment(15);
        VehicleModel v = new SimpleVehicle(1.0);
        DestinationObjective of = new DestinationObjective(10, 10, 50, 50);

        final PlanningServiceRequest request = new PlanningServiceRequest(e, of, v);

        PlanningServiceListenerInt l = new PlanningServiceListenerInt() {
            @Override
            public void responseRecieved(PlanningServiceResponse response) {
                System.out.println("Cost was: " + response.getPath().getCost());
                new Viewer(request.getEm(), response.getPath());
            }
        };

        PotentialField pf = new PotentialField(e, v, of, l, 1);

    }

    private static void contEnvTest() {
        EnvironmentModel e = EnvironmentFactory.createContinuous3DEnvironment();
        VehicleModel v = new SimpleVehicle(1.0);
        ObjectiveFunction of = new DestinationObjective(10, 10, 50, 50);

        PlanningServiceRequest request = new PlanningServiceRequest(e, of, v);

        genericTest(request, true);
    }

    private static void PRMTest() {
        EnvironmentModel e = new Continuous3DEnvironment(100.0, 100.0, 100.0);
        VehicleModel v = new SimpleVehicle(1.0);
        ObjectiveFunction of = new DestinationObjective(10, 10, 50, 50);

        PlanningServiceRequest request = new PlanningServiceRequest(e, of, v);

        genericTest(request, true);
    }

    private static void AStarTest() {

        double[][] values = new double[10][10];
        GridEnvironment ge = new GridEnvironment(values);

        ObjectiveFunction of = new DestinationObjective(4, 99);
        VehicleModel vm = new SimpleVehicle(1.0);

        PlanningServiceRequest request = new PlanningServiceRequest(ge, of, vm);
        final int noOptions = 3;
        request.setNoOptions(noOptions);

        genericTest(request, true);
    }

    private static void genericTest(final PlanningServiceRequest request, final boolean show) {


        PlanningServiceListenerInt l = new PlanningServiceListenerInt() {
            @Override
            public void responseRecieved(PlanningServiceResponse response) {

                Path p = response.getPath();

                if (show) {
                    new Viewer(request.getEm(), response.getPath());
                }

                System.out.println("Path is: " + p);

                if (response.getAlternatives().size() > 0) {
                    System.out.println("Alternatives");
                    for (Path path : response.getAlternatives()) {
                        System.out.println("Alternative: " + path);
                    }
                    System.out.println("End alternatives");
                }
            }
        };

        PlanningService.submitRequest(request, l);
    }
}
