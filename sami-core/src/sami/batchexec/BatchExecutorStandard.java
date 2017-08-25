/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.batchexec;

import com.perc.utils.other.FileUtils;
import crw.Conversion;
import crw.proxy.BoatProxy;
import crw.ui.MapFrame;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import sami.engine.Engine;
import sami.engine.PlanManager;
import sami.engine.PlanManagerListenerInt;
import sami.event.AbortMission;
import sami.event.AbortMissionReceived;
import sami.event.ReflectedEventSpecification;
import sami.mission.InterruptType;
import sami.mission.MissionPlanSpecification;
import sami.mission.Place;
import sami.mission.ProjectSpecification;
import sami.mission.Vertex;
import sami.path.Location;
import sami.proxy.ProxyInt;
import sami.proxy.ProxyServerListenerInt;
import sami.service.ServiceServer;
import sami.ui.MissionDisplay;
import sami.ui.MissionMonitor;
import static sami.ui.MissionMonitor.LAST_DRM_FILE;
import static sami.ui.MissionMonitor.LAST_DRM_FOLDER;
import sami.uilanguage.LocalUiClientServer;
import sami.uilanguage.UiFrame;

/**
 *
 * @author Nicolò Marchi <marchi.nicolo@gmail.com>
 */
public class BatchExecutorStandard implements PlanManagerListenerInt, ProxyServerListenerInt {

    private static final Logger LOGGER = Logger.getLogger(MissionMonitor.class.getName());
    public static final String LAST_DRM_FILE = "LAST_DRM_NAME";
    public static final String LAST_DRM_FOLDER = "LAST_DRM_FOLDER";
    public static final String LAST_EPF_FILE = "LAST_EPF_NAME";
    public static final String LAST_EPF_FOLDER = "LAST_EPF_FOLDER";

    static ArrayList<MissionPlanSpecification> missions = new ArrayList<MissionPlanSpecification>();

    private StopWatch timer;
    private int count;
    private static int timesInput = 4;

    public static void main(String args[]) throws InterruptedException {

        BatchExecutorStandard test = new BatchExecutorStandard();

//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new MissionMonitor().setVisible(true);
//            }
//        });
        MapFrame mf = new MapFrame();

//        timesInput = Integer.parseInt(args[1]);
        test.initEverything();
    }
    private HashMap<String, String> visited;
    private ArrayList<ProxyInt> boats = new ArrayList<ProxyInt>();
    private PlanManager pm;

    public void initEverything() throws InterruptedException {

        LocalUiClientServer clientServer = new LocalUiClientServer();
        Engine.getInstance().setUiClient(clientServer);
//        Engine.getInstance().setUiServer(clientServer);
        
        // Set Engine singleton's services server
        Engine.getInstance().setServiceServer(new ServiceServer());
        Engine.getInstance().getProxyServer().addListener(this);

        //RETRIEVE PLANS
        File drmFile = null;

        // Try to load the last used DRM file
        Preferences p = Preferences.userRoot();
        try {
            String lastDrmPath = p.get(LAST_DRM_FILE, null);
            if (lastDrmPath != null) {
                drmFile = new File(lastDrmPath);
            }
        } catch (AccessControlException e) {
            LOGGER.severe("Failed to load last used DRM");
        }

        if (drmFile == null) {
//            Preferences p = Preferences.userRoot();
            String lastDrmFolder = p.get(LAST_DRM_FOLDER, "");
            JFileChooser chooser = new JFileChooser(lastDrmFolder);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("DREAAM specification files", "drm");
            chooser.setFileFilter(filter);
            int ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                drmFile = chooser.getSelectedFile();
            }
        }

