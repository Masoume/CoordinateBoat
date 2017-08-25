/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.proxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
    
    public enum ReqType { Normal,BatteryRecharge, DangeArea, ConnectionLost,Wait,Fail}
    ReqType reqType;
    int qSize;
    int numOfRemainingTasks;
    int reqTypeInt;//0,1,2,3,4,5
    int[] stateArr = {0,0,0};
    double[] actionValue = {0.0,0.0};
    
    double totalAccRew = 0.0;
    double totalRew = 0.0;
    
    // Probability of failure 
    public static final int P_Battery = 9;
    public static final int P_DangeArea = 4;
    public static final int P_Connection = 2; 
    
    public BoatState(){
        this.qSize = 0;
        this.reqType = ReqType.Normal;
        this.reqTypeInt = 0;
        this.numOfRemainingTasks = 0;  
        this.stateArr[0]=this.stateArr[1]=this.stateArr[2]=0;
        this.actionValue[0]=this.actionValue[1]=0.0;
    }
    public boolean equal(BoatState b){
        if ((b.getqsize() == this.qSize)&&(b.getReqTypeInt()==this.reqTypeInt) && (b.getNumOfRemainingTasks()==this.getNumOfRemainingTasks()))
            return true;
        return false;
    }
    public void updateVal(int i,int j,int k){
        this.setReqType(i);
        this.setNumOfRemainingTasks(j);
        this.setqSize(k);
    }
    public void setqSize(int i){
        this.qSize = i;
        this.stateArr[2]=i;
    }
    public void setNumOfRemainingTasks(int i){
        this.numOfRemainingTasks = i;
        this.stateArr[1]=i;
    }
    public void setReqType(int i){
        this.reqTypeInt = i;
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
        return stateArr;
    } 
    public double[] getActionVal(){
        return actionValue;
    }
    public void printCurrentState(){
        System.out.print("Current State: ");
        int[] tmp = this.getStateArr();
        double[] tmpA = getActionVal();
        System.out.println(tmp[0]+", "+tmp[1]+", "+tmp[2]);
//        System.out.print("Current Action Value: ");
//        System.out.println(tmpA[0]+", "+tmpA[1]);
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
    public void showTotalRew(){
        System.out.println("Total accumulated reward: "+this.totalAccRew);
        System.out.println("Total reward: "+this.totalRew);
    }
    public void saveTotalAccRew(){
    
        FileWriter fw = null;
        boolean b = false;
      //  if (strFileName.contains("failStatistics")) b = true;
        try {
            //  File file = new File("C://myfile.txt");
            boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totaccrew.txt");
            if(!file.exists()){
                file.createNewFile();
                bExist = true;
            }   
            fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(totalAccRew);
//            if (bExist){pw.println(totalRew);bExist=false;}
//            else {pw.println(totalRew);}
 
            pw.close();
            //System.out.println("Data successfully appended at the end of file");
        } catch (IOException ex) {
            Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void saveTotalRew() {
        FileWriter fw = null;
        boolean b = false;
      //  if (strFileName.contains("failStatistics")) b = true;
        try {
            //  File file = new File("C://myfile.txt");
            boolean bExist = false;
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totrew.txt");
            if(!file.exists()){
                file.createNewFile();
                bExist = true;
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
            Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(BoatState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
