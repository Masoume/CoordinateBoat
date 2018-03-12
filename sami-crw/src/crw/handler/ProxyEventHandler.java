package crw.handler;
    

import crw.Conversion;
import crw.Coordinator;
import crw.CrwHelper;
import crw.event.input.operator.OperatorSelectsCheckPointsArea;
import crw.event.input.operator.OperatorSelectsDangerousArea;
import crw.event.input.proxy.ProxyCreated;
import crw.event.input.proxy.ProxyPathCompleted;
import crw.event.input.proxy.ProxyPathFailed;
import crw.event.input.proxy.ProxyPoseUpdated;
import crw.event.input.proxy.QueueLearningDone;
import crw.event.input.proxy.QueueManagementDone;
import crw.event.input.proxy.SetVelocityMultiplierSucceeded;
import crw.event.input.service.AssembleLocationResponse;
import crw.event.input.service.BoatOutsideArea;
import crw.event.input.service.DecisionMakingLeave;
import crw.event.input.service.DecisionMakingStay;
import crw.event.output.proxy.SetVelocityMultiplier;//new
import crw.event.input.service.NearAssemblyLocationResponse;
import crw.event.input.service.NearestProxyToLocationResponse;
import crw.event.input.service.OperatorAvailable;
import crw.event.input.service.OperatorUnavailable;
import crw.event.input.service.ProxyAreaSelected;
import crw.event.input.service.QuantityEqual;
import crw.event.input.service.QuantityGreater;
import crw.event.input.service.QuantityLess;
import crw.event.input.service.TasksAssignmentResponse;
import crw.event.input.service.WaitingProxiesResponse;
import crw.event.output.operator.OperatorSelectDangerousArea;
import crw.event.output.operator.OperatorSelectCheckPointsArea;//new
import crw.event.output.proxy.CheckOperatorAvailable;
import crw.event.output.proxy.ConnectExistingProxy;
import crw.event.output.proxy.CreateSimulatedProxy;
import crw.event.output.service.AssembleLocationRequest;
import crw.event.output.proxy.ProxyEmergencyAbort;
import crw.event.output.proxy.ProxyExecutePath;
import crw.event.output.proxy.ProxyExploreArea;
import crw.event.output.proxy.ProxyGotoPoint;
import crw.event.output.proxy.ProxyPausePath;
import crw.event.output.proxy.ProxyResendWaypoints;
import crw.event.output.proxy.DecisionMakingRequest;
import crw.event.output.proxy.MultipleQueueLearning;//new
import crw.event.output.proxy.QueueLearning;//new
import crw.event.output.proxy.QueueManagement;//new
import crw.event.output.service.NearAssemblyLocationRequest;
import crw.event.output.service.NearestProxyToLocationRequest;
import crw.event.output.service.ProxyCompareDistanceRequest;
import crw.event.output.service.TasksAssignmentRequest;
import crw.general.FastSimpleBoatSimulator;
import crw.proxy.BoatProxy;
import crw.proxy.BoatState;
import crw.proxy.BoatWorld;
import crw.proxy.CentralState;
import crw.ui.BoatMarker;
import crw.ui.ImagePanel;
import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleService;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import randomGenerator.StdRandom; //for Queue management part
import robotutils.Pose3D;
import sami.area.Area2D;
import sami.batchexec.BatchType;
import sami.batchexec.StopWatch;
import sami.engine.Engine;
import sami.engine.PlanManager;
import sami.event.AbortMissionReceived;
import sami.event.GeneratedEventListenerInt;
import sami.event.GeneratedInputEventSubscription;
import sami.event.InputEvent;
import sami.event.OutputEvent;
import sami.event.ProxyAbortMission;
import sami.event.ReflectedEventSpecification;
import sami.handler.EventHandlerInt;
import sami.mission.Token;
import sami.path.Location;
import sami.path.Path;
import sami.path.PathUtm;
import sami.path.UTMCoordinate;
import sami.path.UTMCoordinate.Hemisphere;
import sami.proxy.ProxyInt;
import sami.proxy.ProxyListenerInt;
import sami.proxy.ProxyServerListenerInt;
import sami.service.information.InformationServer;
import sami.service.information.InformationServiceProviderInt;
import sami.uilanguage.toui.InformationMessage;
import sami.mission.InterruptType;
import sami.mission.MissionPlanSpecification;
import sami.mission.Place;
import sami.mission.Transition;
import sami.mission.Vertex;

/**
 *
 * @author pscerri
 */
public class ProxyEventHandler implements EventHandlerInt, ProxyListenerInt, InformationServiceProviderInt, ProxyServerListenerInt {

    private static final Logger LOGGER = Logger.getLogger(ProxyEventHandler.class.getName());
    // For most of the interesting part of the planet, 1 degree latitude is something like 110,000m
    // Longtitude varies a bit more, but 90,000m is a decent number for the purpose of this calculation
    // See http://www.csgnetwork.com/degreelenllavcalc.html
    final double M_PER_LON_D = 1.0 / 90000.0;
    final double M_PER_LAT_D = 1.0 / 110000.0;
    // Sending a waypoints list of size > 68 causes failure due to data size
    final int MAX_SEGMENTS_PER_PROXY = 68;
    ArrayList<GeneratedEventListenerInt> listeners = new ArrayList<GeneratedEventListenerInt>();
    HashMap<GeneratedEventListenerInt, Integer> listenerGCCount = new HashMap<GeneratedEventListenerInt, Integer>();
    int portCounter = 0;
    final Random RANDOM = new Random();
    private Hashtable<UUID, Integer> eventIdToAssembleCounter = new Hashtable<UUID, Integer>();

    private HashMap<BoatMarker, ArrayList<Position>> decisions = new HashMap<BoatMarker, ArrayList<Position>>();
    public ConcurrentHashMap<BoatProxy, ArrayList<Position>> visited = new ConcurrentHashMap<BoatProxy, ArrayList<Position>>();

    public Coordinator.Method method = Coordinator.Method.COST;

    public ArrayList<MissionPlanSpecification> missions = new ArrayList<MissionPlanSpecification>();

    public ProxyEventHandler() {
        LOGGER.log(Level.FINE, "Adding ProxyEventHandler as service provider");
        InformationServer.addServiceProvider(this);
        // Do not add as Proxy server listener here, will cause java.lang.ExceptionInInitializerError
        // Engine will add this for us
        //Engine.getInstance().getProxyServer().addListener(this);
    }

    @Override
    public void invoke(final OutputEvent oe, ArrayList<Token> tokens) {
        LOGGER.log(Level.FINE, "ProxyEventHandler invoked with " + oe);

    //    System.out.println("invoked:" + oe.getId() + " Name: "+ oe.toString());
        
        if (oe.getId() == null) {
            LOGGER.log(Level.WARNING, "\tOutputEvent " + oe + " has null event id");
        }
        if (oe.getMissionId() == null) {
            LOGGER.log(Level.WARNING, "\tOutputEvent " + oe + " has null mission id");
        }

        if (oe instanceof ProxyExecutePath) {
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyExecutePath has no tokens with proxies attached: " + oe);
            } else if (numProxies > 1) {
                LOGGER.log(Level.WARNING, "Place with ProxyExecutePath has " + numProxies + " tokens with proxies attached: " + oe);
            }
//            try {
//                PrintWriter writerMas = new PrintWriter(new File("input-tasks-pos.txt"));
//                writerMas.println(decisions.entrySet().toString());
//                System.out.println(decisions.entrySet());
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }

//            ProxyExecutePath pep = (ProxyExecutePath) oe;
//            (new Thread() {
//                @Override
//                public void run() {
//                    boolean guard;
//                        do {
//                            guard = false;
//                            for (ProxyInt b : pep.getProxyPaths().keySet()) {
//                                Path p = new Path();
//                                p
//                                ArrayList<Position> copy = new ArrayList<Position>(pep.getProxyPaths().get(b));
//                                for (Position p : copy) {
//                                    Double d = Coordinator.computeDistance(b.getProxy().getPosition(), p);
////                            System.out.println("distance: "+d);
//                                    if (d == 0.0) {
//                                        c.getDecisions().get(b).remove(p);
////                                System.out.println("distance equals: " + d);
//                                        LatLon m = new LatLon(p.getLatitude(), p.getLongitude());
//
//                                    } else if (Math.abs(d) < 3) {
//                                        c.getDecisions().get(b).remove(p);
////                                System.out.println("distance error: " + d);
//
//                                    }
//                                }
//                            }
//
//                            for (BoatMarker b : c.getMarkerToProxy().keySet()) {
//                                if (!c.getDecisions().get(b).isEmpty()) {
//                                    guard = true;
//
//                                }
//                            }
//
//                        } while (guard);
//                    
//                }
//
//            }).start();
            for (BoatProxy boatProxy : tokenProxies) {
                // Send the path
                boatProxy.handleEvent(oe);
            }
//        } else if (oe instanceof ProxyExecuteTask) {
//            //@todo simulator integration
        } else if (oe instanceof ProxyGotoPoint) {
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyGotoPoint has no tokens with proxies attached: " + oe);
            } else if (numProxies > 1) {
                LOGGER.log(Level.WARNING, "Place with ProxyGotoPoint has " + numProxies + " tokens with proxies attached: " + oe);
            }
            for (BoatProxy boatProxy : tokenProxies) {
                // Send the path
                boatProxy.handleEvent(oe);
            }
         
        }else if (oe instanceof DecisionMakingRequest){
            
            System.out.println("DecisionMakingRequest");
            
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                   // System.out.println("Color "+ tokenProxies.get(numProxies).getColor()); 
                    numProxies++;
                }
            }
            
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "DecisionMakingRequest has no relevant proxies attached: " + oe);
            }
            int randThreshold = new Random().nextInt((10) + 1);//40% leave, 60% wait
            if (randThreshold <= 6){
            //if (tokenProxies.get(0).getFinaldecision()){//interrupt
            //if (policyArray[iRand].equals("INTERRUPT")){
            //if (tokenProxies.get(0).getColor() != red){
              //  DecisionMakingStay responseEvent = new DecisionMakingStay(new ArrayList<BoatProxy>(Arrays.asList(tokenProxies.get(0))));
                DecisionMakingStay responseEvent = new DecisionMakingStay(oe.getId(), oe.getMissionId(),tokenProxies);
                System.out.println("DecisionMakingStay");
         //       Engine.getInstance().getPlans().get(0).enterSpecialPlace();
         //       System.out.println("I put a token in the special place!!");
                for (GeneratedEventListenerInt listener : listeners) {
                    LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                    listener.eventGenerated(responseEvent);
                }
             }else{
                DecisionMakingLeave responseEvent = new DecisionMakingLeave(oe.getId(), oe.getMissionId(),tokenProxies);
                System.out.println("DecisionMakingLeave");
                for (GeneratedEventListenerInt listener : listeners) {
                    LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                    listener.eventGenerated(responseEvent);
                }
            }
            
