/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Masoume
 */
public class BoatPolicy {
    
    int[] dimSize;
    int boatId;
    int stateType;//0:LocalOnly 1:LocGlob 2:Full
    double[] qValues;//size of actions
    public Object qValuesTable_prev;
    double qValuesTable[][][][];//localGlobal
    double qValuesTableLocal[][][];//new: for local only
    double qValuesTableFull[][][][][][][][][][][];//new: for full state
    int numStates, numActions;
    boolean b = false;
    String QfileName = "";
  //  String filePAth = System.getProperty ("user.dir");

    BoatPolicy( int[] dimSize , int id, int sType) {
        this.dimSize = dimSize;
	this.boatId = id;
        this.stateType = sType;
	// Create n-dimensional array with size given in dimSize array.
        //qValuesTable = Array.newInstance( double.class, dimSize );
        if (this.stateType==0)//localOnly
        {
            qValuesTableLocal = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]];
            this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfileLocal"+id;         
          //  this.QfileName = filePAth+"/myfileLocal"+id;
        }
        else if (this.stateType==1)//localGlobal
        {
            qValuesTable = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]][this.dimSize[3]];
            this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfile"+id;
           // this.QfileName = filePAth+"/myfile"+id;
        }
        else //central full state
        {
            qValuesTableFull = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]][this.dimSize[3]][this.dimSize[4]][this.dimSize[5]][this.dimSize[6]][this.dimSize[7]][this.dimSize[8]][this.dimSize[9]][this.dimSize[10]];
            this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfileFull";
        }       
	// Get number of states.
	numStates = dimSize[0];
	for( int j = 1 ; j < dimSize.length - 1 ; j++)
	    numStates *= dimSize[j];
	
	// Get number of actions.
	numActions = dimSize[dimSize.length - 1]; 
        System.out.println("QfileName: "+QfileName);
    }
//    BoatPolicy( int[] dimSize , int id, boolean local) {
//	
//	this.dimSize = dimSize;
//	this.boatId = id;
//        this.localOnly = local;
//	// Create n-dimensional array with size given in dimSize array.
//        //qValuesTable = Array.newInstance( double.class, dimSize );
//        if (!this.localOnly){
//            qValuesTable = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]][this.dimSize[3]];
//            this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfile"+id;
//        }
//        else {
//            qValuesTableLocal = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]];
//            this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfileLocal"+id;
//        }
//        
//	// Get number of states.
//	numStates = dimSize[0];
//	for( int j = 1 ; j < dimSize.length - 1 ; j++)
//	    numStates *= dimSize[j];
//	
//	// Get number of actions.
//	numActions = dimSize[dimSize.length - 1]; 
//        System.out.println("QfileName: "+QfileName);
//     // this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfile"+id;
//        
//    //    b = this.readFromMyfile();
//        
//    }
    public void initValues( double initValue ) {
	int i;
	int state[] = new int[dimSize.length - 1];//the last dimension of dimSize is action
        b = loadQValue();
	System.out.println( "number of States: " + numStates ); 
        
        if (!b){
            for( int j = 0 ; j < numStates ; j++ ) {

                qValues = (double[]) myQValues( state );
               
                for( i = 0 ; i < numActions ; i++ ) {
                    // System.out.print( i );
                    Array.setDouble( qValues, i, ( initValue )); //+ 0.0000000000000000001 * Math.random() ) );
                    setQValuesTable(state,i,initValue);
                }

                state = getNextState( state );
            }
        }
        //else updateMyQValues();
    }
    private int[] getNextState( int[] state ) {

	int i;
	int actualdim = 0;
	
	state[actualdim]++;
	if( state[actualdim] >= dimSize[actualdim] ) {
	    while( ( actualdim < dimSize.length - 1 ) && ( state[actualdim] >= dimSize[actualdim] ) ) {
		actualdim++;
		
		if( actualdim == dimSize.length - 1 )
		    return state;
		
		state[actualdim]++;
	    }
	    for( i = 0 ; i < actualdim ; i++ ) 
		state[i] = 0;
	    actualdim = 0;
	}
	return state;
    }
    private void setQValuesTable(int[] state,int action, double val){
        if (stateType==0){//localOnly
            int i_0 = state[0];
            int i_1 = state[1];
            qValuesTableLocal[i_0][i_1][action] = val;
        }else if (stateType==1){ //localGlobal
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];
            qValuesTable[i_0][i_1][i_2][action] = val;
        }else{
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];
            int i_3 = state[3];
            int i_4 = state[4];
            int i_5 = state[5];
            int i_6 = state[6];
            int i_7 = state[7];
            int i_8 = state[8];
            int i_9 = state[9];
            qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][action] = val;
        }
        
