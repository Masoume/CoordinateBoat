/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.proxy;

/**
 *
 * @author Masoume
 */
public class BoatWorld {
    
    public enum Action { Join, Balk} //join=0;balk=1;
    int[] actions = {0,1};//join=0;balk|moveOn=1;
    int[] state;
    boolean random = false;
    int boatId;
    //BoatState boatState;
    BoatPolicy policy;

    // dimension of each state feature: { reqType,#tasks,maxQsize, Actions }
    final int[] dimSize = { 6, 7, 6, 2 };
    
    // Learning types
    public static final int Q_LEARNING = 1;
    public static final int SARSA = 2;
    public static final int Q_LAMBDA = 3; // Good parms were lambda=0.05, gamma=0.1, alpha=0.01, epsilon=0.1

    // Action selection types
    public static final int E_GREEDY = 1;
    public static final int SOFTMAX = 2;
    
    // Reward 
    public static final double R_Task = 0.3;
    public static final double R_Fail = -2;
    public static final double R_Service = 1; 
    public static final double R_Wrong_Balk = -2; 

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
    public boolean validAction(int action, int[] state ){
        switch(action){
            case 0: //join
                if ((state[0]==1)||(state[0]==2)||(state[0]==3))//only if there is a request, join makes sense
                    return true;
                else return false;
            case 1://balk or move on
                if ((state[0]==4)||(state[0]==5)||(state[2]==0))//wait,fail
                    return false;
                else return true;
            default:
                return false;
        }
    }
    //int[] getNextState( int action, double prob,int[]state ){
    public int[] getNextState( int action, int[]state ){
        int[] retState = {5,state[1],state[2]};//5 default value for fail state
        if(validAction(action,state)){
            if (action==0){//join
                retState[0]=4;//wait
                retState[1]=state[1];//#tasks
                retState[2]=state[2]+1;//qsize
            }
            else{ //balk
                retState[0]=state[0];//the same type
                retState[1]=state[1];//#tasks
                retState[2]=state[2];//qsize
                //if here i apply randomness, it would be a dynamic prog o.w. i should 
                // find another way of considering the dynamic of the environment
            }        
        }
        return retState;
    }
    public double getNextReward(int action,int[] state){//immidiate reward
        int[] tempState = getNextState(action,state);
        double rew = 0.0;
        if (tempState[0]== 5){//fail
            System.out.println("reward "+R_Fail);
            return R_Fail;
        }
        if (tempState[0] == 4){ //wait: if joined
            if(tempState[2]==1)
                rew = R_Service;
            else 
                rew = R_Service/(tempState[2]-1);
            System.out.println("reward "+rew);
            return rew;//
        }
        if (action == 1){//balk
            if (state[2]==0){//if q is empty, balking has a cost
                System.out.println("reward "+R_Wrong_Balk);
                return R_Wrong_Balk;
            }
        }
        return rew;
    }
    public boolean terminalState(BoatState s){
        if (s.getReqType() == BoatState.ReqType.Fail)
            return true;
        if (s.numOfRemainingTasks == 0)
            return true;
        return false;
    }
    public boolean endState(int[] s){
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
    public BoatWorld(int bId) {
	
        this.boatId = bId;
        // Creating new policy with dimensions to suit the world.
        policy = new BoatPolicy( this.dimSize , this.boatId);

        // Initializing the policy with the initial values defined by the world.
      //  double initVal = Double.NEGATIVE_INFINITY;
        policy.initValues(0.0);

        learningMethod = Q_LEARNING;  //Q_LAMBDA;//SARSA;
        actionSelection = E_GREEDY;

        // set default values
        epsilon = 0.2;//0.2;//after 200 episodes
        temp = 1;

        alpha = 0.1; //  alpha = 1 
        gamma = 0.9;
        lambda = 0.1;  // gamma = 0.1, l = 0.5 (l*g=0.05)is a good choice.

        System.out.println( "RLearner initialized" );
	
    }        
    public int selectAction( int[] state ) {
        System.out.println("selectAction>>");
        for (int i=0;i<state.length;i++)
            System.out.print(" state["+i+"]: "+state[i]+", ");
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
		
		if( maxDV > 0 ) {
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
	    while( ! validAction(selectedAction,state ) ) {
		
		selectedAction = (int) (Math.random() * qValues.length);
		// System.out.println( "Invalid action, new one:" + selectedAction);
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
        System.out.println("<<selectAction");
	return selectedAction;
    }
}