//        }else if (oe instanceof MultipleQueueLearning){
//            
//            System.out.println("CentralQueueLearning");
//            int numProxies = 0;
//            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
//            for (Token token : tokens) {
//                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
//                    tokenProxies.add((BoatProxy) token.getProxy()); 
//                    numProxies++;
//                }
//            }
//            final int numofProxies = numProxies;
//            if (numProxies == 0) {
//                LOGGER.log(Level.WARNING, "CentralQueueLearning has no relevant proxies attached: " + oe);
//            }
//            
//            (new Thread() {
//                
//                @Override
//                public void run(){
//                    // Queue parameters
//                    int maxIter = 20;//(int) (Math.random() * (38 - 25)) + 25;
//                    System.out.println("MaxIter "+maxIter);
//                    boolean balk = true;//balk property is available if set to true
//                    double lambda = Math.random() * (0.00030 - 0.00020) + 0.00020;//0.00025;//arrival rate
//                    double mu = Math.random() * (0.00032 - 0.00022) + 0.00022;//0.00027;//service rate
//                    double avgWaitingTime = 0.00027;
//                   
//                    int stateType = 2;//0:localOnly; 1:localGlobal; 2:fullState
//                    final int setupTime = (int) (Math.random() * (35 - 25)) + 25;//30;
//                    
//                    //learning curve setup
//                    double totalTeamAccRew = 0.0;
//                    double totalTeamRew = 0.0;
//                    
//                    // Service and Request classes
//                    class Service{
//                        int serviceType; //{1:recharge, 2:DangeArea, 3:connecLost }
//                        double[] probF = {0.9,0.4,0.2};
//                        public Service(int iType){
//                            this.serviceType = iType;
//                        }
//                        public int getServType(){
//                            return this.serviceType;
//                        }
//                        public double getProbF(){
//                            return probF[serviceType-1];
//                        }
//                        public double getServReward(){
//                            return getProbF();
//                        }
//                        public void PrintServiceDetail(){
//                            int type = this.serviceType;
//                            switch(type){
//                                case 1:
//                                    System.out.println("Service Type: RECHARGE with prob(f):"+this.probF[type-1]);
//                                    break;
//                                case 2:
//                                    System.out.println("Service Type: DANGEAREA with prob(f):"+this.probF[type-1]);
//                                    break;
//                                default:
//                                    System.out.println("Service Type: ConnLost with prob(f):"+this.probF[type-1]);
//                                    break;      
//                            }
//                        }
//                    } 
//                    class Request{
//                        public int iBoatId;
//                        public double dArrivalTime;
//                        public double dServiceTime;
//                        public double dDepartureTime;
//                        public int iServiceType;
//                        private Request(int iNo, double arrTime, double depTime,double servTime,int servType) {
//                            iBoatId = iNo;
//                            dArrivalTime = arrTime; 
//                            dServiceTime = servTime;
//                            dDepartureTime = depTime;
//                            iServiceType = servType;
//                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                        }
//                    }
//                    
//                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                    // Queue setup
//                    double nextArrival;// = StdRandom.exp(lambda); // time of next arrival
//                    double nextDeparture = Double.POSITIVE_INFINITY; // time of next departure   
//                    Queue<Request> queueOfReq = new LinkedList<>();
//
//                    int iter = 0;
//                    int iRand = 0;//random boat number 
//                    int remains = 0;//num of remaining tasks for learner boat
//                    int totalNumTasks = 0;
//                    boolean endSim = false;//for learning section
//                    double serviceTime;// = Double.POSITIVE_INFINITY;
//                    boolean bFF = false;
//                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                    //Central State
//                    CentralState centralSt = null;
//                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                    
//                    StopWatch timer = new StopWatch();
//                    timer.start();
//                    while (timer.getElapsedTimeSecs()<setupTime);//setup time
//                    
//                    nextArrival = timer.getElapsedTimeSecs() + StdRandom.exp(lambda);
//                    int numCustomersBalked = 0;
//                    //Random selection of boats removing repetitive generations
//                    ArrayList<Integer> boatList = new ArrayList<>();
//                    int[] reqType = {0,0,0,0,0}; 
//                    int[] nTasks = new int[numofProxies];
//                    for (int i=0; i<numofProxies; i++) {
//                        boatList.add(new Integer(i));
//                        tokenProxies.get(i).reqTypeInt = 0;
//                        nTasks[i] = tokenProxies.get(i).getCurrentWaypoints().size();
//                        if (nTasks[i]>=5) nTasks[i] = 2;
//                        else if (nTasks[i]>=3 && nTasks[i]<=4) nTasks[i]=1;
//                        else nTasks[i]=0;
//                    }
//                    if (stateType==2){
//                        centralSt = new CentralState(numofProxies,avgWaitingTime);
//                        centralSt.updateVal(reqType, nTasks, 0);
//                    }
//                    centralSt.printCurrentState();
//                    
//                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                    //main body of simulation
//                    while (iter<maxIter || !queueOfReq.isEmpty()){
//                        
//                        // it's an arrival
//                        if ((nextArrival <= nextDeparture)&&(iter<maxIter)) {
//                            
//                            //Random selection of boats
//                            if (!boatList.isEmpty()){
//                                Collections.shuffle(boatList);
//                                iRand = boatList.remove(0);   
//                                bFF = false;
//                            }
//                            else{
//                                for (int i=0; i<numofProxies; i++) {//there might be consideration when a boat finishes its tasks
//                                    boatList.add(new Integer(i));                                  
//                                }
//                                // remove boats which are already in the queue
//                                if (!queueOfReq.isEmpty()){
//                                    for (Request e : queueOfReq) {
//                                        int x;
//                                        for (int j=0;j<boatList.size();j++){ 
//                                            if (e.iBoatId==boatList.get(j)){
//                                                x = boatList.remove(j);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                                //Random selection of boats
//                                if (!boatList.isEmpty())
//                                {
//                                    Collections.shuffle(boatList);
//                                    iRand = boatList.remove(0);
//                                   // System.out.println("Request from boat " +iRand+" arrived at time: "+nextArrival);
//                                    bFF=false;
//                                }
//                                else {bFF = true;}
//                            }
//                            
//                            if (bFF==false){
//                                //Generate random service time
//                                serviceTime = StdRandom.exp(mu);
//                                if (queueOfReq.isEmpty()) nextDeparture = nextArrival + serviceTime;
//                                //generate a random serviceType {1,2,3}
//                                Service TypeofRequest = new Service((new Random()).nextInt(3)+1);
//                                
//                                //update boat(iRand) value
//                                nTasks[iRand] = tokenProxies.get(iRand).getCurrentWaypoints().size();
//                                
//                                if (nTasks[iRand]>=5) nTasks[iRand] = 2;
//                                else if (nTasks[iRand]>=3 && nTasks[iRand]<=4) nTasks[iRand]=1;
//                                else nTasks[iRand]=0;
//                                
//                                reqType[iRand] = TypeofRequest.getServType();
//                                tokenProxies.get(iRand).reqTypeInt = reqType[iRand];
//                                centralSt.updateVal(reqType, nTasks, queueOfReq.size());
//                                
//                                centralSt.printCurrentState();
//                                
//                                //decide to accept or reject
//                                int action =-1;
//                                if (balk){//always true; in general it means if arrival allowed to balk or not currDecisionMaker.getIntStat().getStateArr()
//                                    action = centralSt.getCentralWorld().selectAction(centralSt.getStateArr(),iRand);
//                                }  
//                                
//                                if (action==0){ //join
//                                    
//                                    System.out.println("action "+action);
//                                    System.out.println("QSize before update: "+queueOfReq.size());
//                                    System.out.println("Qsize in LocalState"+centralSt.getqsize());
//                                   
//                                    //update state 
//                                    int[] nextState = centralSt.getCentralWorld().getNextState(action,centralSt.getStateArr(),iRand); 
//                                    for (int i=0;i<numofProxies;i++)
//                                        tokenProxies.get(iRand).reqTypeInt = nextState[i*2];
//                                        
//                                    //update Q-Values
//                                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
//                                    double reward = centralSt.getCentralWorld().getNextReward(action,centralSt.getStateArr(),iRand,nextState,serviceTime,queueOfReq.size());
//                                    
//                                    double this_Q = centralSt.getCentralWorld().getPolicy().getQValue(centralSt.getStateArr(), action );
//                                    double max_Q = centralSt.getCentralWorld().getPolicy().getMaxQValue( nextState );
//
//                                    double new_Q = this_Q + centralSt.getCentralWorld().getAlpha() * ( reward + centralSt.getCentralWorld().getGamma() * max_Q - this_Q );
//                                   
//                                    centralSt.updateTotalRew(new_Q , reward);
//                                    centralSt.getCentralWorld().getPolicy().setQValue( centralSt.getStateArr(), action, new_Q );
//                                    
//                                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``        
//                                    //add to the queue
//                                    queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
//                                    //^^^^^^^^ 
//                                    //update current state to the new state
//                                   // currDecisionMaker.getIntStat().updateVal(4, nextState[1], queueOfReq.size()); //waiting 
//                                    if (stateType==2){
//                                        for (int i=0;i<numofProxies; i++) {
//                                            reqType[i]= nextState[i*2];//tokenProxies.get(i).getCurrentWaypoints().size();
//                                            nTasks[i]= nextState[i*2 + 1];//tokenProxies.get(i).getIntStat().getReqTypeInt();//waiting
//                                        }
//                                        System.out.println("iRand: "+iRand);
//                                        reqType[iRand] = 4;
//                                        tokenProxies.get(iRand).reqTypeInt = reqType[iRand];
//                                        centralSt.updateVal(reqType,nTasks,queueOfReq.size());
//                                    }
//                                    System.out.println("QSize after update: "+queueOfReq.size());
//                                    centralSt.printCurrentState();
//                                    //currDecisionMaker should stop
//                                    //PlanManager p = Engine.getInstance().getPlans().get(0);
//                                    tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));
//                                    
//                                }  
//                                else if (action == 1){ //balk
//                                    
//                                    System.out.println("action "+action);
//                                    numCustomersBalked++;
//                                    boatList.add(iRand);
//                                                                  
//                                    int[] currState = centralSt.getStateArr();                                  
//                                    double reward,this_Q,max_Q,new_Q;
//                                    
//                                    //update state
//                                    int[] nextState;// = currDecisionMaker.getBoatWorld().getNextState(action, currState);
// 
//                                    nextState = centralSt.getCentralWorld().getNextState(action,currState,iRand); 
//                                    for (int i=0;i<numofProxies;i++)
//                                        tokenProxies.get(iRand).reqTypeInt = nextState[i*2];
//                                                                  
//                                    reward = centralSt.getCentralWorld().getNextReward(action,currState,iRand,nextState,serviceTime,queueOfReq.size());
//                                    
//                                    this_Q = centralSt.getCentralWorld().getPolicy().getQValue(currState, action );
//                                    max_Q = centralSt.getCentralWorld().getPolicy().getMaxQValue( nextState );
//
//                                    new_Q = this_Q + centralSt.getCentralWorld().getAlpha() * ( reward + centralSt.getCentralWorld().getGamma() * max_Q - this_Q );
//                                    
//                                    centralSt.updateTotalRew(new_Q,reward);
//                                    
//                                    centralSt.getCentralWorld().getPolicy().setQValue(currState, action, new_Q);
//                                    
//                                    //update State
//                                    if (stateType==2){
//                                        for (int i=0;i<numofProxies;i++){
//                                            reqType[i] = nextState[i*2];
//                                            nTasks[i] = nextState[i*2+1];
//                                        }   
//                                        reqType[iRand] = 0; //normal
//                                        tokenProxies.get(iRand).reqTypeInt = reqType[iRand];
//                                        centralSt.updateVal(reqType, nTasks, queueOfReq.size());                                   
//                                    }
//                                    centralSt.printCurrentState();
//                                } //end balk
//                                
//                                iter++; 
//                        }
//                            
//                        nextArrival +=StdRandom.exp(lambda);   
//                        
//                    }//end of arrival
//                    else{ //if departure
//                                
//                        System.out.println("Departure: Current Q size: "+queueOfReq.size());
//                        centralSt.printCurrentState();
//                        
//                        if (!queueOfReq.isEmpty()){
//                            Request req;
//                            req = new Request(queueOfReq.peek().iBoatId,queueOfReq.peek().dArrivalTime,queueOfReq.peek().dDepartureTime,queueOfReq.peek().dServiceTime,queueOfReq.peek().iServiceType);
//                            queueOfReq.remove();
//
//                            //update state
//                            reqType[req.iBoatId] = 0;
//                            tokenProxies.get(req.iBoatId).reqTypeInt = 0;
//                            
//                            centralSt.updateVal(reqType, nTasks, queueOfReq.size());
//                            
////                            if (stateType==2){
////                                for (int i=0;i<numofProxies;i++){
////                                    nTasks[i] =  tokenProxies.get(i).getCurrentWaypoints().size();
////                                    if (nTasks[i]>=5) nTasks[i] = 2;
////                                    else if (nTasks[i]>=3 && nTasks[i]<=4) nTasks[i]=1;
////                                    else nTasks[i]=0;
////                                    
////                                    reqType[i] = tokenProxies.get(iRand).reqTypeInt;// tokenProxies.get(i).getIntStat().getReqTypeInt();
////                                }
////                                reqType[req.iBoatId] = 0;//normal
////                                centralSt.updateVal(reqType, nTasks, queueOfReq.size());
////                            }
//                            centralSt.printCurrentState();    
//                            System.out.println("Departure: Q size after: "+queueOfReq.size());
//                            //^^^^^^^^^^
//                            //the boat should resume the path
//                            BoatProxy inQB = tokenProxies.get(req.iBoatId);
//                            ArrayList<Position> currTasksPositions = new ArrayList<Position>();
//                            Set<BoatMarker> boatSet = decisions.keySet();
//                            BoatMarker bMarker;
//                            for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
//                                bMarker = it.next();
//                                if (bMarker.getProxy() == inQB) {
//                                    currTasksPositions.addAll(decisions.get(bMarker));
//                                    break;
//                                } 
//                            }
//
//                            Hashtable<ProxyInt, Path> proxyPathTemp = new Hashtable<ProxyInt, Path>();
//
//                            ArrayList<Location> tasksLocations = Conversion.positionToLocation(currTasksPositions);
//
//                            proxyPathTemp.put(inQB, new PathUtm(tasksLocations));
//
//                            ProxyExecutePath oEv1 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPathTemp);
//
//                            inQB.handleEvent(oEv1);
//
//                            //***********************************
//                            double wait = nextDeparture - req.dArrivalTime;
////                            totalWaitingTime += wait;
////                            numCustomersServed++;
//
//                            if (queueOfReq.isEmpty()) nextDeparture = Double.POSITIVE_INFINITY;
//                            else nextDeparture += StdRandom.exp(mu); //nextDeparture += StdRandom.gaussian(mu,100);
//                        }
//                        else {nextDeparture = Double.POSITIVE_INFINITY;}
//                    }//end if departure
//                    
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    //10 Sep: this part is removed in the new version, in which the episode ends only when #iter = maxIter
//                    if (endSim){
//                        break;
//                    }                
//                }
//    
//                //simulation ends
//                System.out.println("TotalCustomersBalked: "+numCustomersBalked);
//                System.out.println("------------");
//                centralSt.showTotalRew();
//                System.out.println("------------");
//                centralSt.saveTotalAccRew();
//                centralSt.saveTotalRew();
//                centralSt.getCentralWorld().getPolicy().saveQValues();
//                //the whole plan should be aborted           
//                PlanManager p = Engine.getInstance().getPlans().get(0);
//                p.abortMission();
//                //Engine.getInstance().abort(p);
//                System.exit(0); 
//                }     
//            }).start();
//                
//            QueueLearningDone responseEvent = new QueueLearningDone(oe.getId(), oe.getMissionId(),tokenProxies);
//
//            for (GeneratedEventListenerInt listener : listeners) {
//                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
//                listener.eventGenerated(responseEvent);
//            }
//                
//            
        }else if (oe instanceof MultipleQueueLearning){
            
            System.out.println("MultipleQueueLearning");
            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy()); 
                    numProxies++;
                }
            }
            final int numofProxies = numProxies;
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "MultipleQueueLearning has no relevant proxies attached: " + oe);
            }
            
            (new Thread() {
                
                @Override
                public void run(){
                    
                    // Queue parameters
                    int maxIter = 20;//(int) (Math.random() * (38 - 25)) + 25;
                    System.out.println("MaxIter "+maxIter);
                    boolean balk = true;//balk property is available if set to true
                    double lambda = Math.random() * (0.00030 - 0.00020) + 0.00020;//0.00025;//arrival rate
                    double mu = Math.random() * (0.00032 - 0.00022) + 0.00022;//0.00027;//service rate
                    double avgWaitingTime = 0.00027;
                   // boolean bLocal = false; //false: GL; true: L
                    int stateType = 0;//0:localOnly; 1:localGlobal; 2:fullState
                    final int setupTime = (int) (Math.random() * (35 - 25)) + 25;//30;
                    //learning curve setup

                    double totalTeamAccRew = 0.0;
                    double totalTeamRew = 0.0;
                    
                    // Service and Request classes
                    class Service{
                        int serviceType; //{1:recharge, 2:DangeArea, 3:connecLost }
                        double[] probF = {0.9,0.4,0.2};
                        public Service(int iType){
                            this.serviceType = iType;
                        }
                        public int getServType(){
                            return this.serviceType;
                        }
                        public double getProbF(){
                            return probF[serviceType-1];
                        }
                        public double getServReward(){
                            return getProbF();
                        }
                        public void PrintServiceDetail(){
                            int type = this.serviceType;
                            switch(type){
                                case 1:
                                    System.out.println("Service Type: RECHARGE with prob(f):"+this.probF[type-1]);
                                    break;
                                case 2:
                                    System.out.println("Service Type: DANGEAREA with prob(f):"+this.probF[type-1]);
                                    break;
                                default:
                                    System.out.println("Service Type: ConnLost with prob(f):"+this.probF[type-1]);
                                    break;      
                            }
                        }
                    } 
                    class Request{
                        public int iBoatId;
                        public double dArrivalTime;
                        public double dServiceTime;
                        public double dDepartureTime;
                        public int iServiceType;
                        private Request(int iNo, double arrTime, double depTime,double servTime,int servType) {
                            iBoatId = iNo;
                            dArrivalTime = arrTime; 
                            dServiceTime = servTime;
                            dDepartureTime = depTime;
                            iServiceType = servType;
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    }
                    
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Queue setup
                    double nextArrival;// = StdRandom.exp(lambda); // time of next arrival
                    double nextDeparture = Double.POSITIVE_INFINITY; // time of next departure   
                    Queue<Request> queueOfReq = new LinkedList<>();

                    int iter = 0;
                    int iRand = 0;//random boat number 
                    int remains = 0;//num of remaining tasks for learner boat
                    int totalNumTasks = 0;
                    boolean endSim = false;//for learning section
                    double serviceTime;// = Double.POSITIVE_INFINITY;
                    boolean bFF = false;
             
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    
                    StopWatch timer = new StopWatch();
                    timer.start();
                    while (timer.getElapsedTimeSecs()<setupTime);//setup time
                    
                    nextArrival = timer.getElapsedTimeSecs() + StdRandom.exp(lambda);
                   // nextArrival = timer.getElapsedTimeSecs() + StdRandom.gaussian(lambda, 100);
                    int numCustomersBalked = 0;
                    //Random selection of boats removing repetitive generations
                    ArrayList<Integer> boatList = new ArrayList<>();
                    int[] reqType = {0,0,0,0,0}; 
                    int[] nTasks = new int[5];
                    for (int i=0; i<numofProxies; i++) {
                        boatList.add(new Integer(i));
                        // initializing multi-agent learning params
                        tokenProxies.get(i).initQLearning(stateType,avgWaitingTime,lambda); //0:localOnly; 1:localGlobal; 2:fullState //false: GL; true: L 
                        totalNumTasks = tokenProxies.get(i).getCurrentWaypoints().size();
                        nTasks[i] = totalNumTasks;                   
                        tokenProxies.get(i).getIntStat().updateVal(0, totalNumTasks, 0);
                     //  tokenProxies.get(i).getIntStat().setPrevAction(0);//Move-on
                    }
                    if (stateType==2){
                        for (int i=0;i<numofProxies; i++) {
                            tokenProxies.get(i).getIntStat().updateVal(reqType,nTasks,0);//for each boat, keep the reqType and #tasks of all other boats
                          //  tokenProxies.get(i).getIntStat().setPrevAction(0);//Move-on
                        }
                    }
                    
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //main body of simulation
                    while (iter<maxIter || !queueOfReq.isEmpty()){
                        
                        // it's an arrival
                       // balkThreshold = 0;
                        if ((nextArrival <= nextDeparture)&&(iter<maxIter)) {
                            
                            //Random selection of boats
                            if (!boatList.isEmpty()){
                                Collections.shuffle(boatList);
                                iRand = boatList.remove(0);
                               // System.out.println("Request from boat " +iRand+" arrived at time: "+nextArrival);   
                                bFF = false;
                            }
                            else{
                                for (int i=0; i<numofProxies; i++) {//there might be consideration when a boat finishes its tasks
                                    boatList.add(new Integer(i));                                  
                                }
                                // remove boats which are already in the queue
                                if (!queueOfReq.isEmpty()){
                                    for (Request e : queueOfReq) {
                                        int x;
                                        for (int j=0;j<boatList.size();j++){ 
                                            if (e.iBoatId==boatList.get(j)){
                                                x = boatList.remove(j);
                                                break;
                                            }
                                        }
                                    }
                                }
                                //Random selection of boats
                                if (!boatList.isEmpty())
                                {
                                    Collections.shuffle(boatList);
                                    iRand = boatList.remove(0);
                                   // System.out.println("Request from boat " +iRand+" arrived at time: "+nextArrival);
                                    bFF=false;
                                }
                                else {bFF = true;}
                            }
                            
                            if (bFF==false){
                                //Generate random service time
                                serviceTime = StdRandom.exp(mu);
                                if (queueOfReq.isEmpty()) nextDeparture = nextArrival + serviceTime;
                                //generate a random serviceType {1,2,3}
                                Service TypeofRequest = new Service((new Random()).nextInt(3)+1);
                                
                                //update boat(iRand) value
                                BoatProxy currDecisionMaker = tokenProxies.get(iRand);
                                for (int i=0;i<numofProxies; i++) {
                                    nTasks[i]= tokenProxies.get(i).getCurrentWaypoints().size();
                                    reqType[i]= tokenProxies.get(i).getIntStat().getReqTypeInt();
                                }
                                remains = currDecisionMaker.getCurrentWaypoints().size();
                                currDecisionMaker.getIntStat().updateVal(TypeofRequest.getServType(), remains, queueOfReq.size());
                                reqType[iRand] = TypeofRequest.getServType();
                                
                                if (stateType==2)//full state
                                    currDecisionMaker.getIntStat().updateVal(reqType, nTasks, queueOfReq.size());
                                
                                System.out.println("Current Decision Maker: Boat"+iRand);
                                //decide to balk or join
                                int action =-1;
                                if (balk){//always true; in general it means if arrival allowed to balk or not
                                    action = currDecisionMaker.getBoatWorld().selectAction(currDecisionMaker.getIntStat().getStateArr(),iRand);
                                }  
                                // keep the seleceted action; later on as a prev. action
                               // currDecisionMaker.getIntStat().setPrevAction(action);
                                
                                if (action==0){ //join
                                    
                                    System.out.println("action "+action);
                                    System.out.println("QSize before update: "+queueOfReq.size());
                                    System.out.println("Qsize in LocalState"+currDecisionMaker.getIntStat().getqsize());
                                    //update state 
                                    int[] nextState;
                                    
                                    if (stateType!=2) 
                                        nextState = currDecisionMaker.getBoatWorld().getNextState(action, currDecisionMaker.getIntStat().getStateArr());            
                                    else //full state presentation
                                        nextState = currDecisionMaker.getBoatWorld().getNextState(action,currDecisionMaker.getIntStat().getStateArr(),iRand); 
                                    
                                    //!!!this section should update the q-values of all boats not only the currDecisionMaker; new version 6 Oct.
                                    //in other words the reward should go to all boats
                                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    double reward;
                                    
                                    if (stateType==2)
                                        reward = currDecisionMaker.getBoatWorld().getNextReward(action,currDecisionMaker.getIntStat().getStateArr(),iRand,nextState,serviceTime,queueOfReq.size());
                                    else
                                        reward = currDecisionMaker.getBoatWorld().getNextReward(action,currDecisionMaker.getIntStat().getStateArr(),nextState,serviceTime,queueOfReq.size());
                                    
                                    double this_Q = currDecisionMaker.getBoatWorld().getPolicy().getQValue( currDecisionMaker.getIntStat().getStateArr(), action );
                                    double max_Q = currDecisionMaker.getBoatWorld().getPolicy().getMaxQValue( nextState );

                                    double new_Q = this_Q + currDecisionMaker.getBoatWorld().getAlpha() * ( reward + currDecisionMaker.getBoatWorld().getGamma() * max_Q - this_Q );
                                   
                                    currDecisionMaker.getIntStat().updateTotalRew(new_Q,reward);
                                   
                                    currDecisionMaker.getBoatWorld().getPolicy().setQValue( currDecisionMaker.getIntStat().getStateArr(), action, new_Q );

                                    //!! new Q-Values of all other boats
//                                    for (int i=0;i<numofProxies; i++) {
//                                        if (i!=iRand){
//                                            //prev state of boat i: tokenProxies.get(i).getIntStat().getStateArr()
//                                            int[] prevState = tokenProxies.get(i).getIntStat().getPrevStateArr();
//                                            //prev action of boat i: 
//                                            int prevAction = tokenProxies.get(i).getIntStat().getPrevAction();
//                                            
//                                            this_Q = tokenProxies.get(i).getBoatWorld().getPolicy().getQValue(prevState, prevAction);
//                                            max_Q = tokenProxies.get(i).getBoatWorld().getPolicy().getMaxQValue( tokenProxies.get(i).getIntStat().getStateArr());
//                                            new_Q = this_Q + tokenProxies.get(i).getBoatWorld().getAlpha() * ( reward + tokenProxies.get(i).getBoatWorld().getGamma() * max_Q - this_Q );
//                                            
//                                            tokenProxies.get(i).getIntStat().updateTotalRew(new_Q,reward);
//                                            tokenProxies.get(i).getBoatWorld().getPolicy().setQValue( prevState, prevAction, new_Q );
//                                        }
//                                    }  
                                  
                                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    
                                    //update current state to the new state
                                    remains = currDecisionMaker.getCurrentWaypoints().size();
                                   // currDecisionMaker.getIntStat().updateVal(4, nextState[1], nextState[2]); //waiting
                                    
                                    //add to the queue
                                    queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
                                    //^^^^^^^^ 
                                    //update current state to the new state
                                    currDecisionMaker.getIntStat().updateVal(4, nextState[1], queueOfReq.size()); //waiting
                                    
                                    if (stateType==2){
                                        for (int i=0;i<numofProxies; i++) {
                                            nTasks[i]= tokenProxies.get(i).getCurrentWaypoints().size();
                                            reqType[i]= tokenProxies.get(i).getIntStat().getReqTypeInt();//waiting
                                        }
                                        currDecisionMaker.getIntStat().updateVal(reqType,nTasks,queueOfReq.size());
                                    }
                                    System.out.println("QSize after update: "+queueOfReq.size());
                                    System.out.println("Qsize after update in LocalState: "+currDecisionMaker.getIntStat().getqsize());
                                    
                                    currDecisionMaker.getIntStat().printCurrentState();
                                    //currDecisionMaker should stop
                                    //PlanManager p = Engine.getInstance().getPlans().get(0);
                                    currDecisionMaker.handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));
                                }
                                else if (action == 1){ //balk
                                    
                                    System.out.println("action "+action);
                                    numCustomersBalked++;
                                    boatList.add(iRand);
                                                                  
                                    int[] currState = currDecisionMaker.getIntStat().getStateArr();                                  
                                    double reward,this_Q,max_Q,new_Q;
                                    
                                    //update state
                                    int[] nextState;// = currDecisionMaker.getBoatWorld().getNextState(action, currState);
                                    
                                    if (stateType!=2) 
                                        nextState = currDecisionMaker.getBoatWorld().getNextState(action, currState);            
                                    else 
                                        nextState = currDecisionMaker.getBoatWorld().getNextState(action,currState,iRand); 
                                    
                                    if (stateType!=2)
                                        reward = currDecisionMaker.getBoatWorld().getNextReward(action,currDecisionMaker.getIntStat().getStateArr(),nextState,serviceTime,queueOfReq.size());
                                    else
                                        reward = currDecisionMaker.getBoatWorld().getNextReward(action,currDecisionMaker.getIntStat().getStateArr(),iRand,nextState,serviceTime,queueOfReq.size());
                                    
                                    this_Q = currDecisionMaker.getBoatWorld().getPolicy().getQValue( currDecisionMaker.getIntStat().getStateArr(), action );
                                    max_Q = currDecisionMaker.getBoatWorld().getPolicy().getMaxQValue( nextState );

                                    new_Q = this_Q + currDecisionMaker.getBoatWorld().getAlpha() * ( reward + currDecisionMaker.getBoatWorld().getGamma() * max_Q - this_Q );
                                    currDecisionMaker.getIntStat().updateTotalRew(new_Q,reward);
                                    
                                    currDecisionMaker.getBoatWorld().getPolicy().setQValue(currState, action, new_Q);
                                    
                                    //!! new Q-Values of all other boats
//                                    for (int i=0;i<numofProxies; i++) {
//                                        if (i!=iRand){
//                                            //prev state of boat i: tokenProxies.get(i).getIntStat().getStateArr()
//                                            int[] prevState = tokenProxies.get(i).getIntStat().getPrevStateArr();
//                                            //prev action of boat i: 
//                                            int prevAction = tokenProxies.get(i).getIntStat().getPrevAction();
//                                            
//                                            this_Q = tokenProxies.get(i).getBoatWorld().getPolicy().getQValue(prevState, prevAction);
//                                            max_Q = tokenProxies.get(i).getBoatWorld().getPolicy().getMaxQValue( tokenProxies.get(i).getIntStat().getStateArr());
//                                            new_Q = this_Q + tokenProxies.get(i).getBoatWorld().getAlpha() * ( reward + tokenProxies.get(i).getBoatWorld().getGamma() * max_Q - this_Q );
//                                            
//                                            tokenProxies.get(i).getIntStat().updateTotalRew(new_Q,reward);
//                                            tokenProxies.get(i).getBoatWorld().getPolicy().setQValue( prevState, prevAction, new_Q );
//                                        }
//                                    }
                                    
                                    
                                    //update State
                                    //currDecisionMaker.getIntStat().updateVal(nextState[0], nextState[1], queueOfReq.size());         
                                    //@16 Oct.
                                    currDecisionMaker.getIntStat().updateVal(0, nextState[1], queueOfReq.size());//normal
                                    
                                    if (stateType==2){
                                        for (int i=0;i<numofProxies;i++){
                                            reqType[i] = nextState[i*2];
                                            nTasks[i] = nextState[i*2+1];
                                        }                                      
                                        currDecisionMaker.getIntStat().updateVal(reqType, nTasks, queueOfReq.size());                                   
                                    }
                                    
                                    currDecisionMaker.getIntStat().printCurrentState();
                                    //new version @16 Oct.: failure does not let the sim. to end. We continue until the fixed no. of iteration
//                                    if (stateType == 2){
//                                        if (nextState[iRand*2]==5)//fail
//                                         endSim = true;
//                                    }
//                                    else {
//                                        if (nextState[0]==5)//fail
//                                         endSim = true;
//                                    }
                                }
                                
                                iter++;
                            }
                            
                            nextArrival +=StdRandom.exp(lambda);
                        }
                        else{ // it's a departure
                            
                            System.out.println("Departure: Current Q size: "+queueOfReq.size());
                            if (!queueOfReq.isEmpty()){
                                Request req = null;
                                req = new Request(queueOfReq.peek().iBoatId,queueOfReq.peek().dArrivalTime,queueOfReq.peek().dDepartureTime,queueOfReq.peek().dServiceTime,queueOfReq.peek().iServiceType);
                         //       System.out.println("Removed: boat number "+req.iBoatId+" arrivalTime "+req.dArrivalTime+ " DepartureTime "+req.dDepartureTime+ " ServiceType "+req.iServiceType);
                                queueOfReq.remove();

                                //update state
                                tokenProxies.get(req.iBoatId).getIntStat().updateVal(0, remains, queueOfReq.size());//normal=0
                              //  tokenProxies.get(req.iBoatId).getIntStat().setPrevAction(1);//balk or move-on  while waiting
                                
                                if (stateType==2){
                                    for (int i=0;i<numofProxies;i++){
                                        nTasks[i] =  tokenProxies.get(i).getCurrentWaypoints().size();
                                        reqType[i] = tokenProxies.get(i).getIntStat().getReqTypeInt();
                                    }
                                    tokenProxies.get(req.iBoatId).getIntStat().updateVal(reqType, nTasks, queueOfReq.size());//normal=0
                                }
                                
                                tokenProxies.get(req.iBoatId).getIntStat().printCurrentState();
                                
                                System.out.println("Departure: Q size after: "+queueOfReq.size());
                                //^^^^^^^^^^
                                //the boat should resume the path
                                BoatProxy inQB = tokenProxies.get(req.iBoatId);
                                ArrayList<Position> currTasksPositions = new ArrayList<Position>();
                                Set<BoatMarker> boatSet = decisions.keySet();
                                BoatMarker bMarker;
                                for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
                                    bMarker = it.next();
                                    if (bMarker.getProxy() == inQB) {
                                        currTasksPositions.addAll(decisions.get(bMarker));
                                        break;
                                    } 
                                }

                                Hashtable<ProxyInt, Path> proxyPathTemp = new Hashtable<ProxyInt, Path>();

                                ArrayList<Location> tasksLocations = Conversion.positionToLocation(currTasksPositions);

                                proxyPathTemp.put(inQB, new PathUtm(tasksLocations));

                                ProxyExecutePath oEv1 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPathTemp);

                                inQB.handleEvent(oEv1);

                                //***********************************
                                double wait = nextDeparture - req.dArrivalTime;
    //                            totalWaitingTime += wait;
    //                            numCustomersServed++;

                                if (queueOfReq.isEmpty()) nextDeparture = Double.POSITIVE_INFINITY;
                                else nextDeparture += StdRandom.exp(mu); //nextDeparture += StdRandom.gaussian(mu,100);
                            }
                            else {nextDeparture = Double.POSITIVE_INFINITY;}
                        }
                        
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //10 Sep: this part is removed in the new version, in which the episode ends only when #iter = maxIter
                        if (endSim){
                            break;
                        }  
                   }
                   //simulation ends
                   System.out.println("TotalCustomersBalked: "+numCustomersBalked);
                   System.out.println("------------");
                   //for each boat, save Q values and show accumulated reward
                   for (int i=0;i<numofProxies;i++){
                       totalTeamAccRew += tokenProxies.get(i).getIntStat().getTotalAccRew();
                       totalTeamRew += tokenProxies.get(i).getIntStat().getTotalRew();
                       System.out.println("Boat "+i);
                       tokenProxies.get(i).getIntStat().showTotalRew();
                       System.out.println("------------");
                       tokenProxies.get(i).getBoatWorld().getPolicy().saveQValues();
                       tokenProxies.get(i).getIntStat().saveTotalAccRew();
                   }
                   //save team accumulated reward in a file 
                   saveTotalTeamAccRew(totalTeamAccRew);
                   saveTotalTeamRew(totalTeamRew);
                   System.out.println("TotalTeamAccRew: "+totalTeamAccRew);
                   System.out.println("TotalTeamRew: "+totalTeamRew);
                   //the whole plan should be aborted           
                   PlanManager p = Engine.getInstance().getPlans().get(0);
                   p.abortMission();
                   //Engine.getInstance().abort(p);
                   System.exit(0); 
                }
            
            }).start();
            
            QueueLearningDone responseEvent = new QueueLearningDone(oe.getId(), oe.getMissionId(),tokenProxies);

            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                listener.eventGenerated(responseEvent);
            }
            
        }else if (oe instanceof QueueLearning){
            
            System.out.println("QueueLearning");
            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy()); 
                    numProxies++;
                }
            }
            final int numofProxies = numProxies;
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "QueueLearning has no relevant proxies attached: " + oe);
            }
            
            // this thread will generate arrival and take care of pop and push to the queue
            (new Thread() {
                @Override
                public void run() {
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Queue parameters
                    int maxIter = (int) (Math.random() * (38 - 25)) + 25;
                    //int maxIter = 30;//to be compatible with previous experiments
                    System.out.println("MaxIter "+maxIter);
                    boolean balk = true;//balk property is available if set to true
                    double lambda = Math.random() * (0.00034 - 0.00024) + 0.00024;//0.00025;//arrival rate
                    double mu = Math.random() * (0.00036 - 0.00026) + 0.00026;//0.00027;//service rate
                    boolean bDiscipline = true;//true:FIFO; false:SJF
                    double waitingCost;//cost of waiting in the system (both queue and server)
                    double alpha = 20/mu;//1/(2*mu);
                    double betta = 18;//0.24;
                    double serviceReward;//reward of completing a service
                    double balkThreshold = 0; //= Math.ceil(serviceReward/(mu*waitingCost));  //2;   
                    final int numType = 3;
                    final int setupTime = (int) (Math.random() * (35 - 25)) + 25;//27;
                    //learning curve setup
                    double totalRew = 0.0;
                    double totalAccRew = 0.0;
                    // QLearning parameter for boat 1 with index 0;
                    final int learnerBoatNum = 0;
                    BoatProxy bLearner = tokenProxies.get(learnerBoatNum);
                   // bLearner.initQLearning();
                    //at the begining both the q size and #tasksDone are zero
                    class Service{
                        int serviceType; //{1:recharge, 2:DangeArea, 3:connecLost }
                        double[] probF = {0.9,0.4,0.2};
                        public Service(int iType){
                            this.serviceType = iType;
                        }
                        public int getServType(){
                            return this.serviceType;
                        }
                        public double getProbF(){
                            return probF[serviceType-1];
                        }
                        public double getServReward(){
                            return getProbF();
                        }
                        public void PrintServiceDetail(){
                            int type = this.serviceType;
                            switch(type){
                                case 1:
                                    System.out.println("Service Type: RECHARGE with prob(f):"+this.probF[type-1]);
                                    break;
                                case 2:
                                    System.out.println("Service Type: DANGEAREA with prob(f):"+this.probF[type-1]);
                                    break;
                                default:
                                    System.out.println("Service Type: ConnLost with prob(f):"+this.probF[type-1]);
                                    break;      
                            }
                        }
                    } 
                    class Request{
                        public int iBoatId;
                        public double dArrivalTime;
                        public double dServiceTime;
                        public double dDepartureTime;
                        public int iServiceType;
                        private Request(int iNo, double arrTime, double depTime,double servTime,int servType) {
                            iBoatId = iNo;
                            dArrivalTime = arrTime; 
                            dServiceTime = servTime;
                            dDepartureTime = depTime;
                            iServiceType = servType;
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    }
                    
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Queue setup
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    double nextArrival;// = StdRandom.exp(lambda); // time of next arrival
                    double nextDeparture = Double.POSITIVE_INFINITY; // time of next departure   
                    Queue<Request> queueOfReq = new LinkedList<Request>();//for now, we just keep the boat's number in the Q

                    int iter = 0;
                    int iRand = 0;//random number of a boat
                    int remains = 0;//num of remaining tasks for learner boat
                    boolean endSim = false;//for learning section
                    double serviceTime;// = Double.POSITIVE_INFINITY;
                    boolean bFF = false;
                    double tempNDep = Double.POSITIVE_INFINITY;
                    StopWatch timer = new StopWatch();
                    timer.start();
                    while (timer.getElapsedTimeSecs()<setupTime);//setup times
                    bLearner.initQLearning(1,mu,lambda);//if false GL, ow. onyl local
                    nextArrival = timer.getElapsedTimeSecs() + StdRandom.exp(lambda);
                   // nextArrival = timer.getElapsedTimeSecs() + StdRandom.gaussian(lambda, 100);
                    int numCustomersBalked = 0;
                    //Random selection of boats removing repetitive generations
                    ArrayList<Integer> boatList = new ArrayList<Integer>();
                    for (int i=0; i<numofProxies; i++) {
                        boatList.add(new Integer(i));
                    }
                    //new^^^^^
                    final int totalNumTasks = tokenProxies.get(learnerBoatNum).getCurrentWaypoints().size();
                    bLearner.getIntStat().updateVal(0, totalNumTasks, 0);//initial values for learner
                    //^^^^^^^^^^^^^^^^^^^
                    while (iter<maxIter || !queueOfReq.isEmpty()){
                        // it's an arrival
                        balkThreshold = 0;
                        if ((nextArrival <= nextDeparture)&&(iter<maxIter)) {
                            //Random selection of boats
                            if (!boatList.isEmpty()){
                                Collections.shuffle(boatList);
                                iRand = boatList.remove(0);
                               // System.out.println("Request from boat " +iRand+" arrived at time: "+nextArrival);   
                                bFF = false;
                            }
                            else{
                                for (int i=0; i<numofProxies; i++) {//there might be consideration when a boat finishes its tasks
                                    boatList.add(new Integer(i));                                  
                                }
                                // remove boats which are already in the queue
                                if (!queueOfReq.isEmpty()){
                                    for (Request e : queueOfReq) {
                                        int x;
                                        for (int j=0;j<boatList.size();j++){ 
                                            if (e.iBoatId==boatList.get(j)){
                                                x = boatList.remove(j);
                                                break;
                                            }
                                        }
                                    }
                                }
                                //Random selection of boats
                                if (!boatList.isEmpty())
                                {
                                    Collections.shuffle(boatList);
                                    iRand = boatList.remove(0);
                                   // System.out.println("Request from boat " +iRand+" arrived at time: "+nextArrival);
                                    bFF=false;
                                }
                                else {bFF = true;}
                            }
                            if (bFF==false){
                                //Generate random service time
                                serviceTime = StdRandom.exp(mu);
                                if (queueOfReq.isEmpty()) nextDeparture = nextArrival + serviceTime;
                                // serviceTime = StdRandom.gaussian(mu, 100);

                                //generate a random serviceType {1,2,3}
                                Service TypeofRequest = new Service((new Random()).nextInt(3)+1);

                                //new^^^^^
                                if (iRand==learnerBoatNum){
                                    
                                    remains = tokenProxies.get(iRand).getCurrentWaypoints().size();
                                    //if (remains>0)the reward should be updated
                                    bLearner.getIntStat().updateVal(TypeofRequest.getServType(), remains, queueOfReq.size());
                                }
                                //^^^^^^^^ 
                                //computing the threshold value for the new arrival
                                int action =-1;
                                if (balk){//always true
                                    //new^^^^^
                                    if (iRand==learnerBoatNum){//if learner
                                        action = bLearner.getBoatWorld().selectAction(bLearner.getIntStat().getStateArr(),iRand);
                                   //     System.out.println("Selected Action is "+action+" for learner at state "+ bLearner.getIntStat().getStateArr());
                                    }
                                    //^^^^^^^^ 
                                    else {//other agents
                                        serviceReward = alpha*(TypeofRequest.getServReward());
                                      //  System.out.println("ServiceReward = "+serviceReward);
                                        int tmpT = tokenProxies.get(iRand).getCurrentWaypoints().size();
                                        if (tmpT==0){ tmpT = 1;} 
                                        waitingCost = betta/(tmpT);
                                     //   System.out.println("WaitingCost = "+waitingCost);
                                        if (waitingCost==0) {waitingCost = 1;}
                                        balkThreshold = Math.ceil((serviceReward*mu)/waitingCost);
                                       // totalThresholdValue+=balkThreshold;
                                   //     System.out.println("balkThreshold = "+balkThreshold);
                                    }
                                }                                
                                if ((!balk) || (queueOfReq.size()< balkThreshold) || (queueOfReq.isEmpty()) || (action==0)){ //join
                                   // queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.serviceType));
                                    //new^^^^^
                                    if ((iRand==learnerBoatNum)&&(action==0)){
                                        //update state value
                                     //   action = 0;
                                        int[] nextState = bLearner.getBoatWorld().getNextState(action, bLearner.getIntStat().getStateArr());
                                        
                                        double reward = bLearner.getBoatWorld().getNextReward(action,bLearner.getIntStat().getStateArr(),nextState,serviceTime,bLearner.getIntStat().getqsize());
                                        double this_Q = bLearner.getBoatWorld().getPolicy().getQValue( bLearner.getIntStat().getStateArr(), action );
                                        double max_Q = bLearner.getBoatWorld().getPolicy().getMaxQValue( nextState );

                                  //      System.out.println("reward: "+reward+" thisQ: "+this_Q+" maxQ: "+max_Q);
                                        //bLearner.getIntStat().updateTotalRew(reward);
                                        double new_Q = this_Q + bLearner.getBoatWorld().getAlpha() * ( reward + bLearner.getBoatWorld().getGamma() * max_Q - this_Q );
                                        bLearner.getIntStat().updateTotalRew(new_Q,reward);
                                        bLearner.getBoatWorld().getPolicy().setQValue( bLearner.getIntStat().getStateArr(), action, new_Q );
                                        
                                        //update current state to the new state
                                        remains = tokenProxies.get(learnerBoatNum).getCurrentWaypoints().size();
                                        bLearner.getIntStat().updateVal(4, nextState[1], nextState[2]);
                                    }
                                    System.out.println("action "+action);
                                    queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
                                    //^^^^^^^^ 
                                    //tokenProxies.get(iRand) boat should stop
                                    //PlanManager p = Engine.getInstance().getPlans().get(0);
                                    tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));
                                   // tokenProxies.get(iRand).abortMission(p.missionId);                              
                                }
                                else{ //balked|moveOn
                                    numCustomersBalked++;
                                    boatList.add(iRand);
                                    Random rand = new Random(); 
                                    int ran = rand.nextInt(10)+1; //generate random num for probF
                                    int rType = bLearner.getIntStat().getReqTypeInt();
                                    boolean terminate = false;
                                    int[] currState = bLearner.getIntStat().getStateArr();
                                    double reward,max_Q,new_Q;
                                    //new^^^^^^
                                    if (iRand==learnerBoatNum){
                                        //update state value
                                        action = 1;
                                        int[] nextState = bLearner.getBoatWorld().getNextState(action, currState);
                                        
                                    //    double reward = bLearner.getBoatWorld().getNextReward(action,currState);
                                        double this_Q = bLearner.getBoatWorld().getPolicy().getQValue( currState, action );
                                      //  double max_Q = bLearner.getBoatWorld().getPolicy().getMaxQValue( nextState );

                                 //       System.out.println("reward: "+reward+" thisQ: "+this_Q+" maxQ: "+max_Q);
                                       
                                    //    double new_Q = this_Q + bLearner.getBoatWorld().getAlpha() * ( reward + bLearner.getBoatWorld().getGamma() * max_Q - this_Q );
                                  //      bLearner.getIntStat().updateTotalRew(new_Q,reward);
                                   //     bLearner.getBoatWorld().getPolicy().setQValue( currState, action, new_Q );
                                        
                                        //update current state to the new state
                                   //     bLearner.getIntStat().updateVal(nextState[0], nextState[1], nextState[2]);
                                        //when balking there is some prob of failure
                                        switch(rType){
                                            case 1:
                                                if (ran <= bLearner.getIntStat().P_Battery){//fail 0.9
                                                    terminate = true;
                                                }
                                                //sim should be stopped
                                                break;
                                            case 2:
                                                if (ran <= bLearner.getIntStat().P_DangeArea){//fail 0.4
                                                    terminate = true;
                                                }
                                                //sim should be stopped
                                                break;
                                            case 3:
                                                if (ran <= bLearner.getIntStat().P_Connection){//fail 0.2
                                                    terminate = true;
                                                }
                                                //sim should be stopped
                                                break;
                                            default:
                                                System.out.println("No Failure happend");  
                                                terminate = false;    
                                                break;
                                        }
                                        //update q value
                                        if (terminate){
                                            reward = bLearner.getBoatWorld().getRFail();
                                           // new_Q = reward;
                                            //bLearner.getIntStat().updateTotalRew(reward);
//                                            this_Q = bLearner.getBoatWorld().getPolicy().getQValue( currState, action );
//                                            max_Q = bLearner.getBoatWorld().getPolicy().getMaxQValue( nextState );
//                                            System.out.println("reward: "+reward+" thisQ: "+this_Q+" maxQ: "+max_Q);
//                                            
                                            new_Q = this_Q + bLearner.getBoatWorld().getAlpha() * ( reward - this_Q );//it's a final state
                                            bLearner.getIntStat().updateTotalRew(new_Q,reward);
                                            bLearner.getBoatWorld().getPolicy().setQValue( currState, action, new_Q );
                                            //update current state to the new state
                                            bLearner.getIntStat().updateVal(5, nextState[1], nextState[2]);//fail
                                        }
                                        else{
                                            reward = bLearner.getBoatWorld().getRTask();
                                           // this_Q = bLearner.getBoatWorld().getPolicy().getQValue( currState, action );
                                            max_Q = bLearner.getBoatWorld().getPolicy().getMaxQValue( nextState );
                                        //    System.out.println("reward: "+reward+" thisQ: "+this_Q+" maxQ: "+max_Q);
                                            
                                            new_Q = this_Q + bLearner.getBoatWorld().getAlpha() * ( reward + bLearner.getBoatWorld().getGamma() * max_Q - this_Q );                          
                                      //      new_Q+= reward;
                                            bLearner.getIntStat().updateTotalRew(new_Q,reward);
                                            bLearner.getBoatWorld().getPolicy().setQValue(currState, action, new_Q);
                                            bLearner.getIntStat().updateVal(nextState[0], nextState[1], nextState[2]);
                                        }
                                    }
                                    //boat #iRand balked a req of type serviceType
                        //            TypeofRequest.PrintServiceDetail();
                                 }

                                iter++;
                            }
                           // iter++;
                            nextArrival +=StdRandom.exp(lambda);
                        }
                        // it's a departure
                        else{
                            Request req = null;

                       //     System.out.println("queueOfReq size: "+queueOfReq.size());  
                            req = new Request(queueOfReq.peek().iBoatId,queueOfReq.peek().dArrivalTime,queueOfReq.peek().dDepartureTime,queueOfReq.peek().dServiceTime,queueOfReq.peek().iServiceType);
                     //       System.out.println("Removed: boat number "+req.iBoatId+" arrivalTime "+req.dArrivalTime+ " DepartureTime "+req.dDepartureTime+ " ServiceType "+req.iServiceType);
                            queueOfReq.remove();
                            
                            //new^^^^^
                            //if learner
                            if (req.iBoatId == learnerBoatNum ){
                                //update state
                                bLearner.getIntStat().updateVal(0, remains, queueOfReq.size());//normal
                            }
                            
                            //^^^^^^^^^^
                            //the boat should resume the path
                            BoatProxy inQB = tokenProxies.get(req.iBoatId);
                            ArrayList<Position> currTasksPositions = new ArrayList<Position>();
                            Set<BoatMarker> boatSet = decisions.keySet();
                            BoatMarker bMarker;
                            for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
                                bMarker = it.next();
                                if (bMarker.getProxy() == inQB) {
                                    currTasksPositions.addAll(decisions.get(bMarker));
                                    break;
                                } 
                            }

                            Hashtable<ProxyInt, Path> proxyPathTemp = new Hashtable<ProxyInt, Path>();

                            ArrayList<Location> tasksLocations = Conversion.positionToLocation(currTasksPositions);
 
                            proxyPathTemp.put(inQB, new PathUtm(tasksLocations));

                            ProxyExecutePath oEv1 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPathTemp);

                    //        System.out.println("boat "+req.iBoatId + " resumed");

                            inQB.handleEvent(oEv1);

                            //***********************************
                            double wait = nextDeparture - req.dArrivalTime;
//                            totalWaitingTime += wait;
//                            numCustomersServed++;

                            if (queueOfReq.isEmpty()) nextDeparture = Double.POSITIVE_INFINITY;
                            else nextDeparture += StdRandom.exp(mu); //nextDeparture += StdRandom.gaussian(mu,100);

                        }
                       
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        if (bLearner.getBoatWorld().endState(bLearner.getIntStat().getStateArr())){//TERMINAL STATE
                            endSim = true;
                            break;
                        }
                    }
                    System.out.println("numCustomersBalked: "+numCustomersBalked);
                    bLearner.getIntStat().showTotalRew();
                    bLearner.getIntStat().saveTotalAccRew();
                    bLearner.getIntStat().saveTotalRew();
                    //simulation has been ended
                    bLearner.getBoatWorld().getPolicy().saveQValues();
 
                    PlanManager p = Engine.getInstance().getPlans().get(0);
                    p.abortMission();
                    //Engine.getInstance().abort(p);
                    System.exit(0);
                    //the whole plan should be aborted
                }
            }).start();
            
            QueueLearningDone responseEvent = new QueueLearningDone(oe.getId(), oe.getMissionId(),tokenProxies);

            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                listener.eventGenerated(responseEvent);
            }
            
        }else if (oe instanceof QueueManagement){
            System.out.println("QueueManagement");
            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy()); 
                    numProxies++;
                }
            }
            final int numofProxies = numProxies;
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "QueueManagement has no relevant proxies attached: " + oe);
            }