        if (drmFile == null) {
            return;
        }
        ProjectSpecification projectSpec = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(drmFile));
            projectSpec = (ProjectSpecification) ois.readObject();

            LOGGER.info("Reading project specification at [" + drmFile + "]");

            if (projectSpec == null) {
                LOGGER.log(Level.WARNING, "Failed to load project specification at [" + drmFile + "]");
                JOptionPane.showMessageDialog(null, "Specification failed load");
            } else {
                // Add root missions
                missions = projectSpec.getRootMissionPlans();

                for (String variable : projectSpec.getGlobalVariableToValue().keySet()) {
                    Engine.getInstance().setVariableValue(variable, projectSpec.getGlobalVariableValue(variable));
                }

                try {
                    p.put(LAST_DRM_FILE, drmFile.getAbsolutePath());
                    p.put(LAST_DRM_FOLDER, drmFile.getParent());
                } catch (AccessControlException e) {
                    LOGGER.severe("Failed to save preferences");
                }
            }

        } catch (ClassNotFoundException ex) {
            LOGGER.severe("Class not found exception in DRM load");
        } catch (FileNotFoundException ex) {
            LOGGER.severe("DRM File not found");
        } catch (IOException ex) {
            LOGGER.severe("IO Exception on DRM load");
        }

        Engine.getInstance().addListener(this);

        System.out.println("BATCHEXECUTOR STANDARD");

        File directory = new File("results");
        if (directory.isDirectory() && directory.listFiles().length != 0) {
            FileUtils.clearDirectory(directory.getAbsolutePath(), null, true);
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {

            public void run() {

//                for (ProxyInt b : boats) {
//                    b.abortMission(pm.missionId);
//                }
//                pm.processGeneratedEvent(new AbortMissionReceived(pm.missionId));
              //  pm.eventGenerated(new AbortMissionReceived(pm.missionId));
                
                Engine.getInstance().getPlans().get(0).abortMission();
            }
        };
        executor.schedule(task, new Random().nextInt((30 - 20) + 1) + 20, TimeUnit.SECONDS);
        executor.schedule(task, new Random().nextInt((55 - 45) + 1) + 45, TimeUnit.SECONDS);
        executor.schedule(task, new Random().nextInt((85 - 75) + 1) + 75, TimeUnit.SECONDS);
//        executor.schedule(task, new Random().nextInt((120 - 110) + 1) + 110, TimeUnit.SECONDS);
//        executor.schedule(task, new Random().nextInt((150 - 140) + 1) + 140, TimeUnit.SECONDS);

        for (MissionPlanSpecification m : missions) {
            if (m.getName().equals("Create Sim Boats")) {
                System.out.println("SPAWN");
                pm = Engine.getInstance().spawnRootMission(m);
            }

            if (m.getName().equals("CLV Standard Recharge")) {
                for (Vertex v : m.getGraph().getVertices()) {
                    if (v instanceof Place){
                        if (((Place) v).getName().contains("Init")) {//Tasks should be read from file
                            for (ReflectedEventSpecification res : m.getEventSpecList(v)) {
                                if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
                                    System.out.println("Tasks = " + res.getFieldValues().get("tasks"));
                                    LinkedList<Location> toAdd = parsePosFromFile();
                                    res.getFieldValues().clear();
                                    res.getFieldValues().put("tasks", toAdd);
                                    System.out.println("New tasks = " + res.getFieldValues().get("tasks"));
                                }
                            }
                        }
                    }
                }
                
//                System.out.println("SPAWN");
//                pm = Engine.getInstance().spawnRootMission(m);
//                break;
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new File("results/interr.txt"));
            writer.println("" + 3);
            writer.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        timer = new StopWatch();
        timer.start();

//        (new Thread() {
//
//            @Override
//            public void run() {
//                while (true) {
//                    System.out.println("EMPTY?: " + Engine.getInstance().getPlans());
//                    if (Engine.getInstance().getPlans().isEmpty()) {
//
//                    }
//                    try {
//                        Thread.sleep(2000);
//
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(BatchExecutorStandard.class
//                                .getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }).start();
    }

    @Override
    public void planCreated(PlanManager planManager, MissionPlanSpecification mSpec) {
        System.out.println("PLAN CREATED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planStarted(PlanManager planManager) {
        System.out.println("PLAN STARTED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planEnteredPlace(PlanManager planManager, Place place) {
        System.out.println("PLAN ENTERED PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planLeftPlace(PlanManager planManager, Place place) {
        System.out.println("PLAN LEFT PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planFinished(PlanManager planManager) {
        System.out.println("PLAN FINISHED");
        System.out.println("PLAN1: " + planManager.getPlanName() + " - " + pm.getPlanName());
        
        if(planManager.getPlanName().contains("CLV Standard Recharge") && pm.getPlanName().contains("CLV Standard Recharge")){
            resultParse();
            System.exit(0);
        }
        if (planManager.getPlanName().contains("CLV Standard Recharge") && pm.getPlanName().contains("mRecharge")
                || planManager.getPlanName().contains("Create Sim") && pm.getPlanName().contains("Create Sim")
                || planManager.getPlanName().contains("mRecharge") && pm.getPlanName().contains("mRecharge")){

        
                for (MissionPlanSpecification m : missions) {
//
                   
                    if (m.getName().equals("CLV Standard Recharge")) {
                        LinkedList<Location> tasks = null;
                        for (Vertex v : m.getGraph().getVertices()) {
                            if (v instanceof Place){
                                if (((Place) v).getName().contains("Init")) {//Tasks should be read from file
                                    for (ReflectedEventSpecification res : m.getEventSpecList(v)) {
                                        if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
                                            tasks = (LinkedList<Location>) res.getFieldValues().get("tasks");
                                            System.out.println("old tasks size: "+tasks.size());
                                            
                                            LinkedList<Location> toRemove = new LinkedList<Location>(parseVisited());

                                            System.out.println("remove size: "+toRemove.size());
                                            
                                            tasks.removeAll(toRemove);
                                            if (tasks.isEmpty()) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    
//                    
////                if (m.getName().equals("CLV Standard Recharge")) {
////                    LinkedList<Location> tasks = null;
////
////                    for (Vertex ve : m.getGraph().getVertices()) {
////                        for (ReflectedEventSpecification res : ve.getEventSpecs()) {
////
////                            if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
////                                tasks = (LinkedList<Location>) res.getFieldValues().get("tasks");
////
////                                LinkedList<Location> toRemove = new LinkedList<Location>(parseVisited());
////
////                                tasks.removeAll(toRemove);
////                                if (tasks.isEmpty()) {
////                                    return;
////                                }
//////                                res.getFieldValues().put("tasks", tasks);
////                            }
////                        }
////                    }

                    try {
                        PrintWriter writer = new PrintWriter(new File("results/interr" + UUID.randomUUID().getMostSignificantBits() + ".txt"));
                        writer.println("" + 2);
                        writer.close();

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(BatchExecutorStandard.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
//                    if (tasks != null && !tasks.isEmpty()) {

                    System.out.println("SPAWN");
                    pm = Engine.getInstance().spawnRootMission(m);
                    System.out.println("IO HO LANCIATO: " + pm);

                    break;

                }
            }
        }

    }

    @Override
    public void planAborted(PlanManager planManager) {
        System.out.println("PLAN ABORTED");
        try {
            Thread.sleep(1000);

        } catch (InterruptedException ex) {
            Logger.getLogger(BatchExecutorStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        for (MissionPlanSpecification m : missions) {
            if (m.getName().contains("mRecharge")) {
                System.out.println("SPAWN Recharge");
                pm = Engine.getInstance().spawnRootMission(m);
            }
        }
//        planFinished(null);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void resultParse() {

        timer.stop();
//        System.out.println("TIME: "+timer.getElapsedTimeSecs());
        LineNumberReader lineNumberReader = null;
        int lines = 0;
        int numInt = 0;
        int noInterr = 3;
        try {
            File[] listOfFiles = (new File("results")).listFiles();

            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().contains("interr") && !file.getName().contains("reassign")) {
                    Scanner sc = new Scanner(file);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        visited.put(line, line);
                    }

                }
                if (file.isFile() && file.getName().contains("interr")) {
                    Scanner sc = new Scanner(file);
                    numInt += Integer.parseInt(sc.nextLine());

                }
                
                if (file.isFile() && file.getName().contains("reassign")) {
                    
                    Scanner sc = new Scanner(file);
                    while (sc.hasNextInt()) {
                        noInterr += sc.nextInt();
                    }
                }
//                lineNumberReader.close();
            }
//            File file = new File("/results");
//
//             = lineNumberReader.getLineNumber();

//            JOptionPane.showMessageDialog(null, getPanel(lines, numInt), "", JOptionPane.INFORMATION_MESSAGE);
            PrintWriter writer = new PrintWriter(new File("results-standard.txt"));
            writer.println("NUMBER OF TASK ACCOMPLISHED:  " + visited.size() + "\n" + "NUMBER OF INTERACTIONS: " + numInt);
            writer.println("NUMBER OF INTERACTIONS ReAssign: " + noInterr);
//            Double d = ((double) visited.size()) / (timer.getElapsedTimeSecs());
//            writer.println("NUMBER OF TASK/SECOND: " + d);
            writer.println("TIME: " + (double) timer.getElapsedTimeSecs());
//            writer.println("---------\nDISTANCE:");
//
//            for (ProxyInt pi : boats) {
//                BoatProxy bp = (BoatProxy) pi;
//                writer.println(bp.getName() + " - " + bp.getDistance() + " m");
//            }
            writer.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<Location> parseVisited() {

        Set<Location> ret = new HashSet<Location>();
//        HashMap<Location, Location> retMap = new HashMap<Location, Location>();

        visited = new HashMap<String, String>();
        try {
            File[] listOfFiles = (new File("results")).listFiles();

            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().contains("interr") && !file.getName().contains("reassign")) {
                    Scanner sc = new Scanner(file);

                    while (sc.hasNextLine()) {

                        String line = sc.nextLine();

//                        if(!visited.containsKey(line)){
                        visited.put(line, line);
//                        }

                        line = line.replace("[", " ").replace("]", " ");
                        String[] tokens = line.split("\\s");

                        String latitude = tokens[4];
                        String longitude = tokens[5];
                        latitude = latitude.replace(",", "").replace("°", "").replace("(", "").replace(")", "");
                        longitude = longitude.replace(",", "").replace("°", "").replace("(", "").replace(")", "");
                        System.out.println("PRINT POS= " + latitude + " - " + longitude);

                        Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(latitude)), Angle.fromDegreesLongitude(Double.parseDouble(longitude)), 0);
                        ret.add(Conversion.positionToLocation(p));
//                        retMap.put(Conversion.po, null)
                    }

                    sc.close();

                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchExecutorStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }

    @Override
    public void proxyAdded(ProxyInt p) {
        BoatProxy bp = (BoatProxy) p;
        bp.setBatchtype(BatchType.STANDARD);
        boats.add(p);

    }

    @Override
    public void proxyRemoved(ProxyInt p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private LinkedList<Location> parsePosFromFile() {
        LinkedList<Location> returnList = new LinkedList<Location>();

        try {

            Scanner sc = new Scanner(new File("input/input-coordinates.txt"));

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split("\\s");

                Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), 0);
                returnList.add(Conversion.positionToLocation(p));
            }
            sc.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;

    }

}
