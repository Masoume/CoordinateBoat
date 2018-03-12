/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.proxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Masoume
 */
public class BoatState {
    
    public enum ReqType { Normal,BatteryRecharge, DangeArea, ConnectionLost,Wait,Fail}//state local
    ReqType reqType;
    int boatId;
    int qSize; //state global
    int numOfRemainingTasks;
    int reqTypeInt;//0,1,2,3,4,5
    int[] stateLocal = {0,0}; //Req_type, #tasks
    int[] stateArr = {0,0,0}; //Req_type, #tasks, qSize
    int[] fullState = {0,0,0,0,0,0,0,0,0,0}; //local states of all boats
    int[] PrevStateLocal = {0,0}; //Req_type, #tasks
    int[] PrevStateArr = {0,0,0}; //Req_type, #tasks, qSize
    int[] PrevFullState = {0,0,0,0,0,0,0,0,0,0};
    int prevAct = 1;//Balk or Move-on//initial value
    double[] actionValue = {0.0,0.0};//,0.0};
    
    double totalAccRew = 0.0;
    double totalRew = 0.0;
    
    boolean localOnly; //if only local state info is accesible by each boat
    int stateType;//0: LocalOnly, 1: localGlobal, 2: fullState
    // Probability of failure 
    public static final int P_Battery = 9;
    public static final int P_DangeArea = 4;
    public static final int P_Connection = 2; 
    
    public BoatState(int bId,int sType){
        this.boatId = bId;
        this.stateType = sType;
        this.qSize = 0; //state global
        this.reqType = ReqType.Normal;
        this.reqTypeInt = 0;//integer value for state type
        this.numOfRemainingTasks = 0;  
       // this.stateGlobal = 0;
        this.stateLocal[0] = this.stateLocal[1]=0;
        this.stateArr[0]=this.stateArr[1]=this.stateArr[2]=0;
        for (int i=0;i<10;i++)
            this.fullState[i] = 0;
        this.actionValue[0]=this.actionValue[1]=0.0;//this.actionValue[2]=0.0;
    }
    public boolean equal(BoatState b){
        if ((b.getqsize() == this.qSize)&&(b.getReqTypeInt()==this.reqTypeInt) && (b.getNumOfRemainingTasks()==this.getNumOfRemainingTasks()))
            return true;
        return false;
    }
    public void updateVal(int[] reqT,int[] nTasks,int qSize){
        int j = 0;
        for (int i=0;i<5;i++){//5=no. of Boat          
            fullState[j]=reqT[i];
            fullState[j+1]=nTasks[i];
            j = j+2;
        }
        this.setqSize(qSize);
    }
    public void updateVal(int i,int j,int k){
        this.setReqType(i);
        this.setNumOfRemainingTasks(j);
        this.setqSize(k);
    }
    public void setqSize(int i){
        this.PrevStateArr[2]=this.stateArr[2];//before updating
        this.qSize = i;
        this.stateArr[2]=i;
    }
    public void setNumOfRemainingTasks(int i){
        this.PrevStateLocal[1]= this.stateLocal[1];
        this.PrevStateArr[1]=this.stateArr[1];//before updating
        this.numOfRemainingTasks = i;
        this.stateLocal[1] = i;
        this.stateArr[1]=i;
    }
    public void setReqType(int i){
        this.PrevStateLocal[0]= this.stateLocal[0];
        this.PrevStateArr[0]=this.stateArr[0];//before updating
        this.reqTypeInt = i;
        this.stateLocal[0] = i;
        this.stateArr[0] = i;
        switch(i){
            case 0: this.reqType = ReqType.Normal;
                break;
            case 1: this.reqType = ReqType.BatteryRecharge;
                break;
            case 2: this.reqType = ReqType.DangeArea;
                break;
            case 3: this.reqType = ReqType.ConnectionLost;
                break;
            case 4: this.reqType = ReqType.Wait;
                break;
            case 5: this.reqType = ReqType.Fail;
                break;
            default: this.reqType = ReqType.Fail;
                break;         
        }
    }
    public void setActionVal(double[] v){
        this.actionValue[0] = v[0];
        this.actionValue[1] = v[1];
    //    this.actionValue[2] = v[2];
    }
    public void setPrevAction(int iAction){
        this.prevAct = iAction;
    }
    public int getqsize (){
        return this.qSize;
    }
    public int getNumOfRemainingTasks(){
        return this.numOfRemainingTasks;
    }
    public int getReqTypeInt(){
        return this.reqTypeInt;
    }
    public ReqType getReqType(){
        return this.reqType;
    }
    public int[] getStateArr() {
        if (stateType==0)//localOnly
            return stateLocal;
        else if (stateType==1)//localGlob
            return stateArr;
        else //fullState
            return fullState;
    } 
    public int[] getPrevStateArr(){
        if (stateType==0)//localOnly
            return PrevStateLocal;
        else if (stateType==1)//localGlob
            return PrevStateArr;
        else //fullState
            return PrevFullState;
    }
    public int getPrevAction(){
        return prevAct;
    }
    public double[] getActionVal(){
        return actionValue;
    }
    public void printCurrentState(){
        System.out.print("Current State: ");
        int[] tmp = this.getStateArr();
        for (int i=0;i<tmp.length;i++)
            System.out.print(tmp[i]+" ");
        System.out.println();
    }
    public void updateTotalRew(double AccRew,double Rew){
        this.totalAccRew = this.totalAccRew + AccRew;
        this.totalRew = this.totalRew + Rew;
    }
    public double getTotalRew(){
        return this.totalRew;
    }
    public double getTotalAccRew(){
        return this.totalAccRew;
    }
    public void setLocalOnly(boolean loc){
        this.localOnly = loc;
    }
    public boolean getLocalOnly(){
        return this.localOnly;
    }
    public void showTotalRew(){
        System.out.println("Total accumulated reward: "+this.totalAccRew);
        System.out.println("Total reward: "+this.totalRew);
    }
    public void saveTotalAccRew(){    
        FileWriter fw;
        try {
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totaccrew"+ this.boatId+".txt");
            if(!file.exists()){
                file.createNewFile();
               // bExist = true;
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter pw = new PrintWriter(bw)) {
                pw.println(totalAccRew);
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    public void saveTotalRew() {
        FileWriter fw;
        try {
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totrew"+ this.boatId+".txt");
            if(!file.exists()){
                file.createNewFile();
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            try (PrintWriter pw = new PrintWriter(bw)) {
                pw.println(totalRew);
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