// this thread will generate arrival and take care of pop and push to the queue
            (new Thread() {
                @Override
                public void run() {
 
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Queue parameters
                    int maxIter = 20;//(int) (Math.random() * (40 - 30)) + 30;
                    boolean balk = true;//balk property is available if set to true
                    double lambda = 0.00025;//StdRandom.uniform(0.00010, 0.00040);//0.00025;//arrival rate
                    double mu = StdRandom.uniform(0.00011,0.00043);//0.00027;//service rate // rather to be in a range
                    boolean bDiscipline = true;//true:FIFO; false:SJF
                    boolean bLocal = false; //if true only local state else locGlob
                    int stateType = 1;//0: localOnly; 1: localGlobal; 2: fullState
                    double waitingCost;//cost of waiting in the system (both queue and server)
                    double alpha = 20/mu;//1/(2*mu);
                    double betta = 18;//0.24;//total number of tasks for each boat
                    double serviceReward = 0.0;//reward of completing a service
                    double balkThreshold = 0.0; //= Math.ceil(serviceReward/(mu*waitingCost));  //2;   
                    double totalThresholdValue = 0.0;
                    final int numType = 3;
                    int remains = 0;//num of remaining tasks for learner boat
                    final int setupTime = (int) (Math.random() * (30 - 25)) + 25;//27;
                    final int SA_Learning = 0;//Multi-Agent = 0; single-agent learning = 1; dynamic thresh = 2
                    // Single-agent QLearning parameter for boat 1 with index 0;
                    final int learnerBoatNum = 0;
                    BoatProxy bLearner;
                    if (SA_Learning==1) //single-agent learning
                        bLearner = tokenProxies.get(learnerBoatNum);

                    double totalRew = 0.0;
                    double totalAccRew = 0.0;
                    double totalTeamAccRew = 0.0;
                    double totalTeamRew = 0.0;
                    
                    class Service{
                        int serviceType; //{1:recharge, 2:DangeArea, 3:connecLost }
                        double[] probF = {0.9,0.4,0.2};
                        public Service(int iType){
                            this.serviceType = iType;
                        }
                        public int getServType(){
                            return this.serviceType;
                        }
                        public void setServType(int t){
                            this.serviceType = t;
                        }
                        public double getProbF(){
                            return probF[serviceType-1];
                        }
                        public double getServReward(){
                            return getProbF();
                        }
                        public void PrintServiceDetail(){
                            int type = this.serviceType;
                            switch(type){
                                case 1:
                                    System.out.println("Service Type: RECHARGE with prob(f):"+this.probF[type-1]);
                                    break;
                                case 2:
                                    System.out.println("Service Type: DANGEAREA with prob(f):"+this.probF[type-1]);
                                    break;
                                default:
                                    System.out.println("Service Type: RECHARGE with prob(f):"+this.probF[type-1]);
                                    break;      
                            }
                        }
                    } 
                    class Request{
                        public int iBoatId;
                        public double dArrivalTime;
                        public double dServiceTime;
                        public double dDepartureTime;
                        public int iServiceType;
                        private Request(int iNo, double arrTime, double depTime,double servTime,int servType) {
                            iBoatId = iNo;
                            dArrivalTime = arrTime; 
                            dServiceTime = servTime;
                            dDepartureTime = depTime;
                            iServiceType = servType;
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    }
                    class ReqComparator implements Comparator<Request>{

                        public int compare(Request r1, Request r2) {
                            if (r1.dServiceTime > r2.dServiceTime) return 1;
                            if (r1.dServiceTime < r2.dServiceTime) return -1;
                            return 0;
                        }
                    }
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Queue setup
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    double nextArrival;// = StdRandom.exp(lambda); // time of next arrival
                    double nextDeparture = Double.POSITIVE_INFINITY; // time of next departure   
                    Queue<Request> queueOfReq = new LinkedList<Request>();//for now, we just keep the boat's number in the Q
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    ReqComparator priorityComp = new ReqComparator();
                    PriorityQueue<Request> sjfQueue = new PriorityQueue<Request>(11,priorityComp);
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // Statistics
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    String strName = "";
                    if (balk) // for the last paper (mid sep.), balk should always be set to true
                    { 
                        if (SA_Learning==1) strName = "/Users/Masoume/Desktop/Results-QLearning/Experiment/SALearning/results.txt";
                        else if (SA_Learning==0) strName = "/Users/Masoume/Desktop/Results-QLearning/Experiment/MALearning/gLocal-B-new-60.txt";
                        else strName = "/Users/Masoume/Desktop/Results-QLearning/Experiment/resultsDynaBalk_Final.txt";
//                        if (SA_Learning==1)strName = "/Users/Masoume/Desktop/ICAR2017/Experiment/resultsSABalk_Final.txt";
//                        else if (SA_Learning==0) strName = "/Users/Masoume/Desktop/ICAR2017/Experiment/resultsMABalk_Final.txt";
//                        else strName = "/Users/Masoume/Desktop/ICAR2017/Experiment/resultsDynaBalk_Final.txt";
                    }else if (bDiscipline) {strName = "/Users/Masoume/Desktop/Results-QLearning/Experiment/resultsSA-FIFO.txt";}
                    else if (!bDiscipline){strName = "/Users/Masoume/Desktop/Results-QLearning/Experiment/resultsSA-SJF.txt";}
                 //   String inData = "maxIteration: "+maxIter+", lambda: "+lambda+", mu:"+mu;
                    
//                    int h = countFile();
//                    System.out.println("Dataset "+h);
//                    double[][] inputData = loadDataSet(h);
//                    for (int i=0;i<30;i++){
//                        for (int j=0;j<3;j++) //arr-t type serv-t
//                            System.out.print(inputData[i][j]+ " ");
//                        System.out.println();
//                    }
                    
                  //  String inData = "DataSet: "+ h +"maxIteration: "+maxIter+", lambda: "+lambda+", mu:"+mu;
                    
                   //System.out.println("inData "+inData);
                    String strN = "/Users/Masoume/Desktop/Results-QLearning/Experiment/failStatistics-B-new-60.txt";
                    
                   // resultsFile(strN,"Type1    Type2   Type3");
               //     resultsFile(strName,inData);
                    class BoatStatistic{//for one boat
                        
                        public int[][] iCounter = new int[3][2];//for each servType{0,1,2} #occurence & #balk 
                        private BoatStatistic(){
                            for (int i=0;i<numType;i++)
                                for (int j=0;j<2;j++)
                                    iCounter[i][j]=0;
                        }
                    }
                    BoatStatistic[] finalStatistics = new BoatStatistic[numofProxies];//should be equal to no boats
                    double totalWaitingTime = 0.0;//sum of the waiting times for all customers
                    int numCustomersServed = 0;
                    int numCustomersBalked = 0;
                    int iter = 0;
                    int iRand = 0;
                    double serviceTime;// = Double.POSITIVE_INFINITY;
                    boolean bFF = false;
                  //double tempNDep = Double.POSITIVE_INFINITY;
                    StopWatch timer = new StopWatch();
                    timer.start();
                    
                    while (timer.getElapsedTimeSecs()<setupTime);//setup times

                    int totalNumTasks = 0;
                    if (SA_Learning==1){//for MA learning initialze in line 1579
                        bLearner.initQLearning(stateType,mu,lambda);//if true only local state
                        totalNumTasks = tokenProxies.get(learnerBoatNum).getCurrentWaypoints().size();
                        bLearner.getIntStat().updateVal(0, totalNumTasks, 0);//initial values for learner (normal,tasks,qLength)
                    }
                    //(arrival, service type, service time) should be read from the file: new 18 Sep
//                    int h = countFile();
//                    System.out.println("h "+h);
//                    double[][] inputData = loadDataSet(h);
//                    for (int i=0;i<30;i++){
//                        for (int j=0;j<3;j++)
//                            System.out.print(inputData[i][j]+ " ");
//                        System.out.println();
//                    }
                    nextArrival = timer.getElapsedTimeSecs() + StdRandom.exp(lambda);
               //     nextArrival = inputData[iter][0];

                    //Random selection of boats removing repetitive generations
                    ArrayList<Integer> boatList = new ArrayList<Integer>();//line 1157
                    int[] boatFailed = new int[numofProxies];//failure boat(s) in each run
                    for (int i=0; i<numofProxies; i++) {
                        boatFailed[i]=0;
                        boatList.add(new Integer(i));
                        finalStatistics[i] = new BoatStatistic();//initialze statistics for all boats
                        // initializing multi-agent learning params
                        if (SA_Learning==0){ 
                            tokenProxies.get(i).initQLearning(stateType,mu,lambda);  //if true only local state 
                            totalNumTasks = tokenProxies.get(i).getCurrentWaypoints().size();
                            System.out.println("totalNumTasks "+totalNumTasks);
                            tokenProxies.get(i).getIntStat().updateVal(0, totalNumTasks, 0);
                        }
                    }
                    int currIter = 0;
                    while (iter<maxIter || !queueOfReq.isEmpty() || !sjfQueue.isEmpty()){
                        balkThreshold = 0.0;
                        // it's an arrival
                        if ((nextArrival <= nextDeparture)&&(iter<maxIter)) {
                            if (!boatList.isEmpty()){
                                Collections.shuffle(boatList);
                                iRand = boatList.remove(0);
                                System.out.println("Next request from boat " +iRand+" arrived at time: "+nextArrival);
                                bFF = false;
                            }
                            else{
                                for (int i=0; i<numofProxies; i++) {//there might be consideration when a boat finishes its tasks
                                    boatList.add(new Integer(i));                                  
                                }
                                // remove boats which are already in the queue
                                if (bDiscipline){ //if FIFO
                                    if (!queueOfReq.isEmpty()){
                                        for (Request e : queueOfReq) {
                                            int x;
                                            System.out.println("already inside queue e.iBoatId: "+e.iBoatId);
                                            for (int j=0;j<boatList.size();j++){ 
                                                if (e.iBoatId==boatList.get(j)){
                                                    x = boatList.remove(j);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                else { //if SJF
                                    if (!sjfQueue.isEmpty()){
                                        for (Request e : sjfQueue) {
                                            int x;
                                            System.out.println("e.iBoatId: "+e.iBoatId);
                                            for (int j=0;j<boatList.size();j++){ 
                                                if (e.iBoatId==boatList.get(j)){
                                                    x = boatList.remove(j);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!boatList.isEmpty())
                                {
                                    for (int k=0;k<boatList.size();k++)
                                        System.out.println(boatList.get(k));
                                    Collections.shuffle(boatList);
                                    iRand = boatList.remove(0);
                                    System.out.println("Next request from boat " +iRand+" arrived at time: "+nextArrival);
                                    bFF=false;
                                }
                                else {bFF = true;}
                            }
                            if (bFF==false){
                                serviceTime = StdRandom.exp(mu);//should be read from dataset
//                                if (iter<maxIter)
//                                    serviceTime = inputData[iter][2];
//                                else
//                                    serviceTime = StdRandom.exp(mu);
                                
                                if (bDiscipline){
                                    if (queueOfReq.isEmpty()) nextDeparture = nextArrival + serviceTime;
                                }
                                else{
                                    if (sjfQueue.isEmpty()) nextDeparture = nextArrival + serviceTime;
                                }
                                // serviceTime = StdRandom.gaussian(mu, 100);
                               // if (queueOfReq.isEmpty()) nextDeparture = nextArrival + StdRandom.gaussian(mu, 100);

                                //generate a random serviceType {1,2,3}
                                Service TypeofRequest = new Service((new Random()).nextInt(3)+1);
                                
//                                if (iter<maxIter)
//                                    TypeofRequest.setServType((int)inputData[iter][1]);
                          
                                saveDataSet(nextArrival,TypeofRequest.getServType(),serviceTime);
                                
                                int action = -1;
                                BoatProxy bTmp = tokenProxies.get(iRand);
                                if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
                                    remains = bTmp.getCurrentWaypoints().size();
                                    //update state from normal to the type of request 
                                    bTmp.getIntStat().updateVal(TypeofRequest.getServType(), remains, queueOfReq.size());
                                }
                                //computing the threshold value for the new arrival
                                if (balk){
                                    //based on table value learnt before   
                                   // if ((!SA_Learning)||(iRand==learnerBoatNum)){//in multi-agent learning case
                                    if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
                                        action = bTmp.getBoatWorld().getPolicy().getBestAction(bTmp.getIntStat().getStateArr());
                                        System.out.println("Best action is "+action);
                                        bTmp.getIntStat().printCurrentState();
                                    }
                                    else{//other agents // compute threshold value
                                        serviceReward = alpha*(TypeofRequest.getServReward());
                                        int tTmp = tokenProxies.get(iRand).getCurrentWaypoints().size();
                                        if (tTmp==0) tTmp = 1;
                                        waitingCost = betta/(tTmp);
                                        if (waitingCost==0) waitingCost = 1;
                                        balkThreshold = Math.ceil((serviceReward*mu)/waitingCost);
                                        totalThresholdValue+=balkThreshold;
                                        System.out.println("balkThreshold = "+balkThreshold);
                                    }
                                }
                                finalStatistics[iRand].iCounter[TypeofRequest.getServType()-1][0]+=1;//boat #iRand made a req of type reqType
                                if (bDiscipline){//FIFO
                                    BoatProxy tmpProxy = tokenProxies.get(iRand);
//                                    if ((!balk) || (queueOfReq.size()< balkThreshold) || (queueOfReq.isEmpty()) || (action==0)){ 
//                                        //new^^^^^
//                                        if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
//                                            if (action==0){
//                                                    //update state value
//                                                    int[] nextState = tmpProxy.getBoatWorld().getNextState(action, tmpProxy.getIntStat().getStateArr());
//                                                    tmpProxy.getIntStat().updateVal(4, nextState[1], nextState[2]);//4=wait 
//                                                    
////                                                    queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
////                                                    tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));
//                                            }
////                                            else{//action==1 but Q is empty so it is a balk
////                                                //update state value
////                                                int[] nextState = tmpProxy.getBoatWorld().getNextState(action, tmpProxy.getIntStat().getStateArr());
////                                                tmpProxy.getIntStat().updateVal(nextState[0], nextState[1], nextState[2]);
////                                                //balk happend
////                                                numCustomersBalked++;
////                                                boatList.add(iRand);
////                                                //boat #iRand balked a req of type serviceType
////                                                finalStatistics[iRand].iCounter[TypeofRequest.getServType()-1][1]+=1;
////                                            }
//                                        }                                        
//                                        queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
//                                        tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));
//                                       
//                                    }
                                    //new version with multiple learners at the same time
                                    if ((!balk) || (queueOfReq.size()< balkThreshold) || (action==0)){
                                        if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
                                            //update state value
                                            int[] nextState = tmpProxy.getBoatWorld().getNextState(action, tmpProxy.getIntStat().getStateArr());
                                            
                                            double reward = tmpProxy.getBoatWorld().getNextReward(action,tmpProxy.getIntStat().getStateArr(),nextState,serviceTime,queueOfReq.size());
                                            double this_Q = tmpProxy.getBoatWorld().getPolicy().getQValue( tmpProxy.getIntStat().getStateArr(), action );
                                            tmpProxy.getIntStat().updateTotalRew(this_Q, reward);
                                            
                                            tmpProxy.getIntStat().updateVal(4, nextState[1], tmpProxy.getIntStat().getqsize()+1);//4=wait // needs to be fixed
                                        }                                       
                                        queueOfReq.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
                                        tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));

                                    }
                                    else{//new version with multiple learners at the same time
                                        int rType = TypeofRequest.getServType();//tmpProxy.getIntStat().getReqTypeInt();
                                        boolean fail = false;
                                        int[] nextState;
                                        //check if it's Learning
                                        if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
                                            //update state value
                                            rType = tmpProxy.getIntStat().getReqTypeInt();
                                            nextState = tmpProxy.getBoatWorld().getNextState(action, tmpProxy.getIntStat().getStateArr());
                                            
                                            double reward = tmpProxy.getBoatWorld().getNextReward(action,tmpProxy.getIntStat().getStateArr(),nextState,serviceTime,queueOfReq.size());
                                            double this_Q = tmpProxy.getBoatWorld().getPolicy().getQValue( tmpProxy.getIntStat().getStateArr(), action );
                                            tmpProxy.getIntStat().updateTotalRew(this_Q, reward);
                                            
                                            tmpProxy.getIntStat().updateVal(nextState[0], nextState[1], tmpProxy.getIntStat().getqsize()); // needs to be fixed
                                                                                
                                            if (nextState[0]==5){ //for MA and SA should be added if
                                                boatFailed[iRand]++;
                                            }
                                            else{
                                                System.out.println("No Failure happend"); 
                                            }
                                        }
                                        boatList.add(iRand);
                                        numCustomersBalked++;
                                        //boatList.add(iRand);
                                        //boat #iRand balked a req of type serviceType
                                        finalStatistics[iRand].iCounter[TypeofRequest.getServType()-1][1]+=1;
                                    }
//                                    else{ //balk//action==1
//                                        Random rand = new Random(); 
//                                        int ran = rand.nextInt(10)+1; //generate random num for probF
//                                        System.out.println("ran fail = "+ran);
//                                        int rType = TypeofRequest.getServType();//tmpProxy.getIntStat().getReqTypeInt();
//                                        boolean fail = false;
//                                        int[] nextState;
//                                        //check if it's Learning
//                                        if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum))){
//                                            //update state value
//                                            rType = tmpProxy.getIntStat().getReqTypeInt();
//                                            nextState = tmpProxy.getBoatWorld().getNextState(action, tmpProxy.getIntStat().getStateArr());
//                                            tmpProxy.getIntStat().updateVal(nextState[0], nextState[1], tmpProxy.getIntStat().getqsize()); // needs to be fixed
//                                        }   
//                                        switch(rType){
//                                            case 1:
//                                                //if (ran <= tmpProxy.getIntStat().P_Battery){//fail 0.9
//                                                if (ran <= 9){//fail 0.9
//                                                    fail = true;
//                                                }
//                                                //sim should be stopped
//                                                break;
//                                            case 2:
//                                                //if (ran <= tmpProxy.getIntStat().P_DangeArea){//fail 0.4
//                                                if (ran <= 4){
//                                                    fail = true;
//                                                }
//                                                //sim should be stopped
//                                                break;
//                                            case 3:
//                                                //if (ran <= tmpProxy.getIntStat().P_Connection){//fail 0.2
//                                                if (ran <= 2){
//                                                    fail = true;
//                                                }
//                                                //sim should be stopped
//                                                break;
//                                            default:
//                                                System.out.println("No Failure happend");  
//                                                fail = false;    
//                                                break;
//                                        }
//                                        if (fail){ //for MA and SA should be added if
//                                            if ((SA_Learning==0)||((SA_Learning==1) && (iRand==learnerBoatNum)))
//                                                {tmpProxy.getIntStat().setReqType(5);}//fail
//                                            boatFailed[iRand]++;
//                                        }
//                                        boatList.add(iRand);
//                                        numCustomersBalked++;
//                                        //boatList.add(iRand);
//                                        //boat #iRand balked a req of type serviceType
//                                        finalStatistics[iRand].iCounter[TypeofRequest.getServType()-1][1]+=1;
//                                    }
                                }
                                else{ //SJF
                                    if ((!balk) || (sjfQueue.size()< balkThreshold) || (sjfQueue.isEmpty())){ 
                                        Request head;
                                        if (sjfQueue.size()==1) 
                                            head = sjfQueue.remove();
                                        sjfQueue.add(new Request(iRand,nextArrival,nextDeparture,serviceTime,TypeofRequest.getServType()));
                                        //update departure time
                                        Request r = sjfQueue.peek();
                                        if (r.dArrivalTime == nextArrival){//the new arrival is in head of the q
                                            r.dDepartureTime = r.dArrivalTime + r.dServiceTime;
                                            nextDeparture = r.dDepartureTime;
                                        }
                                        tokenProxies.get(iRand).handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));                              
                                    }
                                    else{
                                        numCustomersBalked++;
                                        boatList.add(iRand);
                                        //boat #iRand balked a req of type serviceType
                                        finalStatistics[iRand].iCounter[TypeofRequest.getServType()-1][1]+=1;
                                    }
                                }
                                iter++;                               
                            }
                            nextArrival +=StdRandom.exp(lambda);
//                            if (iter != currIter)
//                                nextArrival = inputData[iter][0];
//                            else if (iter<maxIter)
//                                nextArrival = inputData[iter+1][0];
//                            else 
//                                nextArrival +=StdRandom.exp(lambda);
//                            currIter = iter;
                        }
                        // it's a departure
                        else{
                            Request req = null;
                            if (!queueOfReq.isEmpty()){ // for the previous experiments, the SJF queue also needs to be checked
                                if (bDiscipline){
                                    System.out.println("queueOfReq size: "+queueOfReq.size());  
                                    req = new Request(queueOfReq.peek().iBoatId,queueOfReq.peek().dArrivalTime,queueOfReq.peek().dDepartureTime,queueOfReq.peek().dServiceTime,queueOfReq.peek().iServiceType);
                                    System.out.println("whome is removed: boat number "+req.iBoatId+" arrivalTime "+req.dArrivalTime+ " DepartureTime "+req.dDepartureTime+ " ServiceType "+req.iServiceType);
                                    queueOfReq.remove();
                                    //new^^^^^
                                    //if learner
                                    if ((SA_Learning==0)||((SA_Learning==1) && (req.iBoatId==learnerBoatNum))){//if MA or single
                                        System.out.println("remains: "+remains);
                                        System.out.println("prev remains: "+tokenProxies.get(req.iBoatId).getIntStat().getNumOfRemainingTasks());
                                        //remains = tokenProxies.get(req.iBoatId).getCurrentWaypoints().size()
                                        tokenProxies.get(req.iBoatId).getIntStat().updateVal(0, remains, queueOfReq.size());//normal
                                    }
                                    //^^^^^^^^^^
                                }
                                else{
                                    System.out.println("sjfQueue size: "+sjfQueue.size());  
                                    req = sjfQueue.peek();
                                    nextDeparture = req.dDepartureTime;
                                    //req = new Request(sjfQueue.peek().iBoatId,sjfQueue.peek().dArrivalTime,sjfQueue.peek().dDepartureTime,sjfQueue.peek().dServiceTime,sjfQueue.peek().iServiceType);
                                    System.out.println("whome is removed: boat number "+req.iBoatId+" arrivalTime "+req.dArrivalTime+ " DepartureTime "+req.dDepartureTime+" ServiceTime " +req.dServiceTime+" ServiceType "+req.iServiceType);
                                    sjfQueue.remove();
                                }
                                //the boat should resume the path
                                BoatProxy inQB = tokenProxies.get(req.iBoatId);
                                ArrayList<Position> currTasksPositions = new ArrayList<Position>();
                                Set<BoatMarker> boatSet = decisions.keySet();
                                BoatMarker bMarker;
                                for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
                                    bMarker = it.next();
                                    if (bMarker.getProxy() == inQB) {
                                        currTasksPositions.addAll(decisions.get(bMarker));
                                        break;
                                    } 
                                }

                                Hashtable<ProxyInt, Path> proxyPathTemp = new Hashtable<ProxyInt, Path>();

                                ArrayList<Location> tasksLocations = Conversion.positionToLocation(currTasksPositions);
                                System.out.println("current tasks list: " + currTasksPositions);

                                proxyPathTemp.put(inQB, new PathUtm(tasksLocations));

                                ProxyExecutePath oEv1 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPathTemp);

                                System.out.println("boat "+req.iBoatId + " resumed");

                                inQB.handleEvent(oEv1);

                                //***********************************
                                double wait = nextDeparture - req.dArrivalTime;
                                totalWaitingTime += wait;
                                numCustomersServed++;

                                if (bDiscipline){
                                    if (queueOfReq.isEmpty()) nextDeparture = Double.POSITIVE_INFINITY;
                                    else nextDeparture += StdRandom.exp(mu); //nextDeparture += StdRandom.gaussian(mu,100);
                                }
                                else{
                                    if (sjfQueue.isEmpty()) nextDeparture = Double.POSITIVE_INFINITY;
                                    //else nextDeparture += StdRandom.exp(mu); //nextDeparture += StdRandom.gaussian(mu,100);
                                    else {
                                        nextDeparture += sjfQueue.peek().dServiceTime;
                                        sjfQueue.peek().dDepartureTime = nextDeparture;
                                    } //nextDeparture += StdRandom.gaussian(mu,100);
                                }
                            }
                            else {nextDeparture = Double.POSITIVE_INFINITY;}
                        }                       
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BoatProxy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //print queue statistics
                    int totalNumReq = numCustomersServed+numCustomersBalked;
                    
                    resultsFile(strName,"Total No. of requests arrived: "+ totalNumReq);
                    resultsFile(strName,"No. of requests served: "+ numCustomersServed);
                    resultsFile(strName,"No. of requests balked: "+ numCustomersBalked);
                 //   if (totalNumReq>0) resultsFile(strName,"Avg threshold value: "+ totalThresholdValue/totalNumReq);
                    resultsFile(strName,"Total waiting time: "+totalWaitingTime + " min");
                    if (numCustomersServed>0) resultsFile(strName,"Avg waiting time: "+totalWaitingTime/numCustomersServed + " min");
                    resultsFile(strName,"Failure Boats: "+boatFailed[0]+", "+boatFailed[1]+", "+boatFailed[2]+", "+boatFailed[3]+", "+boatFailed[4]);
                    for (int i=0;i<numofProxies;i++){
                       totalTeamAccRew += tokenProxies.get(i).getIntStat().getTotalAccRew();
                       totalTeamRew += tokenProxies.get(i).getIntStat().getTotalRew();
                     //  System.out.println("Boat "+i);
                     //  tokenProxies.get(i).getIntStat().showTotalRew();
                    }
                    resultsFile(strName,"Total team acc. rew: "+totalTeamAccRew);
                    resultsFile(strName,"Total team rew: "+totalTeamRew);
                    
                    //some extra statistics
                    int[] balkStatistics= new int[numType];//#balk of each req
                    for (int k=0;k<numType;k++)//num of req
                        balkStatistics[k] = 0;
                    for (int i=0;i<numofProxies;i++){
                        for (int j=0;j<numType;j++){//diff serv type
                            String reqName;
                            if (j==0)
                                reqName = "Recharge";
                            else if (j==1)
                                reqName = "DangeArea";
                            else
                                reqName = "ConnLost";  

                            balkStatistics[j]+=finalStatistics[i].iCounter[j][1];
                            resultsFile(strName,"Boat "+i+" ReqType "+ reqName+" #TotalReq: "+ finalStatistics[i].iCounter[j][0]+ " #balk: "+finalStatistics[i].iCounter[j][1]);                            
                        }
                    }
                    String stat=""; 
                    for (int k=0;k<numType;k++)//num of serv type
                    {
                        System.out.println("balkStatistics["+k+"]="+balkStatistics[k]);
                        stat = stat.concat(balkStatistics[k]+"   ");
                    }
                    resultsFile(strN,stat);
                    
                    resultsFile(strName,"====================================");
                    
                    PlanManager p = Engine.getInstance().getPlans().get(0);
                    p.abortMission();
                    //Engine.getInstance().abort(p);
                    System.exit(0); 
                    
                    //BoatProxy inQB = tokenProxies.get(req.iBoatId);
                    //ArrayList<Position> currTasksPositions = new ArrayList<Position>();
//                    Set<BoatMarker> boatSet = decisions.keySet();
//                    int countSet = 0;
//                    int setSize = boatSet.size();
//                    System.out.println("setSize "+setSize);
//                    BoatMarker bMarker;
//                    while (countSet<setSize){
//                        countSet = 0;
//                        for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
//                            bMarker = it.next();
//                            if (bMarker.getProxy().getCurrentWaypoints().isEmpty()) {
//                                countSet++;
//                            } 
//                        }
//                    }
//                    System.out.println("countSet "+countSet);
//                    //the whole plan should be aborted
//                    // means all boats completed their tasks
//                    PlanManager p = Engine.getInstance().getPlans().get(0);
//                    p.abortMission();
//                    //Engine.getInstance().abort(p);
//                    System.exit(0);
                    
                }
            }).start();
            
            QueueManagementDone responseEvent = new QueueManagementDone(oe.getId(), oe.getMissionId(),tokenProxies);

            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                listener.eventGenerated(responseEvent);
            }
        }else if (oe instanceof CheckOperatorAvailable) {
            
            System.out.println("CheckOperatorAvailable");
            
            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                   // System.out.println("Color "+ tokenProxies.get(numProxies).getColor()); 
                    numProxies++;
                }
            }
            
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "CheckOperatorAvailable has no relevant proxies attached: " + oe);
            }
