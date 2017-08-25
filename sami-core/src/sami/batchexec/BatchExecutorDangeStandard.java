/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sami.batchexec;

import com.perc.utils.other.FileUtils;
import crw.Conversion;
import crw.event.output.proxy.ProxyEmergencyAbort;
import crw.event.output.proxy.ProxyExecutePath;
import crw.handler.ProxyEventHandler;
import crw.proxy.BoatProxy;
import crw.ui.BoatMarker;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import static sami.batchexec.BatchExecutorStandard.missions;
import sami.engine.Engine;
import sami.engine.PlanManager;
import sami.engine.PlanManagerListenerInt;
import sami.event.AbortMissionReceived;
import sami.event.ReflectedEventSpecification;
import sami.mission.InterruptType;
import sami.mission.MissionPlanSpecification;
import sami.mission.Place;
import sami.mission.ProjectSpecification;
import sami.mission.Transition;
import sami.mission.Vertex;
import sami.path.Location;
import sami.path.Path;
import sami.path.PathUtm;
import sami.proxy.ProxyInt;
import sami.proxy.ProxyServerListenerInt;
import sami.service.ServiceServer;
import sami.ui.MissionDisplay;
import sami.ui.MissionMonitor;
import sami.uilanguage.LocalUiClientServer;
import sami.uilanguage.UiFrame;



/**
 *
 * @author Masoume
 */
public class BatchExecutorDangeStandard implements PlanManagerListenerInt, ProxyServerListenerInt{
  
    private static final Logger LOGGER = Logger.getLogger(MissionMonitor.class.getName());
    public static final String LAST_DRM_FILE = "LAST_DRM_NAME";
    public static final String LAST_DRM_FOLDER = "LAST_DRM_FOLDER";
    public static final String LAST_EPF_FILE = "LAST_EPF_NAME";
    public static final String LAST_EPF_FOLDER = "LAST_EPF_FOLDER";

    static ArrayList<MissionPlanSpecification> missions = new ArrayList<MissionPlanSpecification>();
    ArrayList<Object> uiElements = new ArrayList<Object>();
    ArrayList<UiFrame> uiFrames = new ArrayList<UiFrame>();
    HashMap<PlanManager, MissionDisplay> pmToDisplay = new HashMap<PlanManager, MissionDisplay>();
 //   private ArrayList<MissionDisplay> missionDisplayList = new ArrayList<MissionDisplay>();
 //   private HashMap<BoatMarker, ArrayList<Position>> decisions = new HashMap<BoatMarker, ArrayList<Position>>();
    ArrayList<ProxyInt> boats = new ArrayList<ProxyInt>();
    private HashMap<String, String> visited;                   
    
    public static void main(String args[]) throws InterruptedException, IOException {

        BatchExecutorDangeStandard test = new BatchExecutorDangeStandard();
        test.initEverything();
    }
   
    private int count;
    private StopWatch timer;
    private PlanManager pm;
    private int noInterr = 3; 
    public int interCount;
    public int noTasks = 30;
//    final ProxyInt foundB = boats.get((new Random()).nextInt(boats.size())); 

