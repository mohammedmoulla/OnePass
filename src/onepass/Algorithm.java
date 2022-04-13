/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onepass;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Algorithm {
    ArrayList<File> files;
    int n ;
    int number;
    int threshold;
    int frequency;
    ArrayList<ArrayList <String>> tokens;
    HashSet <String> terms;
    ArrayList<Cluster> clusters;
    String result;
    
    double [][] TF;
    double [][] FT; //transpose
    
    Algorithm(ArrayList<File> files,int threshold,int frequency) {
        this.files = files;
        this.threshold = threshold;
        this.frequency = frequency;
        n = files.size();
        tokens = new ArrayList<>(n);
        terms = new HashSet<>();
        clusters = new ArrayList<>();
        result = "";
        number = 1;
        TF = null;
        start();
    }
    
    public void start () {
        tokenize();
        preProcess();
        createTF ();
        run ();
    }
    void tokenize() {
        try {
            for (File f : files) {
                Scanner input = new Scanner(f);
                ArrayList <String> arr = new ArrayList<>();
                while (input.hasNextLine()) {
                    String line = input.nextLine();
                    arr.addAll(Arrays.asList(line.split(" ")));
                }                
                tokens.add(arr);
                
                input.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
    void preProcess() {
       
        try {
            //read stop words
            File f = new File("stop words.txt");
            Scanner input = new Scanner(f);
            ArrayList <String> stopWords = new ArrayList<>();
            while (input.hasNextLine()) {
                String line = input.nextLine();
                stopWords.add(line);
            }
            input.close();
            
            //remove stop words (the , of , ...)
            System.out.println("*** removing stop words ***");
            for (ArrayList<String> a : tokens){
                System.out.println("before "+a.toString());
                Iterator itr = a.iterator();
                while (itr.hasNext()){
                    String s = (String) itr.next();
                    int index = stopWords.indexOf(s);
                    
                    if (index != -1)
                        itr.remove();
                        
                }//end of while
                System.out.println("after "+a.toString());
            }//end of for
            
            System.out.println("*** removing last ending ***");
            //Stemming (remove last ending)
             for (ArrayList<String> a : tokens){
                System.out.println("before "+a.toString());

                for (int i=0; i<a.size(); i++){
                    String s = a.get(i);
                    if (s.endsWith("ing"))
                            a.set(i, s.substring(0,s.length()-3));
                    else if (s.endsWith("er")||s.endsWith("ed")||s.endsWith("ly"))
                            a.set(i, s.substring(0,s.length()-2));
                        
                }//end of for
                
                System.out.println("after "+a.toString());
            }//end of for
                   
                       
             
            System.out.println("*** removing low frequency ***");
            //prune words (remove low frequency words)
            for (ArrayList<String> a : tokens){
                System.out.println("before "+a.toString());
                Iterator itr = a.iterator();
                while (itr.hasNext()){
                    String s = (String) itr.next();
                    int count = Collections.frequency(a, s);
                    if (count <=frequency)
                        itr.remove();
                }//end of while
                System.out.println("after "+a.toString());
               
            }//end of for
            
                
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Algorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
    void createTF () {
        
        for (ArrayList<String> a : tokens)
            terms.addAll(a.subList(0, a.size()));
        
        System.out.println("*** terms ***");
        System.out.println(terms.toString());
        
        int m = terms.size();
        int n = tokens.size();
        //line --> terms
        //column --> file
        TF = new double [m][n];
        FT = new double [n][m];
        //start with first row
        int index =0;
        for (String term : terms){
            int j = 0;
            for (ArrayList<String> a : tokens){
                int x = Collections.frequency(a, term);
                TF[index][j] = x;
                FT[j][index] = x;
                j++;
            }
            index++;
        }
        System.out.println("*** original ***");
        for (int i=0; i<m; i++){
            for (int j=0; j<n; j++)
                System.out.print(TF[i][j]+" ");
            System.out.println("");
        }
        
       
        
    }
    double similiarity(double [] A , double [] B){
        if (A.length != B.length)
            return -1;
        double sum = 0;
        for (int i=0; i<A.length; i++)
            sum += A[i]*B[i];
        return sum;
    }
    
    double [] average (double [] A , double [] B) {
        if (A.length != B.length)
            return null;
        double [] t = new double [A.length];
        for (int i=0; i<A.length; i++)
            t[i] = (A[i]+B[i])/2;
        return t;
        
    }
    
    void run () {
        //add first file to first class
        int number = 1;
        Cluster cluster = new Cluster("C"+number,FT[0].clone());
        cluster.files.add(files.get(0).getName());
        clusters.add(cluster);
        
        for (int j=1; j<n; j++){
            double max = -1;
            int max_index = -1;
            
            int i = 0;
            for (Cluster C : clusters){
                double x = similiarity(FT[j],C.arr);
                if (x>max){
                    max = x;
                    max_index = i;
                }
                i++;
            }//end of for
            
            if (max > threshold){
                //add it to the max cluster
                Cluster D = clusters.get(max_index);
                D.files.add(files.get(j).getName());
                D.arr = average(D.arr,FT[j]);
                
            }else {
                //create new cluster
                number++;
                Cluster cluster2 = new Cluster("C"+number,FT[j].clone());
                cluster2.files.add(files.get(j).getName());
                clusters.add(cluster2);
            }
            
        }//end of for
        
        for (Cluster C : clusters){
            System.out.println("name = "+C.name);
            System.out.println("files = "+C.files.toString());
            result += C.name +" = "+C.files.toString()+"\n";
            System.out.println("arr = "+Arrays.toString(C.arr));
            System.out.println("######################################");
        }
        
    }
    
    public String getResult () {
        return result;
    }
    
}