//            
            double dRand = Math.random();//between 0 and 1
            
//            int iRand = new Random().nextInt((1 - 0) + 0);
//            iRand =2;
//            if (iRand == 2){
//                System.out.println("CheckOperatorAvailable here gfdsssfghj");
//                OperatorAvailable responseEvent = new OperatorAvailable(oe.getId(), oe.getMissionId(),tokenProxies);
//
//                for (GeneratedEventListenerInt listener : listeners) {
//                    LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
//                    listener.eventGenerated(responseEvent);
//                }
//            }
//            else{
//                
//                OperatorUnavailable responseEvent = new OperatorUnavailable(oe.getId(), oe.getMissionId(),tokenProxies);
//
//                for (GeneratedEventListenerInt listener : listeners) {
//                    LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
//                    listener.eventGenerated(responseEvent);
//                }
//            }
               
        }else if  (oe instanceof OperatorSelectCheckPointsArea) {
            System.out.println("OperatorSelectCheckPointsArea");
            final int noOfCheckPoints = ((OperatorSelectCheckPointsArea) oe).getNoOfCheckPoints();

            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;

                }
            }
          //  final int numofProxies = numProxies;
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "OperatorSelectCheckPointsArea has no relevant proxies attached: " + oe);
            }
            
           // final ProxyInt foundB1 = tokenProxies.get((new Random()).nextInt(tokenProxies.size()));
            int count = 0;
            final ProxyInt foundB1 = tokenProxies.get(0);
          
            OperatorSelectsCheckPointsArea responseEvent = new OperatorSelectsCheckPointsArea(oe.getId(), oe.getMissionId(), tokenProxies, noOfCheckPoints);

            System.out.println("RESPONSE CREATED");

            for (GeneratedEventListenerInt listener : listeners) {
               // System.out.println("Jooooon *&*%*%^*^%&*^&");
                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                listener.eventGenerated(responseEvent);
            }
            
            
        }else if (oe instanceof OperatorSelectDangerousArea) {

            System.out.println("OperatorSelectDangerousArea");
            final Area2D area = ((OperatorSelectDangerousArea) oe).getArea();

            final BatchType batchtype = BatchType.STANDARD;
            ArrayList<Position> positions = new ArrayList<Position>();
            for (Location location : area.getPoints()) {
                positions.add(Conversion.locationToPosition(location));
            }
            
            System.out.println("Dangerous Area Four Points: " + positions);

            int numProxies = 0;
            final ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;

                }
            }
            final int numofProxies = numProxies;
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "OperatorSelectDangerousArea has no relevant proxies attached: " + oe);
            }

            //Thread for controlling this area
            (new Thread() {
                @Override
                public void run() {
                    boolean flag = true;
                    int[] bList = new int[numofProxies];//catch if it is the first time that the boat enters area
                    while (true) {

//                  //CHECK IF A BOATS ENTERED THE DANGEROUS AREA
                        for (int proxyIndex = 0; proxyIndex < numofProxies; proxyIndex++) {

                            Boolean isInside = isPointInside(area, tokenProxies.get(proxyIndex).getPosition());

                            if (((isInside) && (bList[proxyIndex] > 0)) || ((!isInside) && (bList[proxyIndex] == 0))) {
                                continue;
                            }
                            if ((!isInside) && (bList[proxyIndex] > 0))//boat exited the area
                            {
                                bList[proxyIndex] = 0;
                                //An event should be sent
                                BoatOutsideArea responseEvent = new BoatOutsideArea(new ArrayList<BoatProxy>(Arrays.asList(tokenProxies.get(proxyIndex))), area);
                                //                           BoatOutsideArea responseEvent = new BoatOutsideArea(new ArrayList<BoatProxy>(Arrays.asList(tokenProxies.get(proxyIndex))));          
                                tokenProxies.get(proxyIndex).setIntrSt(0);//added for MDP part
                                System.out.println("Boat " + proxyIndex + " Left Dangerous Area" + " and its Position is " + tokenProxies.get(proxyIndex).getPosition());
                                for (GeneratedEventListenerInt listener : listeners) {
                                    LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                                    listener.eventGenerated(responseEvent);
                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(BoatProxy.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else if ((isInside) && (bList[proxyIndex] == 0)) {
                                bList[proxyIndex]++;
                                System.out.println("Boat " + proxyIndex + " Entered Dangerous Area" + " and its Position is " + tokenProxies.get(proxyIndex).getPosition());
                                switch (batchtype) {
                                    case STANDARD: //for now
                                        // ArrayList<PlanManager> planManagers = Engine.getInstance().getPlans(); 
//                                        PlanManager p = Engine.getInstance().getPlans().get(0);
//                                    //for(PlanManager p : planManagers){
//                                        // p.processGeneratedEvent(new AbortMissionReceived(p.missionId));
//                                        for (BoatProxy b : tokenProxies) {
//                                            b.abortMission(p.missionId);
//                                        }
//
//                                        BoatProxy foundedB = tokenProxies.get(proxyIndex);
//                                        BoatProxy foundedB2 = tokenProxies.get(proxyIndex);
//                                        BoatProxy foundedB3 = tokenProxies.get(proxyIndex);
//
//                                        ArrayList<Position> currTasksPositions = new ArrayList<Position>();
//                                        ArrayList<Position> currTasksPositions2 = new ArrayList<Position>();
//                                        ArrayList<Position> currTasksPositions3 = new ArrayList<Position>();
//
//                                        Set<BoatMarker> boatSet = decisions.keySet();
//                                        BoatMarker bMarker;
//                                        for (Iterator<BoatMarker> it = boatSet.iterator(); it.hasNext();) {
//                                            bMarker = it.next();
//                                            if (bMarker.getProxy() == foundedB) {
//                                                currTasksPositions.addAll(decisions.get(bMarker));
//                                                //   break;
//                                            } else {
//                                                if (currTasksPositions2.isEmpty()) {
//                                                    currTasksPositions2.addAll(decisions.get(bMarker));
//                                                    foundedB2 = bMarker.getProxy();
//                                                } else {
//                                                    currTasksPositions3.addAll(decisions.get(bMarker));
//                                                    foundedB3 = bMarker.getProxy();
//                                                }
//                                            }
//
//                                        }
//
//                                 //   Position safePos = currTasksPositions.get(0); 
//                                        Hashtable<ProxyInt, Path> proxyPath1 = new Hashtable<ProxyInt, Path>();
//
//                                 //   currTasksPositions.add(0,safePos);
//                                        ArrayList<Location> tasksLocations = Conversion.positionToLocation(currTasksPositions);
//                                        System.out.println("New task list: " + currTasksPositions);
//
//                                        proxyPath1.put(foundedB, new PathUtm(tasksLocations));
//                                        System.out.println("proxyPath " + proxyPath1);
//
//                                        ProxyExecutePath oEv1 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPath1);
//
//                                        try {
//                                            Thread.sleep(1000);
//                                        } catch (InterruptedException ex) {
//                                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                                        }
//
//                                        foundedB.handleEvent(oEv1);
//
//                                        try {
//                                            Thread.sleep(8000);
//                                        } catch (InterruptedException ex) {
//                                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                                        }
//
//                                    //*********************************** For making other boats move
//                                        Hashtable<ProxyInt, Path> proxyPath2 = new Hashtable<ProxyInt, Path>();
//
//                                        ArrayList<Location> tasksLocations2 = Conversion.positionToLocation(currTasksPositions2);
//                                        System.out.println("New task list: " + currTasksPositions2);
//
//                                        proxyPath2.put(foundedB2, new PathUtm(tasksLocations2));
//                                        System.out.println("proxyPath2 " + proxyPath2);
//
//                                        ProxyExecutePath oEv2 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPath2);
//
//                                        Hashtable<ProxyInt, Path> proxyPath3 = new Hashtable<ProxyInt, Path>();
//
//                                        ArrayList<Location> tasksLocations3 = Conversion.positionToLocation(currTasksPositions3);
//                                        System.out.println("New task list: " + currTasksPositions3);
//
//                                        proxyPath3.put(foundedB3, new PathUtm(tasksLocations3));
//                                        System.out.println("proxyPath3 " + proxyPath3);
//
//                                        ProxyExecutePath oEv3 = new ProxyExecutePath(oe.getId(), oe.getMissionId(), proxyPath3);
//
//                                        try {
//                                            Thread.sleep(1000);
//                                        } catch (InterruptedException ex) {
//                                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                                        }

//                                        foundedB2.handleEvent(oEv2);
//                                        foundedB3.handleEvent(oEv3);
                                    //***********************************
                                        // }
//                                    PlanManager p = Engine.getInstance().abort();
//                                    p.abortMission();
//                                    p.processGeneratedEvent(new AbortMissionReceived(p.missionId));
                                        flag = false;
                                        break;
                                    case INTERRUPT:

                                        BoatProxy foundB = tokenProxies.get(proxyIndex);
                                        foundB.setIntrSt(2);
////                                    Engine.getInstance().setVariableValue("newPoint", safeLoc);
//                                      Engine.getInstance().getPlans().get(0).enterInterruptPlace(InterruptType.PROXY, new ArrayList<ProxyInt>(Arrays.asList(foundB)));
                                        flag = false;

                                        break;
                                }
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BoatProxy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }).start();

            OperatorSelectsDangerousArea responseEvent = new OperatorSelectsDangerousArea(oe.getId(), oe.getMissionId(), tokenProxies, area);

            System.out.println("RESPONSE CREATED");

            for (GeneratedEventListenerInt listener : listeners) {
                //  System.out.println("Sending response to listener *&*%*%^*^%&*^&");
                LOGGER.log(Level.FINE, "\tSending response to listener: {0}", listener);
                listener.eventGenerated(responseEvent);
            }
        } else if (oe instanceof ProxyExploreArea) {
            // Get the lawnmower path for the whole area
            // How many meters the proxy should move north after each horizontal section of the lawnmower pattern
            double latDegInc = M_PER_LAT_D * 10;
            ArrayList<Position> positions = new ArrayList<Position>();
            Area2D area = ((ProxyExploreArea) oe).getArea();
            for (Location location : area.getPoints()) {
                positions.add(Conversion.locationToPosition(location));
            }
            Polygon polygon = new Polygon(positions);
            Object[] tuple = getLawnmowerPath(polygon, latDegInc);
            ArrayList<Position> lawnmowerPositions = (ArrayList<Position>) tuple[0];
            double totalLength = (Double) tuple[1];

            // Divy up the waypoints to the selected proxies
            // Explore rectangle using horizontally oriented lawnmower paths
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "ProxyExecutePath had no relevant proxies attached: " + oe);
            }

            Hashtable<ProxyInt, Path> thisProxyPath = new Hashtable<ProxyInt, Path>();
            ArrayList<ProxyInt> relevantProxies = new ArrayList<ProxyInt>();
            double lengthPerProxy = totalLength / numProxies, proxyLength, length;
            List<Location> lawnmowerLocations;
            int lawnmowerIndex = 0;
            for (int proxyIndex = 0; proxyIndex < numProxies; proxyIndex++) {
                lawnmowerLocations = new ArrayList<Location>();
                proxyLength = 0.0;
                // Must have at least two remaining positions to form a path segment
                Position p1 = null;

                if (lawnmowerIndex < lawnmowerPositions.size() - 1) {
                    // We still have lawnmower segments to assign
                    p1 = lawnmowerPositions.get(lawnmowerIndex);
                    lawnmowerIndex++;
                    boolean loop = lawnmowerIndex < lawnmowerPositions.size() && proxyLength < lengthPerProxy;
                    while (loop) {
                        Position p2 = lawnmowerPositions.get(lawnmowerIndex);
                        if (lawnmowerIndex % 2 == 0) {
                            // Horizontal segment
                            length = Math.abs((p1.longitude.degrees - p2.longitude.degrees) * M_PER_LON_D);
                        } else {
                            // Vertical shift
                            length = latDegInc;
                        }
                        if (proxyLength + length < lengthPerProxy) {
                            lawnmowerLocations.add(Conversion.positionToLocation(p2));
                            proxyLength += length;
                            p1 = p2;
                            lawnmowerIndex++;
                            loop = lawnmowerIndex < lawnmowerPositions.size() && proxyLength < lengthPerProxy;
                        } else {
                            loop = false;
                        }
                    }

                    if (lawnmowerLocations.size() > MAX_SEGMENTS_PER_PROXY) {
                        LOGGER.log(Level.WARNING, "Waypoint list size is " + lawnmowerLocations.size() + ": Breaking waypoints list into pieces to prevent communication failure");
                    }

                    List<Location> proxyLocations;
                    for (int i = 0; i < lawnmowerLocations.size() / MAX_SEGMENTS_PER_PROXY + 1; i++) {
//                        LOGGER.log(Level.FINE, "i = " + i + " of " + (lawnmowerLocations.size() / MAX_SEGMENTS_PER_PROXY + 1) + ": sublist " + i * MAX_SEGMENTS_PER_PROXY + ", " + Math.min(lawnmowerLocations.size(), (i + 1) * MAX_SEGMENTS_PER_PROXY));
                        proxyLocations = lawnmowerLocations.subList(i * MAX_SEGMENTS_PER_PROXY, Math.min(lawnmowerLocations.size(), (i + 1) * MAX_SEGMENTS_PER_PROXY));
                        // Send the path
//                        LOGGER.log(Level.FINE, "Creating ProxyExecutePath with " + proxyLocations.size() + " waypoints for proxy " + tokenProxies.get(proxyIndex));
                        PathUtm path = new PathUtm(proxyLocations);
//                        Hashtable<ProxyInt, Path> thisProxyPath = new Hashtable<ProxyInt, Path>();
                        thisProxyPath.put(tokenProxies.get(proxyIndex), path);
                        relevantProxies.add(tokenProxies.get(proxyIndex));
//                        ProxyExecutePath proxyEvent = new ProxyExecutePath(oe.getId(), oe.getMissionId(), thisProxyPath);
//                        tokenProxies.get(proxyIndex).handleEvent(proxyEvent);
                    }
                } else {
                    // We have finished assigning all the lawnmower path segments
                    // Send a blank path to the remaining proxies otherwise we won't get a ProxyPathComplete InputEvent                        
                    // Send the path
                    thisProxyPath.put(tokenProxies.get(proxyIndex), new PathUtm(new ArrayList<Location>()));
                    relevantProxies.add(tokenProxies.get(proxyIndex));
//                    ProxyExecutePath proxyEvent = new ProxyExecutePath(oe.getId(), oe.getMissionId(), thisProxyPath);
//                    tokenProxies.get(proxyIndex).handleEvent(proxyEvent);
                }

            }
            ProxyAreaSelected responseEvent = new ProxyAreaSelected(oe.getId(), oe.getMissionId(), thisProxyPath, relevantProxies);
            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: " + listener);
                listener.eventGenerated(responseEvent);
            }
        } else if (oe instanceof NearAssemblyLocationRequest) {
            NearAssemblyLocationRequest request = (NearAssemblyLocationRequest) oe;

            int assembleCounter = 0;
            int numProxies = 0;

            Hashtable<ProxyInt, Location> proxyPoints = new Hashtable<ProxyInt, Location>();
            ArrayList<ProxyInt> relevantProxies = new ArrayList<ProxyInt>();
            Location l = null;
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    l = request.getProxyPoints().get(token.getProxy());
                    break;
                }
            }

            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {

                    UTMCoordinate centerCoord = l.getCoordinate();
                    UTMCoordinate proxyCoord = new UTMCoordinate(centerCoord.getNorthing(), centerCoord.getEasting(), centerCoord.getZone());

                    int magnitude = (assembleCounter - 1) / 8 + 1;

                    proxyCoord.setNorthing(centerCoord.getNorthing() + magnitude * 50);
                    proxyCoord.setEasting(centerCoord.getEasting() + magnitude * 50);

                    Location rendezvousLocation = new Location(proxyCoord, l.getAltitude());

                    proxyPoints.put(token.getProxy(), rendezvousLocation);
                    relevantProxies.add(token.getProxy());
                    numProxies++;
                    assembleCounter++;

                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with LocationNearRendezvousRequest has no tokens with proxies attached: " + oe);
            } else if (numProxies > 1) {
                LOGGER.log(Level.WARNING, "Place with LocationNearRendezvousRequest has " + numProxies + " tokens with proxies attached: " + oe);
            }

//            System.out.println("AFTER LOGGING");
            NearAssemblyLocationResponse responseEvent = new NearAssemblyLocationResponse(oe.getId(), oe.getMissionId(), proxyPoints, relevantProxies);

//            System.out.println("RESPONSE CREATED");
            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: " + listener);
                listener.eventGenerated(responseEvent);
            }
//            System.out.println("EVENT GENERATED. END");

        } else if (oe instanceof AssembleLocationRequest) {

            AssembleLocationRequest request = (AssembleLocationRequest) oe;
            int assembleCounter = 0;
            if (eventIdToAssembleCounter.contains(request.getId())) {
                assembleCounter = eventIdToAssembleCounter.get(request.getId());
            }

            int numProxies = 0;
            Hashtable<ProxyInt, Location> proxyPoints = new Hashtable<ProxyInt, Location>();
            Hashtable<ProxyInt, Path> proxyPointsPath = new Hashtable<ProxyInt, Path>();
            ArrayList<ProxyInt> relevantProxies = new ArrayList<ProxyInt>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    Location assembleLocation = null;
                    if (assembleCounter == 0) {
                        assembleLocation = request.getLocation();
//                        Scanner sc;
//                        try {
//                            sc = new Scanner(new File("input/assemble.txt"));
//                            
//                            while (sc.hasNextLine()) {
//                                String line = sc.nextLine();
//                                String[] split = line.split("\\s");
//                                
//                                Position p = new Position(Angle.fromDegreesLatitude(Double.parseDouble(split[0])), Angle.fromDegreesLongitude(Double.parseDouble(split[1])), Double.parseDouble(split[2]));
//                                assembleLocation = Conversion.positionToLocation(p);
//                            }
//                            sc.close();
//                        } catch (FileNotFoundException ex) {
//                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        System.out.println("loc: " + request.getLocation());
//                        System.out.println("pos: " + Conversion.locationToPosition(request.getLocation()));

                    } else {
                        int direction = (assembleCounter - 1) % 8;
                        int magnitude = (assembleCounter - 1) / 8 + 1;
                        UTMCoordinate centerCoord = request.getLocation().getCoordinate();
                        UTMCoordinate proxyCoord = new UTMCoordinate(centerCoord.getNorthing(), centerCoord.getEasting(), centerCoord.getZone());
                        switch (direction) {
                            case 0:
                                //  0: N
                                proxyCoord.setNorthing(centerCoord.getNorthing() + magnitude * request.getSpacing());
                                break;
                            case 1:
                                //  1: NE
                                proxyCoord.setNorthing(centerCoord.getNorthing() + magnitude * request.getSpacing());
                                proxyCoord.setEasting(centerCoord.getEasting() + magnitude * request.getSpacing());
                                break;
                            case 2:
                                //  2: E
                                proxyCoord.setEasting(centerCoord.getEasting() + magnitude * request.getSpacing());
                                break;
                            case 3:
                                //  3: SE
                                proxyCoord.setNorthing(centerCoord.getNorthing() - magnitude * request.getSpacing());
                                proxyCoord.setEasting(centerCoord.getEasting() + magnitude * request.getSpacing());
                                break;
                            case 4:
                                //  4: S
                                proxyCoord.setNorthing(centerCoord.getNorthing() - magnitude * request.getSpacing());
                                break;
                            case 5:
                                //  5: SW
                                proxyCoord.setNorthing(centerCoord.getNorthing() - magnitude * request.getSpacing());
                                proxyCoord.setEasting(centerCoord.getEasting() - magnitude * request.getSpacing());
                                break;
                            case 6:
                                //  6: W
                                proxyCoord.setEasting(centerCoord.getEasting() - magnitude * request.getSpacing());
                                break;
                            case 7:
                                //  7: NW
                                proxyCoord.setNorthing(centerCoord.getNorthing() + magnitude * request.getSpacing());
                                proxyCoord.setEasting(centerCoord.getEasting() - magnitude * request.getSpacing());
                                break;
                        }
                        assembleLocation = new Location(proxyCoord, request.getLocation().getAltitude());
                    }
                    proxyPoints.put(token.getProxy(), assembleLocation);
                    Location myPos = ((BoatProxy) token.getProxy()).getLocation();
//                    proxyPointsPath.put(token.getProxy(), new PathUtm(new ArrayList<Location>(Arrays.asList(myPos,assembleLocation))));
                    proxyPointsPath.put(token.getProxy(), new PathUtm(new ArrayList<Location>(Arrays.asList(assembleLocation))));
                    relevantProxies.add(token.getProxy());
                    numProxies++;
                    assembleCounter++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyExecutePath has no tokens with proxies attached: " + oe);
            } else if (numProxies > 1) {
                LOGGER.log(Level.WARNING, "Place with ProxyExecutePath has " + numProxies + " tokens with proxies attached: " + oe);
            }

            eventIdToAssembleCounter.put(request.getId(), assembleCounter);

            AssembleLocationResponse responseEvent = new AssembleLocationResponse(oe.getId(), oe.getMissionId(), proxyPoints, relevantProxies);
            TasksAssignmentResponse responseDiff = new TasksAssignmentResponse(oe.getId(), oe.getMissionId(), proxyPointsPath, relevantProxies);

            System.out.println("TAASKASS: " + responseDiff);
            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.FINE, "\tSending response to listener: " + listener);
