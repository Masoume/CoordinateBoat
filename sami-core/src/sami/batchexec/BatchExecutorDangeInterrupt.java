

package sami.batchexec;

import com.perc.utils.other.FileUtils;
import crw.Conversion;
import crw.event.output.proxy.ProxyEmergencyAbort;
import crw.event.output.proxy.ProxyExecutePath;
import crw.event.output.proxy.SetVelocityMultiplier;
import crw.proxy.BoatProxy;
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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
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
import sami.engine.Engine;
import sami.engine.PlanManager;
import sami.engine.PlanManagerListenerInt;
import sami.event.OutputEvent;
import sami.event.ReflectedEventSpecification;
import sami.mission.InterruptType;
import sami.mission.MissionPlanSpecification;
import sami.mission.Place;
import sami.mission.ProjectSpecification;
import sami.mission.Token;
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
public class BatchExecutorDangeInterrupt implements PlanManagerListenerInt, ProxyServerListenerInt{
  
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
   
    public static void main(String args[]) throws InterruptedException, IOException {

        BatchExecutorDangeInterrupt test = new BatchExecutorDangeInterrupt();

        test.initEverything();
    }
   
    private int count;
    private int interCount;
    private StopWatch timer;
    private PlanManager pm;

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
            
//            if (m.getName().equals("CLV Seq TeleOperation")) {
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
//                            System.out.println("TASKS2= " + toAdd);
//                            System.out.println("TASKS NEW= " + res.getFieldValues().get("tasks"));
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
                    writer.println("" + 1);
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
        System.out.println("interCount = "+interCount);

        if (count < 1) {

            for (MissionPlanSpecification m : missions) {
                
                if (m.getName().equals("CLV Seq TeleOperation")) {  
//                if (m.getName().equals("TeleOp")) {      
                    System.out.println("SPAWN && CLV DangeArea &&");
                    pm = Engine.getInstance().spawnRootMission(m);
                    break;
                }
            }
            
         //   final ProxyInt foundB1 = boats.get((new Random()).nextInt(boats.size()));
          //  ProxyInt foundB2 = boats.get((new Random()).nextInt(boats.size()));

            final ProxyInt foundB1 = boats.get(0);
            final ProxyInt foundB2 = boats.get(1);
            final ProxyInt foundB3 = boats.get(2);
            final ProxyInt foundB4 = boats.get(3);
            final ProxyInt foundB5 = boats.get(4);
            
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            Runnable task = new Runnable() {

                @Override
                public void run() {

                    interCount++;//number of interrupt for the same boat(inside and outside area)
                    System.out.println("I am here "+interCount);
                    
                    switch (interCount)
                    {
                        case 1:
                            Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB1)));
                            break;
                        case 2:
                            Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB5)));
                            break;
                        case 3:
                            Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB3)));
                            break;
//                        case 4:
//                            Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB4)));
//                            break;
//                        case 5:
//                            Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB5)));
//                            break;
                    }
                 }
           };
 
            executor.schedule(task, 13, TimeUnit.SECONDS);//boat 1 enters
            executor.schedule(task, 18, TimeUnit.SECONDS);//boat 2 enters
            executor.schedule(task, 37, TimeUnit.SECONDS);//boat 3 enters
           // executor.schedule(task, new Random().nextInt((45 - 40) + 1) + 40, TimeUnit.SECONDS);//boat 1 enters
//            executor.schedule(task, 50, TimeUnit.SECONDS);//boat 2 enters
//            executor.schedule(task, 55, TimeUnit.SECONDS);//boat 1 enters
       //     executor.schedule(task, new Random().nextInt((30 - 25) + 1) + 25, TimeUnit.SECONDS);//boat 2 enters
            
            count++;
        } 
    }

    @Override
    public void planAborted(PlanManager planManager
    ) {
        System.out.println("PLAN & "+ planManager.getPlanName() + " & ABORTED");
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

            PrintWriter writer = new PrintWriter(new File("results-interrupt.txt"));
            writer.println("NUMBER OF TASK ACCOMPLISHED:  " + lines);
            writer.println("NUMBER OF INTERACTIONS: " + 3);
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

    private LinkedList<Location> parsePosFromFile() {
        LinkedList<Location> returnList = new LinkedList<Location>();

        try {

            Scanner sc = new Scanner(new File("input/task-pos-20.txt"));

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] split = line.split("\\s");

                Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), 0);
                returnList.add(Conversion.positionToLocation(p));
            }
            sc.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BatchExecutorStandard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;
    }
    public void ChangeVelocity(){
        
        System.out.println("==>ChangeVelocity");

        if (interCount>=2){
            
            for (MissionPlanSpecification m : missions) { 
               
                 if (m.getName().equals("CLV Seq TeleOperation")) {     
                     System.out.println("MissionPlanSpecification "+m.getName());
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
                                     
                                //    res1.getFieldValues().clear();
                                    res1.getFieldValues().put("velocityMultiplier", 1.0);
//                                 }
                                    System.out.println("New Velocity = " + res1.getFieldValues().get("velocityMultiplier"));

                               //  break;
                             }
                         }
                     }
                 }
            }
        }
            
        System.out.println("<==ChangeVelocity");
        
    }
}