//        if (localOnly){
//            int i_0 = state[0];
//            int i_1 = state[1];
//            qValuesTableLocal[i_0][i_1][action] = val;
//        }
//        else{
//            int i_0 = state[0];
//            int i_1 = state[1];
//            int i_2 = state[2];
//            qValuesTable[i_0][i_1][i_2][action] = val;
//        }
    }
    private double[] myQValues( int[] state ) {
        if (stateType==0){//localOnly
            int i_0 = state[0];
            int i_1 = state[1];
            double[] retQ = {qValuesTableLocal[i_0][i_1][0],qValuesTableLocal[i_0][i_1][1]};//,qValuesTableLocal[i_0][i_1][2]};
            return retQ;
        }
        else if (stateType==1) {//localGlobal
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];
            //int i_3 = state[3];
            double[] retQ = {qValuesTable[i_0][i_1][i_2][0],qValuesTable[i_0][i_1][i_2][1]};//,qValuesTable[i_0][i_1][i_2][2]};
            return retQ;
        }
        else{//fullState
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];
            int i_3 = state[3];
            int i_4 = state[4];
            int i_5 = state[5];
            int i_6 = state[6];
            int i_7 = state[7];
            int i_8 = state[8];
            int i_9 = state[9];
            double[] retQ = {qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][0],qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][1]};
            return retQ;
        }
        
//	for( i = 0 ; i < dimSize.length - 2 ; i++ ) {
//	    //descend in each dimension
//	    curTable = Array.get( curTable, state[i] );
//	}
//	//at last dimension of Array get QValues.
//	return (double[]) Array.get( curTable, state[i] );
    }
    
    public double[] getQValuesAt( int[] state ) {
        System.out.println(">>getQValuesAt");
	if (stateType==0)//localOnly
        {
            int i_0 = state[0];
            int i_1 = state[1];

            double[] returnValues;

            qValues[0] = qValuesTableLocal[i_0][i_1][0];
            qValues[1] = qValuesTableLocal[i_0][i_1][1];
      //      qValues[2] = qValuesTableLocal[i_0][i_1][2];
            returnValues = new double[ qValues.length ];
            System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
            System.out.println("getQValuesAt<<");
            return returnValues;
        }
        else if (stateType==1){//localGlobal
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];

            double[] returnValues;

            qValues[0] = qValuesTable[i_0][i_1][i_2][0];
            qValues[1] = qValuesTable[i_0][i_1][i_2][1];
       //     qValues[2] = qValuesTable[i_0][i_1][i_2][2];
            returnValues = new double[ qValues.length ];
            System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
            System.out.println("getQValuesAt<<");
            return returnValues;
        }
        else{//fullState
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];
            int i_3 = state[3];
            int i_4 = state[4];
            int i_5 = state[5];
            int i_6 = state[6];
            int i_7 = state[7];
            int i_8 = state[8];
            int i_9 = state[9];

            double[] returnValues;

            qValues[0] = qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][0];
            qValues[1] = qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][1];
   //         qValues[2] = qValuesTableFull[i_0][i_1][i_2][i_3][i_4][i_5][i_6][i_7][i_8][i_9][2];
            returnValues = new double[ qValues.length ];
            System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
            System.out.println("getQValuesAt<<");
            return returnValues;
        }
    }
    
 /*   public double[] getQValuesAt( int[] state ) {
	System.out.println(">>getQValuesAt");
	if (localOnly)
        {
            int i_0 = state[0];
            int i_1 = state[1];

            double[] returnValues;

            qValues[0] = qValuesTableLocal[i_0][i_1][0];
            qValues[1] = qValuesTableLocal[i_0][i_1][1];
            returnValues = new double[ qValues.length ];
            System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
            System.out.println("getQValuesAt<<");
            return returnValues;
        }
        else{
            int i_0 = state[0];
            int i_1 = state[1];
            int i_2 = state[2];

            double[] returnValues;

            qValues[0] = qValuesTable[i_0][i_1][i_2][0];
            qValues[1] = qValuesTable[i_0][i_1][i_2][1];
            returnValues = new double[ qValues.length ];
            System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
            System.out.println("getQValuesAt<<");
            return returnValues;
        }
    }*/
