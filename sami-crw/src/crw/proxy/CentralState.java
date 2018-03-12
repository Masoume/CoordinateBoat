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
public class CentralState {
    
    int[] fullState = {0,0,0,0,0,0,0,0,0,0}; //local states of all boats
    double[] actionValue = {0.0,0.0};//,0.0};
    int numberOfBoats = 0;
    double totalAccRew = 0.0;
    double totalRew = 0.0;
    
    BoatWorld CentralWorld;
    
    // Probability of failure 
    public static final int P_Battery = 9;
    public static final int P_DangeArea = 4;
    public static final int P_Connection = 2;
    
    int stateType = 2;
    int qSize = 0;
    
    public CentralState(int numBoats,double wt,double at){
        this.numberOfBoats = numBoats;
        
        for (int i=0;i<numberOfBoats*2;i++)
            this.fullState[i] = 0;
        
        this.actionValue[0]=this.actionValue[1]=0.0;
        
        //boatId, sType, avgWt
        this.CentralWorld = new BoatWorld(0,2,wt,at);
    }
    
    public void updateVal(int[] reqT,int[] nTasks,int qSize){
        int j = 0;
        for (int i=0;i<numberOfBoats;i++){         
            fullState[j]=reqT[i];
//            nTasks[i]-= 4;
//            if (nTasks[i]<=0)
//              nTasks[i] = 0;  
            fullState[j+1]=nTasks[i];            
            j = j+2;
        }
        this.setqSize(qSize);
    }
    
    public BoatWorld getCentralWorld(){
        return this.CentralWorld;
    }
    
    public int[] getStateArr() {
        return fullState;
    } 
    
    public void setqSize(int n){
        this.qSize = n;
    }
    
    public int getqsize (){
        return this.qSize;
    }
    
    public void setActionVal(double[] v){
        this.actionValue[0] = v[0];
        this.actionValue[1] = v[1];
    }
    
    public double[] getActionVal(){
        return this.actionValue;
    }
    
    public void printCurrentState(){
        System.out.print("Current State: ");
        int[] tmp = this.getStateArr();
        for (int i=0;i<tmp.length;i++)
            System.out.print(tmp[i]+" ");
        System.out.println();
    }
    
    public void updateTotalRew(double AccRew,double Rew){
        this.totalAccRew += AccRew;
        this.totalRew += Rew;
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
        FileWriter fw;
        try {
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totaccrew_fullState.txt");
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
            File file = new File("/Users/Masoume/Desktop/CoordinateBoat/totrew_fullState.txt");
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
