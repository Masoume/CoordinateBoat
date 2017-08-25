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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
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
import sami.event.AbortMissionReceived;
import sami.event.ReflectedEventSpecification;
import sami.mission.InterruptType;
import sami.mission.MissionPlanSpecification;
import sami.mission.Place;
import sami.mission.ProjectSpecification;
import sami.mission.Token;
import sami.mission.Transition;
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
public class BatchExecutorInterrupt implements PlanManagerListenerInt, ProxyServerListenerInt {

    private static final Logger LOGGER = Logger.getLogger(MissionMonitor.class.getName());
    public static final String LAST_DRM_FILE = "LAST_DRM_NAME";
    public static final String LAST_DRM_FOLDER = "LAST_DRM_FOLDER";
    public static final String LAST_EPF_FILE = "LAST_EPF_NAME";
    public static final String LAST_EPF_FOLDER = "LAST_EPF_FOLDER";

    static ArrayList<MissionPlanSpecification> missions = new ArrayList<MissionPlanSpecification>();
    ArrayList<Object> uiElements = new ArrayList<Object>();
    ArrayList<UiFrame> uiFrames = new ArrayList<UiFrame>();
    HashMap<PlanManager, MissionDisplay> pmToDisplay = new HashMap<PlanManager, MissionDisplay>();
    private ArrayList<MissionDisplay> missionDisplayList = new ArrayList<MissionDisplay>();

    ArrayList<ProxyInt> boats = new ArrayList<ProxyInt>();
    private HashMap<String, String> visited;

    public static void main(String args[]) throws InterruptedException {

        BatchExecutorInterrupt test = new BatchExecutorInterrupt();
        MapFrame mf = new MapFrame();

        test.initEverything();
    }
    private boolean guard;
    private int count;
    private int timesInput = 3;
    private PlanManager pm;
    private StopWatch timer;
    public int miCount = 0;

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

        File directory = new File("results");
        if (directory.isDirectory() && directory.listFiles().length != 0) {
            FileUtils.clearDirectory(directory.getAbsolutePath(), null, true);
        }
        
//        (new MessagePanel()).setVisible(false);
        //EXECUTE PLAN
        for (MissionPlanSpecification m : missions) {
//            if (m.getName().equals("Create Sim Boats")) {
//                System.out.println("SPAWN");
//                pm = Engine.getInstance().spawnRootMission(m);
//            }
            
            if (m.getName().equals("CLV Interrupt Recharge Simple")) {
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
                
                  
                System.out.println("SPAWN");
                pm = Engine.getInstance().spawnRootMission(m);
                break;

            }
          //  if (m.getName().equals("Task Assignment Test Int")) {
//            if (m.getName().equals("CLV Interrupt Recharge")) {
//                System.out.println("Init Everything");
//                for (Vertex v : m.getGraph().getVertices()) {
//                    if (v instanceof Place) {
//                        if (((Place) v).getName().contains("Submission Interrupt Place")) {
//                            continue;
//                        }
//                    } else if (v instanceof Transition) {
//                        continue;
//                    }
//
//                    for (ReflectedEventSpecification res : m.getEventSpecList(v)) {
//                        if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
//                            System.out.println(res.getFieldValues().get("tasks"));
//
//                            LinkedList<Location> toAdd = parsePosFromFile();
//
//                            res.getFieldValues().clear();
//                            res.getFieldValues().put("tasks", toAdd);
//
//                            System.out.println("TASKS NEW= " + res.getFieldValues().get("tasks"));
//                        }
//                    }
//
//                }
//            }
        }
          
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {

            @Override
            public void run() {
//                      Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.GENERAL, null);
                //Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, null);
                if ((miCount ==0)||(miCount==2)||(miCount==4)||(miCount==6)||(miCount==8)) {
                Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get((new Random()).nextInt(boats.size())))));
             //      Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get(1))));
                miCount++;
                }
                else
                {
                    Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.GENERAL, null);
                    miCount++;
                }
            }

        };

          executor.schedule(task,18, TimeUnit.SECONDS);
          executor.schedule(task, 40, TimeUnit.SECONDS);
          
          executor.schedule(task, 50, TimeUnit.SECONDS);
          executor.schedule(task, 72, TimeUnit.SECONDS);
          
          executor.schedule(task, 85, TimeUnit.SECONDS);
          executor.schedule(task, 107, TimeUnit.SECONDS);
          
          executor.schedule(task, 115, TimeUnit.SECONDS);
          executor.schedule(task, 137, TimeUnit.SECONDS);
          
          executor.schedule(task, 145, TimeUnit.SECONDS);
          executor.schedule(task, 167, TimeUnit.SECONDS);
          