//                listener.eventGenerated(responseEvent);
                listener.eventGenerated(responseDiff);
            }
        } else if (oe instanceof TasksAssignmentRequest) {

            TasksAssignmentRequest request = (TasksAssignmentRequest) oe;
//            if(tokens.isEmpty()){
//                return;
//            }

            for (Location l : request.getTasks()) {
                System.out.println(Conversion.locationToPosition(l));
            }

            final ArrayList<ProxyInt> relevantProxies = new ArrayList<ProxyInt>();
            Hashtable<BoatMarker, BoatProxy> markerToProxy = new Hashtable<BoatMarker, BoatProxy>();

            for (final Token token : tokens) {
//                            System.out.println("TOKENS?: "+token);

                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
//                    System.out.println("PROXY?: " + token.getProxy());

                    BoatProxy bp = (BoatProxy) token.getProxy();
                    BoatMarker bm = new BoatMarker(bp, bp.getPosition(), new BasicMarkerAttributes(new Material(bp.getColor()), BasicMarkerShape.ORIENTED_SPHERE, 0.9));

                    markerToProxy.put(bm, bp);

                    relevantProxies.add(bp);

                }

            }
            Hashtable<ProxyInt, Path> proxyPaths = new Hashtable<ProxyInt, Path>();
            for (ProxyInt pi : relevantProxies) {
                proxyPaths.put(pi, new PathUtm(new ArrayList<Location>(Arrays.asList(Conversion.positionToLocation(((BoatProxy) pi).getPosition())))));
            }

