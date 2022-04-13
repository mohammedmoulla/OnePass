/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onepass;

import java.util.ArrayList;

/**
 *
 * @author M#
 */
public class Cluster {
    public String name;
    public double [] arr;
    public ArrayList<String> files;
    public Cluster(String name, double[] arr) {
        this.name = name;
        this.arr = arr;
        files = new ArrayList<>();
    }
    
}
