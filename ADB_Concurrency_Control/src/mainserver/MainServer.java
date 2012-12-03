package mainserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import entity.Request;
import entity.RequestType;

/**
 * 
 * @author jinglun
 *
 */
public class MainServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);        
        String line = "";
        while(true){
            line = scanner.nextLine();               
            String[] instructions = line.trim().split(";"); 
            try{
                for (String str: instructions){
                    String[] words = str.split("[^\\w|\\s]+");
                    System.out.println(parse(words));                
                }
            }
            catch (IOException e){
                System.out.println("An unsupport line of instructions, because of:\n "+ 
                        e + "\ntry again:");                
            }
        }
    }
    
    
    /**
     * Construct request from String[] which contains one single instruction
     * @param words
     * @return
     * @throws IOException
     */
    private static Request parse(String[] words) throws IOException{
        if (validWordLength(words, "begin", 2)){            
            return new Request(null, form(words[1]), RequestType.BEGIN, null);                                
        }
        if (validWordLength(words, "beginro", 2)){            
            return new Request(null, form(words[1]), RequestType.BEGINRO, null);                                
        }
        if (validWordLength(words, "w", 4)){            
            return new Request(form(words[2]), form(words[1]), RequestType.WRITE, form(words[3]));                                
        }
        if (validWordLength(words, "r", 3)){            
            return new Request(form(words[2]), form(words[1]), RequestType.READ, null);                                
        }
        if (validWordLength(words, "fail", 2)){            
            return new Request(RequestType.FAIL, form(words[1]));                                
        }
        if (validWordLength(words, "recover", 2)){            
            return new Request(RequestType.RECOVER, form(words[1]));                                
        }
        if (form(words[0]).equals("dump")){      
            switch (words.length){
            case 1:
                return new Request(RequestType.DUMP, null);                            
            case 2:
                if (form(words[1]).matches("x.*")){ //dump resource
                    return new Request(form(words[1]), null, RequestType.DUMP, null);
                }
                else{   //dump site
                    return new Request(RequestType.DUMP, form(words[1]));
                }                                
            default:
                throw new IOException("wrong number of arguments of \"DUMP\"");
            }
        }
        if (validWordLength(words, "end", 2)){            
            return new Request(null, form(words[1]), RequestType.END, null);                                
        }
        throw new IOException("The following instruction is not supported :\n" 
                + Arrays.deepToString(words) );
    }
    
    
    /**
     * Check the number of arguments
     * @param words
     * @param word
     * @param length
     * @return
     * @throws IOException
     */
    private static boolean validWordLength(String[] words, String word, int length) throws IOException{        
        String firstWord = words[0].trim().toLowerCase();
        if (firstWord.equals(word)){
            if (words.length == length){
                return true;
            }
            else{
                throw new IOException(word + " need " + length + " arguments");
            }
        }
        else{
            return false;
        }
    }

    
    private static String form(String str){
        return str.trim().toLowerCase();
    }
    
    
    /**
     * Generate data for different site depends on the site number.
     * Even variables are created for each site.
     * Odd variables created only for site (siteNum + 1) %10
     * @param siteNum In our test case, 1 <= siteNum <= 10
     * @return 
     */
    static HashMap<String, String> createData(int siteNum){        
        HashMap<String, String> data = new HashMap<String, String>();
        for (int i=2; i<=20; i+=2){            
            data.put("x" + i, String.valueOf(i*10));
        }
        if (siteNum % 2 == 0){
            data.put("x" + (siteNum -1), String.valueOf(10*(siteNum-1)));
            data.put("x" + (siteNum -1 + 10), String.valueOf(10*(siteNum-1+10)));
        }
        return data;
    }
    
    
    /**
     * A set of odd variables in given site
     * @param siteNum
     * @return  return empty set if it is a odd site
     */
    static Set<String> createUnique(int siteNum){
        Set<String> unique = new HashSet<String>();
        if (siteNum % 2 == 0){  
            unique.add("x" + (siteNum -1));
            unique.add("x" + (siteNum -1 + 10));
        }
        return unique;
    }
}