//            if (request.getTasks() == null || request.getTasks().isEmpty()) {
//
//                TasksAssignmentResponse responseEvent = new TasksAssignmentResponse(oe.getId(), oe.getMissionId(), proxyPaths, relevantProxies);
//
//                for (GeneratedEventListenerInt listener : listeners) {
//                    LOGGER.log(Level.FINE, "\tSending response to listener: " + listener);
//                    listener.eventGenerated(responseEvent);
//                }
//                return;
//            }
            if (!decisions.isEmpty()) {
                ArrayList<Position> p = new ArrayList<Position>();

                for (BoatMarker b : decisions.keySet()) {
                    p.addAll(decisions.get(b));
                }

                synchronized (this){
                    ArrayList<Location> tmp = new ArrayList<Location>(request.getTasks());
            //    CopyOnWriteArrayList<Location> tmp = new CopyOnWriteArrayList<Location>(request.getTasks());

                    for (Location l : tmp) {

                        if (!p.contains(Conversion.locationToPosition(l))) {
               //             System.out.println("I removed this task: "+Conversion.locationToPosition(l));
                            request.getTasks().remove(l);
                        }

                    }
                }
            }
            System.out.println("Finally: ");
            for (Location l : request.getTasks()) {
                System.out.println(Conversion.locationToPosition(l));
            }
            
            //+++++ new for reassignment interaction counting +++++++
            
