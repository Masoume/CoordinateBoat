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
    double[] qValues;//size of actions
    public Object qValuesTable_prev;
    double qValuesTable[][][][];
    int numStates, numActions;
    boolean b = false;
    String QfileName = "";

    BoatPolicy( int[] dimSize , int id) {
	
	this.dimSize = dimSize;
	this.boatId = id;
	// Create n-dimensional array with size given in dimSize array.
        //qValuesTable = Array.newInstance( double.class, dimSize );
        qValuesTable = new double[this.dimSize[0]][this.dimSize[1]][this.dimSize[2]][this.dimSize[3]];
                
	// Get number of states.
	numStates = dimSize[0];
	for( int j = 1 ; j < dimSize.length - 1 ; j++)
	    numStates *= dimSize[j];
	
	// Get number of actions.
	numActions = dimSize[dimSize.length - 1]; 
        
        this.QfileName = "/Users/Masoume/Desktop/CoordinateBoat/myfile"+id;
        
    //    b = this.readFromMyfile();
        
    }
    public void initValues( double initValue ) {
	int i;
	int state[] = new int[dimSize.length - 1];//the last dimension of dimSize is action
       // b = this.readFromMyfile();
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
        int i_0 = state[0];
        int i_1 = state[1];
        int i_2 = state[2];
        qValuesTable[i_0][i_1][i_2][action] = val;
    }
    private double[] myQValues( int[] state ) {
	int i_0 = state[0];
        int i_1 = state[1];
        int i_2 = state[2];
        //int i_3 = state[3];
	double[] retQ = {qValuesTable[i_0][i_1][i_2][0],qValuesTable[i_0][i_1][i_2][1]};
        return retQ;
        
//	for( i = 0 ; i < dimSize.length - 2 ; i++ ) {
//	    //descend in each dimension
//	    curTable = Array.get( curTable, state[i] );
//	}
//	//at last dimension of Array get QValues.
//	return (double[]) Array.get( curTable, state[i] );
    }
    public double[] getQValuesAt( int[] state ) {
	System.out.println(">>getQValuesAt");
	
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
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(QfileName+".txt"));
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
        } finally {
            writer.close();
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
//            int state2[] = new int[dimSize.length - 1];
//            for( int j = 0 ; j < numStates ; j++ ) {
//                
//                qValues = myQValues(state2 );
//                
//                for (int k=0;k<numActions;k++)
//                    System.out.println("qValues["+k+"]="+qValues[k]+" in state: "+ state2[0]+" "+state2[1]+" "+state2[2]);
//                
//                state2 = getNextState( state2 );
//            }
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BoatPolicy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    public void writeInFile(){
        try{
         FileOutputStream fos= new FileOutputStream("/Users/Masoume/Desktop/CoordinateBoat/myfile");
         ObjectOutputStream oos= new ObjectOutputStream(fos);
         oos.writeObject(this.qValuesTable);
       //  System.out.println(Arrays.deepToString((Object[]) qValuesTable));
//         oos.writeObject(Arrays.deepToString((Double[])this.qValuesTable));
     //    oos.writeObject(this.qValuesTable);
         oos.close();
         fos.close();
       }catch(IOException ioe){
            ioe.printStackTrace();
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