    public void initEverything() throws InterruptedException, IOException {
        
        System.out.println(">> Init Everything");

        LocalUiClientServer clientServer = new LocalUiClientServer();
        Engine.getInstance().setUiClient(clientServer);

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
        
        
        
        //EXECUTE PLAN
        for (MissionPlanSpecification m : missions) {
            if (m.getName().equals("Create Sim Boats")) {
                System.out.println("SPAWN &Create Sim Boats&");
                pm = Engine.getInstance().spawnRootMission(m);
            }
            //if (m.getName().equals("Task Assignment Test Int")) {
//            if (m.getName().equals("CLV New Interrupt")) {
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
//                        //Tasks should be read from file
//                        if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
//                            System.out.println("Tasks = " + res.getFieldValues().get("tasks"));
////                            System.out.println("TASKS1= " + Conversion.locationToPosition(new ArrayList<Location>((LinkedList<Location>) res.getFieldValues().get("tasks"))));
//
//                            LinkedList<Location> toAdd = parsePosFromFile(false);
//
//                            res.getFieldValues().clear();
//                            res.getFieldValues().put("tasks", toAdd);
//                    
//                            System.out.println("New tasks = " + res.getFieldValues().get("tasks"));
//                        }
//                        //Area points should be read from file
//                        if (res.getClassName().equals("crw.event.output.operator.OperatorSelectDangerousArea")) {
//                            
//                            System.out.println("Area = " + res.getFieldValues().get("area"));
//                            
//                            LinkedList<Location> areaPoints = parsePosFromFile(true);//should be completed
//                            
//                            res.getFieldValues().clear();
//                            res.getFieldValues().put("area",areaPoints);
//                            
//                            System.out.println("New area = " + res.getFieldValues().get("area"));
//                        }
//                    }
//
//                }
//            }
        }

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
            Logger.getLogger(BatchExecutorDangeInterrupt.class
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
                        Logger.getLogger(BatchExecutorDangeInterrupt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    @Override
    public void planCreated(PlanManager planManager, MissionPlanSpecification mSpec
    ) {
        System.out.println("PLAN &"+ planManager.getPlanName() + "& CREATED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planStarted(PlanManager planManager
    ) {
        System.out.println("PLAN &"+ planManager.getPlanName() + "& STARTED");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planEnteredPlace(PlanManager planManager, Place place
    ) {
        System.out.println("PLAN &"+ planManager.getPlanName() + "& ENTERED PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planLeftPlace(PlanManager planManager, Place place
    ) {
        System.out.println("PLAN &"+ planManager.getPlanName() + "& LEFT PLACE");

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void planFinished(PlanManager planManager) {
        
        System.out.println("PLAN &"+ planManager.getPlanName() + "& FINISHED");

        if (count < 1) {
                for (MissionPlanSpecification m : missions) {
//                    if (m.getName().equals("CLV Seq TeleOperation")) { 
                    if (m.getName().equals("TeleOp")){
                        System.out.println("SPAWN &CLV Seq TeleOperation&");
                        pm = Engine.getInstance().spawnRootMission(m);
                        break;
                    }
                }
                
                final ProxyInt foundB1 = boats.get(0);
                final ProxyInt foundB2 = boats.get(1);
                final ProxyInt foundB3 = boats.get(2);
//                final ProxyInt foundB4 = boats.get(3);
//                final ProxyInt foundB5 = boats.get(4);
                
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    Runnable task = new Runnable() {

                    public void run() {
                        
                       interCount++;
                       switch (interCount){
                           case 1:
                              Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB3)));
                              break;
                           case 2:
                               //Count no. of interactions
                               LinkedList<Location> alreadDone = new LinkedList<Location>(parseVisited());
                               noInterr =  noInterr + noTasks - alreadDone.size();
                               System.out.println("No of tasks already done: "+alreadDone.size());
                               
                               ArrayList<ProxyInt> ShouldbeStopped1 = new ArrayList<ProxyInt>();
                               ShouldbeStopped1.add(foundB1);
                               ShouldbeStopped1.add(foundB2);
//                               ShouldbeStopped1.add(foundB3);
//                               ShouldbeStopped1.add(foundB2);
                               
                               Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, ShouldbeStopped1);
                               break;                
                       }
                        
                    //    BoatProxy foundedB = (BoatProxy)boats.get((new Random()).nextInt(boats.size())); 

                    }
                };
                
                executor.schedule(task,25, TimeUnit.SECONDS);//b2&b3 should be stopped
                executor.schedule(task, 32, TimeUnit.SECONDS);//b2 should start again
       //         executor.schedule(task, 55, TimeUnit.SECONDS);//b3 should start
//                
//                    executor.schedule(task, new Random().nextInt((40 - 30) + 1) + 30, TimeUnit.SECONDS);
//                    executor.schedule(task, new Random().nextInt((60 - 50) + 1) + 50, TimeUnit.SECONDS);
//                
                count++;
         } 
    }

    @Override
    public void planAborted(PlanManager planManager
    ) {
        System.out.println("PLAN &"+ planManager.getPlanName() + "& ABORTED");

        try {
            Thread.sleep(3000);

        } catch (InterruptedException ex) {
            Logger.getLogger(BatchExecutorDangeStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        for (MissionPlanSpecification m : missions) {
            
            if (m.getName().contains("CLV Seq TeleOperation")) {

                LinkedList<Location> tasks = null;

                for (Vertex ve : m.getGraph().getVertices()) {
                    for (ReflectedEventSpecification res : ve.getEventSpecs()) {

                        if (res.getClassName().equals("crw.event.output.service.TasksAssignmentRequest")) {
                            tasks = (LinkedList<Location>) res.getFieldValues().get("tasks");
                            System.out.println("Tasks before remove: "+tasks);
                            
                            
                            LinkedList<Location> toRemove = new LinkedList<Location>(parseVisited());

                            tasks.removeAll(toRemove);
                            noInterr =  noInterr + tasks.size();
                            System.out.println("Tasks after remove: "+res.getFieldValues().get("tasks"));

                            if (tasks.isEmpty()) {
                                return;
                            }
                        }
                    }
                }

                try {
                    PrintWriter writer = new PrintWriter(new File("results/interr" + UUID.randomUUID().getMostSignificantBits() + ".txt"));
                    writer.println("" + 2);
                    writer.close();

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(BatchExecutorStandard.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Thread.sleep(1000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(BatchExecutorDangeStandard.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("SPAWN &"+m.getName()+"&");
                pm = Engine.getInstance().spawnRootMission(m);

                break;
            }
        }
    }

    private Set<Location> parseVisited() {

        Set<Location> ret = new HashSet<Location>();
//        HashMap<Location, Location> retMap = new HashMap<Location, Location>();

        visited = new HashMap<String, String>();
        try {
            File[] listOfFiles = (new File("results")).listFiles();

            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().contains("interr")) {
                    Scanner sc = new Scanner(file);

                    while (sc.hasNextLine()) {

                        String line = sc.nextLine();

                        visited.put(line, line);

                        line = line.replace("[", " ").replace("]", " ");
                        String[] tokens = line.split("\\s");

                        String latitude = tokens[4];
                        String longitude = tokens[5];
                        latitude = latitude.replace(",", "").replace("°", "").replace("(", "").replace(")", "");
                        longitude = longitude.replace(",", "").replace("°", "").replace("(", "").replace(")", "");
                        System.out.println("POS= " + latitude + " - " + longitude);

                        Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(latitude)), Angle.fromDegreesLongitude(Double.parseDouble(longitude)), 0);
                        ret.add(Conversion.positionToLocation(p));
//                        retMap.put(Conversion.po, null)
                    }

                    sc.close();

                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorDangeStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchExecutorDangeStandard.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
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
//
//             = lineNumberReader.getLineNumber();
            PrintWriter writer = new PrintWriter(new File("results-standard.txt"));
            writer.println("NUMBER OF TASK ACCOMPLISHED:  " + lines);//numInt
            writer.println("NUMBER OF INTERACTIONS: " + noInterr);//numInt
            Double d = new Double(((double) lines) / ((double) timer.getElapsedTimeSecs()));
        //    writer.println("NUMBER OF TASK/SECOND: " + d);
            writer.println("TIME: " + (double) timer.getElapsedTimeSecs());
            writer.println("---------\nDISTANCE:");
            

            for (ProxyInt pi : boats) {
                BoatProxy bp = (BoatProxy) pi;
                writer.println(bp.getName() + " - " + bp.getDistance() + " m");
            }
            writer.close();
//            JOptionPane.showMessageDialog(null, "NUMBER OF TASK ACCOMPLISHED: " + lines);
//            JOptionPane.showMessageDialog(null, "NUMBER OF INTERACTIONS: " + lines);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorDangeInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchExecutorDangeInterrupt.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                lineNumberReader.close();

            } catch (IOException ex) {
                Logger.getLogger(BatchExecutorDangeInterrupt.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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

    private LinkedList<Location> parsePosFromFile(boolean bFlag) {
        LinkedList<Location> returnList = new LinkedList<Location>();

        try {

            Scanner sc = new Scanner(new File("input/area-pos-test30.txt"));
            int iCount = 0;  
            if (bFlag)//read only the first 4 points which makes area
            {
                while (iCount<4) {
                    iCount++;
                    String line = sc.nextLine();
                    String[] split = line.split("\\s");

                    Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), 0);
                    returnList.add(Conversion.positionToLocation(p));
                }
                sc.close();
            }
            else
            {
                String line = sc.nextLine();
                iCount = 1;
                while (iCount<4)
                {
                    iCount++;
                    line = sc.nextLine();
                }
                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    String[] split = line.split("\\s");

                    Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), 0);
                    returnList.add(Conversion.positionToLocation(p));
                }
                sc.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorDangeInterrupt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;

    }
    
    public void ChangeVelocity(){
        
        System.out.println("==>ChangeVelocity");

        if (interCount>=2){
            
            for (MissionPlanSpecification m : missions) { 
                 if (m.getName().equals("CLV DangeArea Velocity")) {     
                     for (Vertex v : m.getGraph().getVertices()) {
                         if (v instanceof Transition) {
                             continue;
                         }

                         for (ReflectedEventSpecification res1 : m.getEventSpecList(v)) {
                             System.out.println("res1.getClassName()=>"+res1.getClassName());
                             if (res1.getClassName().equals("crw.event.output.proxy.SetVelocityMultiplier")) {
                                 System.out.println("Old Velocity = " + res1.getFieldValues().get("velocityMultiplier"));

//                                 if (res1.getFieldValues().get("velocityMultiplier").equals(0.0))
//                                 {
//                                     res1.getFieldValues().clear();
                                     res1.getFieldValues().put("velocityMultiplier", 1.0);
//                                 }
                                 System.out.println("New Velocity = " + res1.getFieldValues().get("velocityMultiplier"));
                               //  return;
                             }    
                         }
                     }
                 }
             } 
        }  
    }

        

}