//            try {
//                    PrintWriter reassign = new PrintWriter(new BufferedWriter(new FileWriter("results/reassign" , true)));
//                    reassign.println(request.getTasks().size());
////                    inter.println();
//                    reassign.close();
//            } catch (IOException ex) {
//                Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
            //++++++++++++++++++++++++++++++++++++++++++++ by masoume
            
            int numOptions = 2;

            ArrayList<Position> selectedTasks = new ArrayList<Position>();

//            System.out.println("@@ TEST PRINT METHOD: " + request.getMethod());
//            System.out.println("@@ TEST PRINT TASKS: " + request.getTasks());
            for (Location l : request.getTasks()) {
                selectedTasks.add(Conversion.locationToPosition(l));
            }

            if (!markerToProxy.isEmpty()) {

                Coordinator coordinator = new Coordinator(markerToProxy);

//                coordinator.setMethod(request.getMethod());
                coordinator.setMethod(method);

                decisions = coordinator.taskAssignment(selectedTasks);
                try {
                    PrintWriter inter = new PrintWriter(new BufferedWriter(new FileWriter("results/interr" + coordinator, true)));
                    inter.println(selectedTasks.size() + 1);
//                    inter.println();
                    inter.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                final Coordinator c = coordinator;
                (new Thread() {
                    @Override
                    public void run() {
                        boolean guard;
                        do {
                            guard = false;
                            for (BoatMarker b : c.getMarkerToProxy().keySet()) {
                                ArrayList<Position> copy = new ArrayList<Position>(c.getDecisions().get(b));
                                for (Position p : copy) {
                                    Double d = Coordinator.computeDistance(b.getProxy().getPosition(), p);
                                    if (Math.abs(d) < 3) {
                                        c.getDecisions().get(b).remove(p);
                                        Engine.getInstance().getUiClient().toUiMessageReceived(new InformationMessage(oe.getId(), oe.getMissionId(), 1, "@METRICS " + b.getProxy() + " " + p));
                                        PrintWriter writer;
                                        try {
                                            writer = new PrintWriter(new BufferedWriter(new FileWriter("results/" + c, true)));
                                            writer.println(new InformationMessage(oe.getId(), oe.getMissionId(), 1, "@METRICS " + b.getProxy() + " " + p).toString());
                                            writer.close();
                                        } catch (IOException ex) {
                                            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        visited.put(b.getProxy(), c.getDecisions().get(b));
                                    }
                                }
                            }

                            for (BoatMarker b : c.getMarkerToProxy().keySet()) {
                                if (!c.getDecisions().get(b).isEmpty()) {
                                    guard = true;

                                }
                            }
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException ex) {
//                                Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
//                            }

                        } while (guard);

                    }
                }).start();

                for (BoatMarker b : markerToProxy.keySet()) {
//                    synchronized (decisions.get(b)) {
                    Path p = new PathUtm(Conversion.positionToLocation(decisions.get(b)));
                    ProxyInt pr = b.getProxy();
                    proxyPaths.put(pr, p);
//                    }
                }

                TasksAssignmentResponse responseEvent = new TasksAssignmentResponse(oe.getId(), oe.getMissionId(), proxyPaths, relevantProxies);

                for (GeneratedEventListenerInt listener : listeners) {
                    LOGGER.log(Level.FINE, "\tSending response to listener: " + listener);
                    listener.eventGenerated(responseEvent);
                }
            }

        } else if (oe instanceof ProxyCompareDistanceRequest) {
            ProxyCompareDistanceRequest request = (ProxyCompareDistanceRequest) oe;
            ArrayList<InputEvent> responses = new ArrayList<InputEvent>();

            int numProxies = 0;
            ArrayList<ProxyInt> relevantProxies;
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    BoatProxy boatProxy = (BoatProxy) token.getProxy();
                    if (!request.getProxyCompareLocation().containsKey(boatProxy)) {
                        LOGGER.severe("Passed in proxy token for " + boatProxy + " to place with ProxyCompareDistanceRequest, but there is no compare location entry for the proxy!");
                    } else {
                        Position stationKeepPosition = Conversion.locationToPosition(request.getProxyCompareLocation().get(boatProxy));
                        UTMCoord stationKeepUtm = UTMCoord.fromLatLon(stationKeepPosition.latitude, stationKeepPosition.longitude);
                        UtmPose stationKeepPose = new UtmPose(new Pose3D(stationKeepUtm.getEasting(), stationKeepUtm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(stationKeepUtm.getZone(), stationKeepUtm.getHemisphere().contains("North")));
                        Position boatPosition = boatProxy.getPosition();
                        UTMCoord boatUtm = UTMCoord.fromLatLon(boatPosition.latitude, boatPosition.longitude);
                        UtmPose boatPose = new UtmPose(new Pose3D(boatUtm.getEasting(), boatUtm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(boatUtm.getZone(), boatUtm.getHemisphere().contains("North")));
                        double distance = boatPose.pose.getEuclideanDistance(stationKeepPose.pose);

                        InputEvent response;
                        relevantProxies = new ArrayList<ProxyInt>();
                        relevantProxies.add(boatProxy);
                        if (distance > request.compareDistance) {
                            response = new QuantityGreater(request.getId(), request.getMissionId(), relevantProxies);
                        } else if (distance < request.compareDistance) {
                            response = new QuantityLess(request.getId(), request.getMissionId(), relevantProxies);
                        } else {
                            response = new QuantityEqual(request.getId(), request.getMissionId(), relevantProxies);
                        }
                        responses.add(response);
                    }
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyCompareDistanceRequest has no tokens with proxies attached: " + oe);
            }

            for (GeneratedEventListenerInt listener : listeners) {
                for (InputEvent response : responses) {
                    LOGGER.log(Level.FINE, "\tSending response: " + response + " to listener: " + listener);
                    listener.eventGenerated(response);
                }
            }
        } else if (oe instanceof NearestProxyToLocationRequest) {
            NearestProxyToLocationRequest request = (NearestProxyToLocationRequest) oe;
            ArrayList<InputEvent> responses = new ArrayList<InputEvent>();

            int numProxies = 0;

//            for (Token token : tokens) {
            LOGGER.log(Level.INFO, "---NEW STAGE: " + tokens.size());

            ArrayList<ProxyInt> relevantProxiesMove = new ArrayList<ProxyInt>();
            ArrayList<ProxyInt> relevantProxiesWait = new ArrayList<ProxyInt>();

            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    BoatProxy boatProxy = (BoatProxy) token.getProxy();

//                    if (relevantProxiesMove.size() > 0) {
                    relevantProxiesWait.add(boatProxy);
//                        if (!request.getProxyPoints().containsKey(boatProxy)) {
//                            LOGGER.severe("Passed in proxy token for " + boatProxy + " to place with ProxyCompareDistanceRequest, but there is no compare location entry for the proxy!");
//                        } else {
//                            final Position stationKeepPosition = Conversion.locationToPosition(request.getProxyPoints().get(boatProxy));
//                            System.out.println("TEST POS = " + stationKeepPosition.toString());
//                        }
//                    } else {
//                        relevantProxiesMove.add(boatProxy);
//                        numProxies++;
//                    }
                }
            }

            final Position stationKeepPosition = Conversion.locationToPosition(request.getProxyPoints().get(relevantProxiesWait.get(0)));

            BoatProxy min = (BoatProxy) Collections.min(relevantProxiesWait, new Comparator<ProxyInt>() {

                @Override
                public int compare(ProxyInt t, ProxyInt t1) {
                    double d1 = Coordinator.computeDistance(stationKeepPosition, ((BoatProxy) t).getPosition());
                    double d2 = Coordinator.computeDistance(stationKeepPosition, ((BoatProxy) t1).getPosition());
                    if (d1 < d2) {
                        return -1;
                    } else if (d2 > d1) {
                        return 1;
                    }
                    return 0;
                }
            });

            relevantProxiesWait.remove(min);
            relevantProxiesMove.add(min);

            NearestProxyToLocationResponse responseMove = new NearestProxyToLocationResponse(request.getId(), request.getMissionId(), relevantProxiesMove);
            WaitingProxiesResponse responseWait = new WaitingProxiesResponse(oe.getId(), oe.getMissionId(), relevantProxiesWait);

            responses.add(responseMove);
            responses.add(responseWait);

//            System.out.println("size move = " + responseMove.getRelevantProxyList().size());
//            System.out.println("size wait = " + responseWait.getRelevantProxyList().size());
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyCompareDistanceRequest has no tokens with proxies attached: " + oe);
            }

            LOGGER.info("Handling OnlyOneProxyRequest, have responses: " + responses + ", have listeners: " + listeners);
            for (GeneratedEventListenerInt listener : listeners) {
                for (InputEvent response : responses) {
                    LOGGER.log(Level.INFO, "\tSending response: " + response + " to listener: " + listener);
                    listener.eventGenerated(response);
                }
            }

        } else if (oe instanceof ProxyAbortMission) {
            for (Token token : tokens) {
                if (token.getProxy() != null) {
                    token.getProxy().abortMission(oe.getMissionId());
                }
            }
        } else if (oe instanceof ConnectExistingProxy) {
            // Connect to a non-simulated proxy
            ConnectExistingProxy connectEvent = (ConnectExistingProxy) oe;
            ProxyInt proxy = Engine.getInstance().getProxyServer().createProxy(connectEvent.name, connectEvent.color, CrwNetworkUtils.toInetSocketAddress(connectEvent.server));
            ImagePanel.setImagesDirectory(connectEvent.imageStorageDirectory);
            if (proxy != null) {
                ProxyCreated proxyCreated = new ProxyCreated(oe.getId(), oe.getMissionId(), proxy);
                for (GeneratedEventListenerInt listener : listeners) {
                    listener.eventGenerated(proxyCreated);
                }
            } else {
                LOGGER.severe("Failed to connect proxy");
            }
        } else if (oe instanceof CreateSimulatedProxy) {
            CreateSimulatedProxy createEvent = (CreateSimulatedProxy) oe;
            String name = createEvent.name;
            Color color = createEvent.color;
            boolean error = false;
            ArrayList<ProxyInt> relevantProxyList = new ArrayList<ProxyInt>();
            ArrayList<String> proxyNames = new ArrayList<String>();
            ArrayList<ProxyInt> proxyList = Engine.getInstance().getProxyServer().getProxyListClone();
            for (ProxyInt proxy : proxyList) {
                proxyNames.add(proxy.getProxyName());
            }
            for (int i = 0; i < createEvent.numberToCreate; i++) {
                // Create a simulated boat and run a ROS server around it
                VehicleServer server = new FastSimpleBoatSimulator();
                UdpVehicleService rosServer = new UdpVehicleService(11411 + portCounter, server);
                UTMCoordinate utmc = createEvent.startLocation.getCoordinate();
                UtmPose p1 = new UtmPose(new Pose3D(utmc.getEasting(), utmc.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utmc.getZoneNumber(), utmc.getHemisphere().equals(Hemisphere.NORTH)));
                server.setPose(p1);
                name = CrwHelper.getUniqueName(name, proxyNames);
                proxyNames.add(name);
                ProxyInt proxy = Engine.getInstance().getProxyServer().createProxy(name, color, new InetSocketAddress("localhost", 11411 + portCounter));
                color = randomColor();
                portCounter++;

                if (proxy != null) {
                    relevantProxyList.add(proxy);
                } else {
                    LOGGER.severe("Failed to create simulated proxy");
                    error = true;
                }
            }
            if (!error) {
                ProxyCreated proxyCreated = new ProxyCreated(oe.getId(), oe.getMissionId(), relevantProxyList);
                for (GeneratedEventListenerInt listener : listeners) {
                    listener.eventGenerated(proxyCreated);
                }
            }
        } else if (oe instanceof SetVelocityMultiplier) {
            final SetVelocityMultiplier setVelocityMultiplier = (SetVelocityMultiplier) oe;
            ArrayList<InputEvent> responses = new ArrayList<InputEvent>();

            int numBoatProxies = 0;
            // @todo Grouped or individual GainsSent result?
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    numBoatProxies++;

                    BoatProxy boatProxy = (BoatProxy) token.getProxy();
                    boatProxy.getVehicleServer().setVelocityMultiplier(setVelocityMultiplier.velocityMultiplier, new FunctionObserver<Void>() {
                        public void completed(Void v) {
                            LOGGER.fine("Set velocity multipliersucceeded: Multiplier [" + setVelocityMultiplier.velocityMultiplier + "]");
                        }

                        public void failed(FunctionObserver.FunctionError fe) {
                            LOGGER.severe("Set thrust gains failed: Multiplier [" + setVelocityMultiplier.velocityMultiplier + "]");
                        }
                    });

                    //@todo add in recognition of async success or failure
                    // SetGainsSucceeded
                    // SetGainsFailed
                }
            }
            if (numBoatProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with SetVelocityMultiplier has no tokens with boat proxies attached: " + oe + ", tokens [" + tokens + "]");
            }

            ArrayList<GeneratedEventListenerInt> listenersCopy = (ArrayList<GeneratedEventListenerInt>) listeners.clone();
            for (GeneratedEventListenerInt listener : listenersCopy) {
                for (InputEvent response : responses) {
                    LOGGER.log(Level.FINE, "\tSending response: " + response + " to listener: " + listener);
                    listener.eventGenerated(response);
                }
            }
        } else if (oe instanceof ProxyPausePath) {
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            Hashtable<ProxyInt, Path> proxyPaths = new Hashtable<ProxyInt, Path>();

            ArrayList<ProxyInt> rel = new ArrayList<ProxyInt>();

            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    rel.add(token.getProxy());
                    numProxies++;
                }
            }

            for (ProxyInt pi : rel) {
                ProxyExecutePath pep = null;
                if (pi.getCurrentEvent() instanceof ProxyExecutePath) {
                    System.out.println("EVENTS; " + pi.getEvents());
                    if (pi.getEvents().size() == 1) {
                        pep = (ProxyExecutePath) pi.getCurrentEvent();

                    } else {
                        pep = (ProxyExecutePath) pi.getEvents().get(1);

                    }
                }
//                pep = (ProxyExecutePath) pi.getCurrentEvent();
                BoatProxy b = (BoatProxy) pi;

                pi.handleEvent(new ProxyEmergencyAbort(oe.getId(), oe.getMissionId()));

                Hashtable<ProxyInt, Path> pp = pep.getProxyPaths();
                
                PathUtm path = (PathUtm) pp.get(pi);
                
                HashMap<Location, Double> distances = new HashMap<Location, Double>();

                if (path != null) {
                    PathUtm tmp = new PathUtm(path.getPoints());
                    if (visited.contains(b)) {
                        tmp.getPoints().removeAll(visited.get(b));
                        System.out.println("Umad inja");
                    }

                    for (Location p : tmp.getPoints()) {
                        distances.put(p, Coordinator.computeDistance(b.getPosition(), Conversion.locationToPosition(p)));
                    }
                    
                    
//                //}
//                
//                if (path == null)
//                {
//                    System.out.println("something is wrong: path is empty");
//                }
//                
                Map.Entry<Location, Double> min = Collections.min(distances.entrySet(), new Comparator<Map.Entry<Location, Double>>() {

                    public int compare(Map.Entry<Location, Double> t, Map.Entry<Location, Double> t1) {
                        return t.getValue().compareTo(t1.getValue());
                    }
                });
                
                int index = path.getPoints().indexOf(min.getKey());
                proxyPaths.put(b, new PathUtm(path.getPoints().subList(index, path.getPoints().size())));
                }
                File[] listOfFiles = (new File("results")).listFiles();

                try {
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.getName().contains("interr")) {
                            Scanner sc = new Scanner(file);
                            int numInt = Integer.parseInt(sc.nextLine());

                            sc.close();

                            PrintWriter inter = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                            inter.println(numInt + 1);

                            inter.close();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
//            --------------------------------------
//            for (Token token : tokens) {
//                if (token.getProxy().getCurrentEvent() instanceof ProxyExecutePath) {
//
//                    ProxyExecutePath pep = (ProxyExecutePath) token.getProxy().getCurrentEvent();
//
//                    BoatProxy b = (BoatProxy) token.getProxy();
//                    boolean guard = true;
//
////                    Hashtable<ProxyInt, Path> proxyPaths = pep.getProxyPaths();
////                    for (PlanManager p : Engine.getInstance().getPlans()) {
////                        if (p.getPlanName().equals("Assemble")) {
//////else {
////
////                            proxyPaths.put(b, pep.getProxyPaths().get(b));
////                            pp.put(b, pep.getProxyPaths().get(b));
////
////                            guard = false;
//////                    }     
////                        }
////                    }
////                    pep.getMissionId().compareTo(null)
//                    PathUtm path = (PathUtm) pep.getProxyPaths().get(token.getProxy());
//
//                    HashMap<Location, Double> distances = new HashMap<Location, Double>();
//
//                    System.out.println("PATHS FROM EVENT: " + path.getPoints());
//                    for (Location p : path.getPoints()) {
//                        distances.put(p, Coordinator.computeDistance(b.getPosition(), Conversion.locationToPosition(p)));
//                    }
//
//                    boolean minFound = true;
//                    int index = 0;
//                    System.out.println(distances);
//
//                    while (minFound && guard) {
//                        Map.Entry<Location, Double> min = Collections.min(distances.entrySet(), new Comparator<Map.Entry<Location, Double>>() {
//
//                            public int compare(Map.Entry<Location, Double> t, Map.Entry<Location, Double> t1) {
//                                return t.getValue().compareTo(t1.getValue());
//                            }
//                        });
//
//                        System.out.println("MIN: " + min);
//                        distances.remove(min.getKey());
//
////                    Map.Entry<Location, Double> min2 = Collections.min(distances.entrySet(), new Comparator<Map.Entry<Location, Double>>() {
////
////                        public int compare(Map.Entry<Location, Double> t, Map.Entry<Location, Double> t1) {
////                            return t.getValue().compareTo(t1.getValue());
////                        }
////                    });
////                        System.out.println(min.getKey());
////                        Position tmp = 
////                        if(distances.size() == 1){
////                            minFound = false;
////                            index = path.getPoints().indexOf(min.getKey());
////                        }
//                        if (visited.containsKey(b)) {
//                            System.out.println("DENTRO VISISTED");
//                            if (!visited.get(b).contains(Conversion.locationToPosition(min.getKey()))) {
//                                System.out.println("DENTRO CONTAINS");
//                                minFound = false;
//                                index = path.getPoints().indexOf(min.getKey());
//                            }
//                        } else {
//                            minFound = false;
//                            index = path.getPoints().indexOf(min.getKey());
//
////                        }
//                        }
//                        proxyPaths.put(b, new PathUtm(path.getPoints().subList(index, path.getPoints().size())));
//                        pp.put(b, new PathUtm(path.getPoints().subList(index, path.getPoints().size())));
//
//                    }
////                    if (path.getPoints().indexOf(min1.getKey()) > path.getPoints().indexOf(min2.getValue())) {
////                        index = path.getPoints().indexOf(min1.getKey());
////                    } else {
////                        index = path.getPoints().indexOf(min2.getKey());
////                    }
////                        if()
//
//                    System.out.println("INDEX= " + index + "; DIMENSION= " + distances.size() + 1);
////                    }
////                    Hashtable<ProxyInt, Path> newpath = new Hashtable<ProxyInt, Path>();
////                    newpath.put(b, new PathUtm(path.getPoints().subList(index, path.getPoints().size())));
//
////                    proxyPaths.remove(b);
////                    proxyPaths.clear();
//                }
//
//            }

            System.out.println("TEST PRIMA TASKASS");
            TasksAssignmentResponse responseEvent = new TasksAssignmentResponse(oe.getId(), oe.getMissionId(), proxyPaths, rel);

//                    ProxyExecutePath pep2 = new ProxyExecutePath(pep.getId(), pep.getMissionId(), newpath);
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyPausePath has no tokens with proxies attached: " + oe);
            }
            System.out.println("TEST PRIMA HANDLING");

            LOGGER.info("Handling ProxyPausePath, have responses: " + responseEvent + ", have listeners: " + listeners);
            for (GeneratedEventListenerInt listener : listeners) {
                LOGGER.log(Level.INFO, "\tSending response to listener: " + listener);
                listener.eventGenerated(responseEvent);
                System.out.println("RESPONSE: " + responseEvent);

            }

        } else if (oe instanceof ProxyEmergencyAbort) {
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyEmergencyAbort has no tokens with proxies attached: " + oe);
            }
            for (BoatProxy boatProxy : tokenProxies) {
                boatProxy.handleEvent(oe);
            }
        } else if (oe instanceof ProxyResendWaypoints) {
            int numProxies = 0;
            ArrayList<BoatProxy> tokenProxies = new ArrayList<BoatProxy>();
            for (Token token : tokens) {
                if (token.getProxy() != null && token.getProxy() instanceof BoatProxy) {
                    tokenProxies.add((BoatProxy) token.getProxy());
                    numProxies++;
                }
            }
            if (numProxies == 0) {
                LOGGER.log(Level.WARNING, "Place with ProxyResendWaypoints has no tokens with proxies attached: " + oe);
            }
            for (BoatProxy boatProxy : tokenProxies) {
                boatProxy.handleEvent(oe);
            }
        }
    }

    @Override
    public boolean offer(GeneratedInputEventSubscription sub
    ) {
        LOGGER.log(Level.FINE, "ProxyEventHandler offered subscription: " + sub);

        if (sub.getSubscriptionClass() == ProxyPathCompleted.class
                || sub.getSubscriptionClass() == ProxyPathFailed.class
                || sub.getSubscriptionClass() == ProxyCreated.class
                || sub.getSubscriptionClass() == NearestProxyToLocationResponse.class
                || sub.getSubscriptionClass() == AssembleLocationResponse.class
                || sub.getSubscriptionClass() == OperatorSelectsDangerousArea.class
                || sub.getSubscriptionClass() == OperatorSelectsCheckPointsArea.class
                || sub.getSubscriptionClass() == DecisionMakingStay.class//new
                || sub.getSubscriptionClass() == DecisionMakingLeave.class//new
                || sub.getSubscriptionClass() == QueueManagementDone.class//new
                || sub.getSubscriptionClass() == QueueLearningDone.class//new
                || sub.getSubscriptionClass() == OperatorAvailable.class
                || sub.getSubscriptionClass() == OperatorUnavailable.class
                || sub.getSubscriptionClass() == BoatOutsideArea.class//new
                || sub.getSubscriptionClass() == SetVelocityMultiplierSucceeded.class//new
                || sub.getSubscriptionClass() == NearAssemblyLocationResponse.class
                || sub.getSubscriptionClass() == TasksAssignmentResponse.class
                || sub.getSubscriptionClass() == QuantityGreater.class
                || sub.getSubscriptionClass() == QuantityLess.class
                || sub.getSubscriptionClass() == QuantityEqual.class
                || sub.getSubscriptionClass() == ProxyAreaSelected.class
                || sub.getSubscriptionClass() == ProxyPoseUpdated.class) {
            LOGGER.log(Level.FINE,
                    "\tProxyEventHandler taking subscription: " + sub);
            if (!listeners.contains(sub.getListener())) {
                LOGGER.log(Level.FINE, "\t\tProxyEventHandler adding listener: " + sub.getListener());
                listeners.add(sub.getListener());
                listenerGCCount.put(sub.getListener(), 1);
            } else {
                LOGGER.log(Level.FINE, "\t\tProxyEventHandler incrementing listener: " + sub.getListener());
                listenerGCCount.put(sub.getListener(), listenerGCCount.get(sub.getListener()) + 1);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean cancel(GeneratedInputEventSubscription sub
    ) {
        LOGGER.log(Level.FINE, "ProxyEventHandler asked to cancel subscription: " + sub);
        if ((sub.getSubscriptionClass() == ProxyPathCompleted.class
                || sub.getSubscriptionClass() == ProxyPathFailed.class
                || sub.getSubscriptionClass() == ProxyCreated.class
                || sub.getSubscriptionClass() == NearestProxyToLocationResponse.class
                || sub.getSubscriptionClass() == AssembleLocationResponse.class
                || sub.getSubscriptionClass() == BoatOutsideArea.class//new
                || sub.getSubscriptionClass() == SetVelocityMultiplierSucceeded.class//new
                || sub.getSubscriptionClass() == QueueManagementDone.class//new
                || sub.getSubscriptionClass() == QueueLearningDone.class//new
                || sub.getSubscriptionClass() == NearAssemblyLocationResponse.class
                || sub.getSubscriptionClass() == OperatorSelectsDangerousArea.class
                || sub.getSubscriptionClass() == OperatorSelectsCheckPointsArea.class
                || sub.getSubscriptionClass() == DecisionMakingStay.class
                || sub.getSubscriptionClass() == DecisionMakingLeave.class
                || sub.getSubscriptionClass() == OperatorAvailable.class
                || sub.getSubscriptionClass() == OperatorUnavailable.class
                || sub.getSubscriptionClass() == TasksAssignmentResponse.class
                || sub.getSubscriptionClass() == QuantityGreater.class
                || sub.getSubscriptionClass() == QuantityLess.class
                || sub.getSubscriptionClass() == ProxyAreaSelected.class
                || sub.getSubscriptionClass() == QuantityEqual.class
                || sub.getSubscriptionClass() == ProxyPoseUpdated.class)
                && listeners.contains(sub.getListener())) {
            LOGGER.log(Level.FINE, "\tProxyEventHandler canceling subscription: " + sub);
            if (listenerGCCount.get(sub.getListener()) == 1) {
                // Remove listener
                LOGGER.log(Level.FINE, "\t\tProxyEventHandler removing listener: " + sub.getListener());
                listeners.remove(sub.getListener());
                listenerGCCount.remove(sub.getListener());
            } else {
                // Decrement garbage colleciton count
                LOGGER.log(Level.FINE, "\t\tProxyEventHandler decrementing listener: " + sub.getListener());
                listenerGCCount.put(sub.getListener(), listenerGCCount.get(sub.getListener()) - 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void eventOccurred(InputEvent proxyEventGenerated) {
        LOGGER.log(Level.FINE, "Event occurred: " + proxyEventGenerated + ", rp: " + proxyEventGenerated.getRelevantProxyList() + ", listeners: " + listeners);
        for (GeneratedEventListenerInt listener : listeners) {
            listener.eventGenerated(proxyEventGenerated);
        }
    }

    @Override
    public void poseUpdated() {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void waypointsUpdated() {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void waypointsComplete() {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void proxyAdded(ProxyInt p
    ) {
        p.addListener(this);
    }

    @Override
    public void proxyRemoved(ProxyInt p
    ) {
    }

    private Color randomColor() {
        float r = RANDOM.nextFloat();
        float g = RANDOM.nextFloat();
        float b = RANDOM.nextFloat();

        return new Color(r, g, b);
    }

    private Object[] getLawnmowerPath(Polygon area, double stepSize) {
        // Compute the bounding box
        Angle minLat = Angle.POS360;
        Angle maxLat = Angle.NEG360;
        Angle minLon = Angle.POS360;
        Angle maxLon = Angle.NEG360;
        Angle curLat = null;
        for (LatLon latLon : area.getOuterBoundary()) {
            if (latLon.latitude.degrees > maxLat.degrees) {
                maxLat = latLon.latitude;
            } else if (latLon.latitude.degrees < minLat.degrees) {
                minLat = latLon.latitude;
            }
            if (latLon.longitude.degrees > maxLon.degrees) {
                maxLon = latLon.longitude;
            } else if (latLon.longitude.degrees < minLon.degrees) {
                minLon = latLon.longitude;
            }
        }
        curLat = minLat;

        double totalLength = 0.0;
        Angle leftLon = null, rightLon = null;
        ArrayList<Position> path = new ArrayList<Position>();
        while (curLat.degrees <= maxLat.degrees) {
            // Left to right
            leftLon = getMinLonAt(area, minLon, maxLon, curLat);
            rightLon = getMaxLonAt(area, minLon, maxLon, curLat);
            if (leftLon != null && rightLon != null) {
                path.add(new Position(new LatLon(curLat, leftLon), 0.0));
                path.add(new Position(new LatLon(curLat, rightLon), 0.0));
                totalLength += Math.abs((rightLon.degrees - leftLon.degrees) * M_PER_LON_D);
            } else {
            }
            // Right to left
            curLat = curLat.addDegrees(stepSize);
            if (curLat.degrees <= maxLat.degrees) {
                totalLength += stepSize;
                rightLon = getMaxLonAt(area, minLon, maxLon, curLat);
                leftLon = getMinLonAt(area, minLon, maxLon, curLat);
                if (leftLon != null && rightLon != null) {
                    path.add(new Position(new LatLon(curLat, rightLon), 0.0));
                    path.add(new Position(new LatLon(curLat, leftLon), 0.0));
                    totalLength += Math.abs((rightLon.degrees - leftLon.degrees) * M_PER_LON_D);
                } else {
                }
            }
            curLat = curLat.addDegrees(stepSize);
            if (curLat.degrees <= maxLat.degrees) {
                totalLength += stepSize;
            }
        }

        return new Object[]{path, totalLength};
    }

    private static Angle getMinLonAt(Polygon area, Angle minLon, Angle maxLon, Angle lat) {
        final double lonDiff = 1.0 / 90000.0 * 10.0;
        LatLon latLon = new LatLon(lat, minLon);
        while (!isLocationInside(latLon, (ArrayList<LatLon>) area.getOuterBoundary()) && minLon.degrees <= maxLon.degrees) {
            minLon = minLon.addDegrees(lonDiff);
            latLon = new LatLon(lat, minLon);
            if (minLon.degrees > maxLon.degrees) {
                // Overshot (this part of the area is tiny), so ignore it by returning null
                return null;
            }
        }
        return minLon;
    }

    private static Angle getMaxLonAt(Polygon area, Angle minLon, Angle maxLon, Angle lat) {
        final double lonDiff = 1.0 / 90000.0 * 10.0;
        LatLon latLon = new LatLon(lat, maxLon);
        while (!isLocationInside(latLon, (ArrayList<LatLon>) area.getOuterBoundary())) {
            maxLon = maxLon.addDegrees(-lonDiff);
            latLon = new LatLon(lat, maxLon);
            if (maxLon.degrees < minLon.degrees) {
                // Overshot (this part of the area is tiny), so ignore it by returning null
                return null;
            }
        }
        return maxLon;
    }

    /**
     * From: http://forum.worldwindcentral.com/showthread.php?t=20739
     *
     * @param point
     * @param positions
     * @return
     */
    public static boolean isLocationInside(LatLon point, ArrayList<? extends LatLon> positions) {
        boolean result = false;
        LatLon p1 = positions.get(0);
        for (int i = 1; i < positions.size(); i++) {
            LatLon p2 = positions.get(i);

            if (((p2.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p1.getLatitude().degrees)
                    || (p1.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p2.getLatitude().degrees))
                    && (point.getLongitude().degrees < (p1.getLongitude().degrees - p2.getLongitude().degrees)
                    * (point.getLatitude().degrees - p2.getLatitude().degrees)
                    / (p1.getLatitude().degrees - p2.getLatitude().degrees) + p2.getLongitude().degrees)) {
                result = !result;
            }
            p1 = p2;
        }
        return result;
    }

    //@masoume
    public boolean isPointInside(Area2D area, Position p) {

        ArrayList<Position> positions = new ArrayList<Position>();

        for (Location location : area.getPoints()) {
            positions.add(Conversion.locationToPosition(location));
        }

        double minLat = positions.get(1).getLatitude().getDegrees();
        double maxLat = positions.get(1).getLatitude().getDegrees();
        double minLon = positions.get(1).getLongitude().getDegrees();
        double maxLon = positions.get(1).getLongitude().getDegrees();

        for (Position pos : positions) {
            if (pos.getLatitude().getDegrees() < minLat) {
                minLat = pos.getLatitude().getDegrees();
            }
            if (pos.getLatitude().getDegrees() > maxLat) {
                maxLat = pos.getLatitude().getDegrees();
            }
            if (pos.getLongitude().getDegrees() < minLon) {
                minLon = pos.getLongitude().getDegrees();
            }
            if (pos.getLongitude().getDegrees() > maxLon) {
                maxLon = pos.getLongitude().getDegrees();
            }
        }

        return p.getLatitude().getDegrees() >= minLat
                && p.getLatitude().getDegrees() <= maxLat
                && p.getLongitude().getDegrees() >= minLon
                && p.getLongitude().getDegrees() <= maxLon;
    }

    //@masoume
    public void saveTotalTeamAccRew(double totalRew){
    
        FileWriter fw = null;
        try {
            //boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/total-team-acc-rew-fullState.txt");
            if(!file.exists()){
                file.createNewFile();
             //   bExist = true;
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(totalRew);
//            if (bExist){pw.println(totalRew);bExist=false;}
//            else {pw.println(totalRew);}
 
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //@masoume
    public void saveTotalTeamRew(double totalRew){
    
        FileWriter fw = null;
        try {
            //boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/total-team-rew-fullState.txt");
            if(!file.exists()){
                file.createNewFile();
             //   bExist = true;
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(totalRew);
//            if (bExist){pw.println(totalRew);bExist=false;}
//            else {pw.println(totalRew);}
 
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //@masoume
    public void saveDataSet(double arrival,int reqType,double servTime) {
       
        FileWriter fw = null;
        try {
            //  File file = new File("C://myfile.txt");
            boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/Results-QLearning/Experiment/data5g.txt");
            if(!file.exists()){
                file.createNewFile();
                bExist = true;
            }   
            else {bExist = true;}
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
 
            if (bExist){
                pw.write(String.valueOf(arrival)+" ");
                pw.write(String.valueOf(reqType)+" ");
                pw.write(String.valueOf(servTime)+" ");
                pw.println();  
            }
            
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public int countFile(){
        int i=1;
        
        FileWriter fw = null;
        FileReader fr = null;
        try {
            //  File file = new File("C://myfile.txt");
            boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/Results-QLearning/Experiment/count.txt");
            if(!file.exists()){
                file.createNewFile();               
                bExist = true;
            }   
            else {
                Scanner sc = new Scanner(file);
                i=sc.nextInt();
                sc.close();              
                bExist = true;
  
            }
            fw = new FileWriter(file,false);
            
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
 
            if (bExist){
                if (i==20)
                    pw.print(1);
                else 
                    pw.print(i+1);
            }          
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return i;
    }
    
    //@masoume
    public double[][] loadDataSet(int i) {
        try {
            double[][] arrData = new double[30][3];
            Scanner scan = new Scanner(new File("/Users/Masoume/Desktop/Results-QLearning/Experiment/data"+i+".txt"));
            for (int k=0;k<30;k++){
                for (int j=0;j<3;j++){
                    arrData[k][j] = Double.parseDouble(scan.next());
                }     
            }
            scan.close();
            return arrData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    
    //@masoume
    public void getLocation(double x0, double y0, int radius) {

        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLongitude = new_x + x0;
        double foundLatitude = y + y0;

        System.out.println("Longitude: " + foundLongitude + "  Latitude: " + foundLatitude);

    }

    //@masoume
    public void resultsFile(String strFileName,String strData) {
        FileWriter fw = null;
        boolean b = false;
      //  if (strFileName.contains("failStatistics")) b = true;
        try {
            //  File file = new File("C://myfile.txt");
            boolean bExist = false;
            File file = new File(strFileName);
            if(!file.exists()){
                file.createNewFile();
                bExist = true;
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
 
            if (bExist){pw.println(strData);pw.println("====================================");bExist=false;}
            else {pw.println(strData);}
 
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(ProxyEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    //@masoume
//    public double pointToLineDistance(Point A, Point B, Point P) {
//    double normalLength = Math.sqrt((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
//    return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
//  }
}