//    public double[] getQValuesAt( int[] state ) {
//	System.out.println(">>getQValuesAt");
//	int i;
//	Object curTable = (Object)qValuesTable;
//	double[] returnValues;
//
//	for( i = 0 ; i < dimSize.length - 2 ; i++ ) {
//	    //descend in each dimension
//	    curTable = Array.get( curTable, state[i] );
//	}
//	//at last dimension of Array get QValues.
//	qValues = (double[]) Array.get( curTable, state[i] );
//	returnValues = new double[ qValues.length ];
//	System.arraycopy( qValues, 0, returnValues, 0, qValues.length );
//        System.out.println("getQValuesAt<<");
//	return returnValues;
//    }
//    
    
    public void setQValue( int[] state, int action, double newQValue ) {
	Array.setDouble( qValues, action, newQValue );
        setQValuesTable(state,action,newQValue);
    }

    public double getMaxQValue( int[] state ) {
	
	double maxQ = -Double.MAX_VALUE;
	
	qValues = myQValues( state );
	
	for( int action = 0 ; action < qValues.length ; action++ ) {
	    if( qValues[action] > maxQ ) {
		maxQ = qValues[action];
	    }
	}
	return maxQ;
    }
    
    public double getQValue( int[] state, int action ) {
	
	double qValue = 0;
	
	qValues = myQValues( state );
	qValue = qValues[action];

	return qValue;
    }

    public int getBestAction( int[] state ) {
        
	double maxQ = -Double.MAX_VALUE;
	int selectedAction = -1;
	int[] doubleValues = new int[qValues.length];
	int maxDV = 0;

	qValues = myQValues( state );
        System.out.println("qValues are: "+qValues[0]+", "+qValues[1]);
        
	for( int action = 0 ; action < qValues.length ; action++ ) {
	    //System.out.println( "STATE: [" + state[0] + "," + state[1] + "]" ); 
	    //System.out.println( "action:qValue, maxQ " + action + ":" + qValues[action] + "," + maxQ );
	    
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
	    //System.out.println( "DOUBLE values, random selection, maxdv =" + maxDV );
	    int randomIndex = (int) ( Math.random() * ( maxDV + 1 ) );
	    selectedAction = doubleValues[ randomIndex ];
	}
	
	
	if( selectedAction == -1 ) {
	    //System.out.println("RANDOM Choice !" );
	    selectedAction = (int) ( Math.random() * qValues.length );
	}
	
	return selectedAction;
    
    }
    
    public void saveQValues() {
        try (PrintWriter writer = new PrintWriter(new File(QfileName+".txt"))) {
            int state[] = new int[dimSize.length - 1];
            for( int j = 0 ; j < numStates ; j++ ) {
                for (int i=0;i<qValues.length;i++)
                    writer.write(String.valueOf(getQValue(state,i))+" ");
                state = getNextState( state );
                writer.println(); //leave one line 
            }   
            writer.flush();  //flush the writer
            writer.close();  //close the writer      
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BoatPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean loadQValue() {
        try {
            Scanner scan = new Scanner(new File(QfileName+".txt"));
            
            int state[] = new int[dimSize.length - 1];
            int i;
            double initValue;
            for( int j = 0 ; j < numStates ; j++ ) {
                qValues =  myQValues(state );
                for( i = 0 ; i < numActions ; i++ ) {
                    // System.out.print( i );
                    initValue = Double.parseDouble(scan.next());
                    Array.setDouble( qValues, i, ( initValue )); 
                    setQValuesTable(state,i,initValue);
                }
                
                state = getNextState( state );
            }
            scan.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BoatPolicy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    public void writeInFile(){
        try{
            try (FileOutputStream fos = new FileOutputStream("/Users/Masoume/Desktop/CoordinateBoat/myfile"); 
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(this.qValuesTable);
            }
       }catch(IOException ioe){
           Logger.getLogger(BoatPolicy.class.getName()).log(Level.SEVERE, null, ioe);
       }
    }
    
    public boolean readFromMyfile(){
        
       // Object qValuesTable;
        try
        {
            FileInputStream fis = new FileInputStream("/Users/Masoume/Desktop/CoordinateBoat/myfile");
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                System.out.println("readFromMyfile");// + ois.readObject());
                int i_0 = this.qValuesTable[0].length;
                int i_1= this.qValuesTable[1].length;
                int i_2 = this.qValuesTable[2].length;
                int i_3 = this.qValuesTable[3].length;
                double[][][][] tmpDim4 = new double[i_0][i_1][i_2][i_3];
                tmpDim4 = (double[][][][])ois.readObject();
                
            //    System.out.println("read: "+obj.equals(this.qValuesTable));
           //     System.out.println(Arrays.deepToString((Object[]) qValuesTable));
//                this.qValuesTable = (double[][][][]) ois.readObject();
//                //update qValues from previous simulation run
//                int state[] = new int[dimSize.length - 1];
//                for( int j = 0 ; j < numStates ; j++ ) {
//                    qValues = myQValues( state );
//                    for( int action = 0 ; action < qValues.length ; action++ ) {
//                        System.out.println("qValues["+action+"]="+qValues[action]) ;
//                    }
//                }
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BoatPolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
            ois.close();
            fis.close();
           
            
            return true;
         }catch(IOException ioe){
             ioe.printStackTrace();
             return false;
          }

    }
}
