/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apexsimulator;

import java.util.LinkedHashMap;

/**
 *
 * @author Paritosh Fulara
 */
public class GlobalVariables {
        public static LinkedHashMap<String, Integer> hmap = new LinkedHashMap<String, Integer>();
        public static int memory[] = new int[4000];
      
       /*
        Method for initializing the 16 architectural registers and memory location all to 0  
        */
        public void  Initialize()
        {
         for(int i=0;i<16;i++)
        {
           hmap.put("R"+i, 0);
        
        }
        hmap.put("X",0);
                
                
        for(int k=0;k<4000;k++ )
        {
          memory[k]=0;
        }
        }
}
