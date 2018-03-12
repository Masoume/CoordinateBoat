/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.proxy;

import java.util.Random;

/**
 *
 * @author Masoume
 */
public class BoatWorld {
    
    public enum Action { Join, Balk} //join=0;balk(or move-on)=1
    int[] actions = {0,1};//join=0;balk=1;
    int[] state; //
  //  boolean localOnly; //only local state if true, o.w global state too
    int stateType;
    double avgWaitingTime = 0.00027;
    double avgArrivingTime = 0.00025;
    boolean random = false;
    int boatId;
    //BoatState boatState;
    BoatPolicy policy;

    // dimension of each state feature: { reqType,#tasks,maxQsize, Actions }
    final int[] dimSize = { 6, 7, 6, 2 }; // bayad bar hasbe localOnly taghir kond; farda
    
    // for only local state: { reqType,#tasks, Actions }
  //  final int[] dimSize = { 6, 7, 2 }; // bayad bar hasbe localOnly taghir kond; farda
    
    //for full state ; size of the queue does not need to be presented in the state { reqType,#tasks,Actions }
  //  final int[] dimSize = { 6,3,6,3,6,3,6,3,6,3,2 };
    
    // Learning types
    public static final int Q_LEARNING = 1;
    public static final int SARSA = 2;
    public static final int Q_LAMBDA = 3; // Good parms were lambda=0.05, gamma=0.1, alpha=0.01, epsilon=0.1

    // Action selection types
    public static final int E_GREEDY = 1;
    public static final int SOFTMAX = 2;
    
    // Reward //setting 4
    public static final double R_Task = 0.3;//0.4;
    public static final double R_Fail = -2;//-2;
    public static final double R_Service = 1; 
    public static final double R_Wrong_Balk = -0.5;//-2; 

    int learningMethod;
    int actionSelection;