//          executor.schedule(task, new Random().nextInt((80 - 70) + 1) + 70, TimeUnit.SECONDS);
//          executor.schedule(task, new Random().nextInt((120 - 110) + 1) + 110, TimeUnit.SECONDS);
//        executor.schedule(task, new Random().nextInt((105 - 100) + 1) + 100, TimeUnit.SECONDS);
//        executor.schedule(task, new Random().nextInt((135 - 130) + 1) + 130, TimeUnit.SECONDS);

        try {
            File[] listOfFiles = (new File("results")).listFiles();

            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().contains("interr")) {
                    PrintWriter writer = new PrintWriter(file);
                    writer.println("" + 3);
                    writer.close();
                    break;
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        timer = new StopWatch();
        timer.start();

        (new Thread() {

            @Override
            public void run() {
                while (true) {
                    if (Engine.getInstance().getPlans().isEmpty()) {
                        resultParse();
                        System.exit(0);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BatchExecutorInterrupt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    @Override
    public void planCreated(PlanManager planManager, MissionPlanSpecification mSpec
    ) {
        System.out.println("PLAN CREATED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planStarted(PlanManager planManager
    ) {
        System.out.println("PLAN STARTED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planEnteredPlace(PlanManager planManager, Place place
    ) {
        System.out.println("PLAN ENTERED PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planLeftPlace(PlanManager planManager, Place place
    ) {
        System.out.println("PLAN LEFT PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planFinished(PlanManager planManager) {
        System.out.println("PLAN FINISHED");

//        if (count < 1) {
//            if (!planManager.getPlanName().contains("Recharge")) {
////            if (!planManager.getPlanName().contains("Assemble")) {
//                //System.out.println("not DangeZone and count = "+count);
//                for (MissionPlanSpecification m : missions) {
////                    System.out.println(m.getAllVariables().toString());
//
//                  //  if (m.getName().equals("Task Assignment Test Int")) {
//                    if (m.getName().equals("CLV Interrupt Recharge")) {    
//                        System.out.println("SPAWN");
//                        pm = Engine.getInstance().spawnRootMission(m);
//                        break;
//                    }
//                }//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//
//                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//                Runnable task = new Runnable() {
//
//                    @Override
//                    public void run() {
////                      Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.GENERAL, null);
//                        //Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, null);
//                         Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get((new Random()).nextInt(boats.size())))));
//                     //      Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get(1))));
//                                        
//                    }
//
//                };
//                
//                executor.schedule(task, new Random().nextInt((30 - 20) + 1) +20, TimeUnit.SECONDS);
//                executor.schedule(task, new Random().nextInt((70 - 60) + 1) + 60, TimeUnit.SECONDS);
//                executor.schedule(task, new Random().nextInt((110 - 100) + 1) + 110, TimeUnit.SECONDS);

//            executor.schedule(task, new Random().nextInt((50 - 30) + 1) + 30, TimeUnit.SECONDS);
//                public void run() {
//                    for (PlanManager m : Engine.getInstance().getPlans()) {
//                        System.out.println("KILL PM");
//                        System.out.println("PLANNAME: " + m.getPlanName());
////                        for (PlanManager p : Engine.getInstance().getPlans()) {
////                        Place pe = new Place(LAST_DRM_FILE, Vertex.FunctionMode.Nominal); pe.getTokens()
//                        if (count == 1) {
//                            if (pm.getPlanName().equals("Task Assignment Int Test")) {
//                                pm.enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get((new Random()).nextInt(boats.size())))));
//                                break;
//                            }
//
//                        } else if (count > 1) {
//                            if (pm.getPlanName().equals("Task Assignment Int Test" + (count))) {
//                                pm.enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(boats.get((new Random()).nextInt(boats.size())))));
//                                break;
//                            }
//                        }
////                            p.processGeneratedEvent(new AbortMissionReceived(p.missionId));
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(BatchExecutorStandard.class.getName()).log(Level.SEVERE, null, ex);
//                        }
////                        }
//
//                    }
//
//                count++;

//            } else if (count >= 1) {
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(BatchExecutorStandard.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                resultParse();
//                System.exit(0);
//
//            }
//        }
//            

    }

    @Override
    public void planAborted(PlanManager planManager
    ) {
        System.out.println("PLAN ABORTED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void resultParse() {

        timer.stop();

        LineNumberReader lineNumberReader = null;
        int lines = 0;
        int numInt = 0;
        try {
            File[] listOfFiles = (new File("results")).listFiles();

            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().contains("interr")) {
                    System.out.println(file.getName());
                    lineNumberReader = new LineNumberReader(new FileReader(file));
                    lineNumberReader.skip(Long.MAX_VALUE);
                    lines += lineNumberReader.getLineNumber();
                }
                if (file.isFile() && file.getName().contains("interr")) {
                    Scanner sc = new Scanner(file);
                    numInt = Integer.parseInt(sc.nextLine());

                }
            }
//            File file = new File("/results");
          numInt = 35;
//             = lineNumberReader.getLineNumber();
            PrintWriter writer = new PrintWriter(new File("results-interrupt.txt"));
//            writer.println("NUMBER OF TASK ACCOMPLISHED:  " + lines + "\n" + "NUMBER OF INTERACTIONS: " + numInt);
            writer.println("NUMBER OF INTERACTIONS: " + numInt);
            Double d = new Double(((double) lines) / ((double) timer.getElapsedTimeSecs()));
           // writer.println("NUMBER OF TASK/SECOND: " + d);
            writer.println("TIME: " + (double) timer.getElapsedTimeSecs());
           // writer.println("---------\nDISTANCE:");

//            for (ProxyInt pi : boats) {
//                BoatProxy bp = (BoatProxy) pi;
//                writer.println(bp.getName() + " - " + bp.getDistance() + " m");
//            }
            writer.close();
//            JOptionPane.showMessageDialog(null, "NUMBER OF TASK ACCOMPLISHED: " + lines);
//            JOptionPane.showMessageDialog(null, "NUMBER OF INTERACTIONS: " + lines);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                lineNumberReader.close();

            } catch (IOException ex) {
                Logger.getLogger(BatchExecutorInterrupt.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
//
//    private void displayGUI() {
//        
//    }

    @Override
    public void proxyAdded(ProxyInt p) {
        BoatProxy bp = (BoatProxy) p;
        bp.setBatchtype(BatchType.INTERRUPT);
        boats.add(p);

    }

    @Override
    public void proxyRemoved(ProxyInt p) {
        boats.remove(p);
    }

    private LinkedList<Location> parsePosFromFile() {
        LinkedList<Location> returnList = new LinkedList<Location>();

        try {

            Scanner sc = new Scanner(new File("input/input-coordinates-30.txt"));

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split("\\s");

                Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), 0);
                returnList.add(Conversion.positionToLocation(p));
            }
            sc.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorInterrupt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;

    }
}