    double epsilon;
    double temp;
    double alpha;
    double gamma;
    double lambda;

    
    public double getAlpha(){
        return this.alpha;
    }
    public double getGamma(){
        return this.gamma;
    }
    public double getepsilon(){
        return epsilon;
    }
    public int[] getDimension() {
	
	return dimSize;
    }
    public double getRFail(){
        return R_Fail;
    }
    public double getRTask(){
        return R_Task;
    }
    public boolean validAction(int action,int[] state,int index){ // new version only used for full state
        System.out.println(">>ValidAction action: "+action+" for boat "+index);
        int tmp = state[index*2];// = state[]
        System.out.println(">>ValidAction action: "+action+" for boat "+index+ " in state "+ tmp);
        switch(action){
            case 0://join
                if (tmp == 1 || tmp == 2 || tmp == 3)//only if there is a request, join makes sense
                    return true;
                else return false; //normal, wait, fail
            case 1://balk or move-on
                //if (tmp == 4 || tmp == 5 || tmp == 0) //normal, wait, fail
                if (tmp == 5) //fail
                    return false;
                else return true;  
            default:
                return false;
        }      
    }
    public boolean validAction(int action, int[] state ){
        System.out.println(">>ValidAction action: "+action+" in state[0]="+state[0]);
        switch(action){
            case 0: //join
                if ((state[0]==1)||(state[0]==2)||(state[0]==3))//only if there is a request, join makes sense
                    return true;
                else return false; //normal, wait, fail
            case 1:
                if (state[0]==5)//fail
                    return false;
                else return true; //only if there is a request, balk makes sense
    
//            case 1://balk or move on
//                if (!localOnly){
//                    if ((state[0]==4)||(state[0]==5)||(state[2]==0))//wait,fail or q is empty 
//                        return false;//balk is not an option
//                    else return true;//can balk
//                }
//                else if (localOnly){
//                    if ((state[0]==4)||(state[0]==5))//wait,fail
//                        return false;
//                    else return true;
//                }
            default:
                return false;
        }
    }
    public int[] getNextState( int action, int[]state,int index){// new version only used for full state
        int[] retState = new int[state.length];
        for(int i=0;i<state.length;i++)
            retState[i] = state[i];
        
        int relatedBoatIndex = index*2;//
        System.out.println("relatedBoatIndex: "+relatedBoatIndex);
        
        if(validAction(action,state,index)){
            if (action==0){//join
                retState[relatedBoatIndex] = 4;//wait
            }  
            else if (action==1){//balk
                if ((state[relatedBoatIndex]!= 0)&&(state[relatedBoatIndex]!= 4)){ //if not waiting or normal
                    Random rand = new Random(); 
                    int ran = rand.nextInt(10)+1; //generate random num for prob of failure
                    if (((state[relatedBoatIndex]==1) && (ran<=9))||((state[relatedBoatIndex]==2) && (ran<=4)) || ((state[relatedBoatIndex]==3) && (ran<=2)))
                            retState[relatedBoatIndex] = 5;//fail
                    else 
                        retState[relatedBoatIndex] = 0;//normal
                }
            }
        }    
        return retState;
    } 
    public int[] getNextState( int action, int[]state ){
        
        int[] retState = new int[state.length];
        for(int i=0;i<state.length;i++)
            retState[i] = state[i];
       // retState[0] = 0; //normal

        if(validAction(action,state)){
            if (action==0){//join
                retState[0]=4;//wait
                //if (!localOnly)//locGlob
                if (stateType==1)//locGlob
                    retState[2]=state[2]+1;//qsize
            }  
            else if (action==1){//balk or move-on
                if ((state[0]!= 0) && (state[0]!= 4) ){ //if not waiting or normal
                    Random rand = new Random(); 
                    int ran = rand.nextInt(10)+1; //generate random num for prob of failure
                    if (((state[0]==1) && (ran<=9))||((state[0]==2) && (ran<=4)) || ((state[0]==3) && (ran<=2)))//battery recharge
                            retState[0] = 5;//fail
                    else 
                        retState[0] = 0;//normal
                }
            }
            //when action==2 (no action), the next state will be the same as the current state
        }       
        return retState;
    }
    public double getNextReward(int action,int[] state, int index,int[] nextState,double servTime,int qSize){//immidiate reward given by the system//full state
        double rew = 0.0;       
        int relatedBoatIndex = index*2;// 
        if (nextState[relatedBoatIndex]== 5){//fail//the corresponding boat
            rew = (R_Fail*(this.avgWaitingTime/this.avgArrivingTime))+ qSize;
            System.out.println("reward: "+rew);
            return rew;
        }
        if ((nextState[relatedBoatIndex]==0)&&((state[relatedBoatIndex]==1)||(state[relatedBoatIndex]==2)||(state[relatedBoatIndex]==3))){
           //successful balk
            rew = R_Task+((state[relatedBoatIndex+1]*1.0)/100);//setting 4: #tasks/100
            System.out.println("reward: "+rew);
            return rew; 
        }
            
        if (nextState[relatedBoatIndex] == 4){ //wait: if joined           
            rew = R_Service - (qSize*(1/this.avgWaitingTime)+servTime)/10000;//
            System.out.println("reward: "+rew);
            return rew;//
        }
        
        return rew;
    }
    public double getNextReward(int action,int[] state, int[] nextState,double servTime,int qSize){//immidiate reward given by the system
       // int[] tempState = getNextState(action,state);
        double rew = 0.0;
       
        if (nextState[0]== 5){//fail
            rew = (R_Fail*(this.avgWaitingTime/this.avgArrivingTime))+ qSize;
            System.out.println("reward: "+rew);
            return rew;
        }
        if ((nextState[0]==0)&&((state[0]==1)||(state[0]==2)||(state[0]==3))){ //succesful balk
            rew = R_Task+((state[1]*1.0)/100);//setting 4: #tasks/100
            System.out.println("reward: "+rew);
            return rew; 
        }
            
        if (nextState[0] == 4){ //wait: if joined           
            //rew = R_Service/(qSize+1);
            rew = R_Service - (qSize*(1/this.avgWaitingTime)+servTime)/10000;//
            //if(tempState[2]==1)
            System.out.println("reward: "+rew);
            return rew;//
        }
        
        return rew;
    }
    public boolean terminalState(BoatState s){//don't need change
        if (s.getReqType() == BoatState.ReqType.Fail)
            return true;
        if (s.numOfRemainingTasks == 0)
            return true;
        return false;
    }
    public boolean endState(int[] s){ //don't need change
        for (int i=0;i<s.length;i++)
            System.out.print("endState["+i+"]= "+s[i]+", ");
        if (s[0] == 5){//fail 
            System.out.println("Terminal State!! Fail");
            return true;
        }
        if (s[1] == 0){//all tasks done
            System.out.println("Terminal State!! All tasks done");
            return true;
        }
        return false;
    }
    public BoatPolicy getPolicy(){
        return this.policy;
    }
    public BoatWorld(int bId,int sType,double wt,double at) {
	
        this.boatId = bId;
      //  this.localOnly = locOnly;
        this.stateType = sType;
        this.avgWaitingTime = wt;
        this.avgArrivingTime = at;
        // Creating new policy with dimensions to suit the world.
        policy = new BoatPolicy( this.dimSize , this.boatId, this.stateType);

        // Initializing the policy with the initial values defined by the world.
      //  double initVal = Double.NEGATIVE_INFINITY;
        policy.initValues(0.0);

        learningMethod = Q_LEARNING;  //Q_LAMBDA;//SARSA;
        actionSelection = E_GREEDY;

        // set default values
        epsilon = 0.2;//0.2;
        temp = 1;

        alpha = 0.1; //  alpha = 1 //learning rate
        gamma = 0.9; //discount factor
        lambda = 0.1;  // gamma = 0.1, l = 0.5 (l*g=0.05)is a good choice.

        System.out.println( "RLearner initialized" );
	
    }        
    public int selectAction( int[] state , int index ) { //don't need change// for full state does not need change too
        System.out.println("selectAction>>");
        for (int i=0;i<state.length;i++)
            System.out.print(" in state["+i+"]: "+state[i]+", ");
        System.out.println();
           
	double[] qValues = policy.getQValuesAt( state );
	int selectedAction = -1;
    
	switch (actionSelection) {
	    
	case E_GREEDY : {
	    
	    random = false;
	    double maxQ = -Double.MAX_VALUE;
	    int[] doubleValues = new int[qValues.length];
	    int maxDV = 0;
	    
	    //Explore
	    if ( Math.random() < epsilon ) {
		selectedAction = -1;
		random = true;
	    }
	    else {
	    
		for( int action = 0 ; action < qValues.length ; action++ ) {
		    
		    if( qValues[action] > maxQ ) {
			selectedAction = action;
			maxQ = qValues[action];
			maxDV = 0;
			doubleValues[maxDV] = selectedAction;
		    }
		    else if( qValues[action] == maxQ ) {
			maxDV++;
			doubleValues[maxDV] = action; 
		    }
		}
		
		if( maxDV > 0 ) { //breaking ties
		    int randomIndex = (int) ( Math.random() * ( maxDV + 1 ) );
		    selectedAction = doubleValues[ randomIndex ];
		}
	    }
	    
	    // Select random action if all qValues == 0 or exploring.
	    if ( selectedAction == -1 ) {
		
		// System.out.println( "Exploring ..." );
		selectedAction = (int) (Math.random() * qValues.length);
	    }
	    
	    // Choose new action if not valid.
            if (stateType!=2){
                while( ! validAction(selectedAction,state ) ) {

                    selectedAction = (int) (Math.random() * qValues.length);
                    // System.out.println( "Invalid action, new one:" + selectedAction);
                }
            }
            else{
                while( ! validAction(selectedAction,state,index) ) {
                    selectedAction = (int) (Math.random() * qValues.length);
                }
            }
	    
	    break;
	}
	
	case SOFTMAX : {
	    
	    int action;
	    double prob[] = new double[ qValues.length ];
	    double sumProb = 0;
	    
	    for( action = 0 ; action < qValues.length ; action++ ) {
		prob[action] = Math.exp( qValues[action] / temp );
		sumProb += prob[action];
	    }
	    for( action = 0 ; action < qValues.length ; action++ )
		prob[action] = prob[action] / sumProb;
	    
	    boolean valid = false;
	    double rndValue;
	    double offset;
	    
	    while( ! valid ) {
		
		rndValue = Math.random();
		offset = 0;
		
		for( action = 0 ; action < qValues.length ; action++ ) {
		    if( rndValue > offset && rndValue < offset + prob[action] )
			selectedAction = action;
		    offset += prob[action];
		    // System.out.println( "Action " + action + " chosen with " + prob[action] );
		}

		if( validAction( selectedAction,state ) )
		    valid = true;
	    }
	    break;
	    
	}
	}
        System.out.println("selectedAction: "+selectedAction+" <<selectAction");
	return selectedAction;
    }
}
