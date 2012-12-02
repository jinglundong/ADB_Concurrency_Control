package mainserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

import entity.Request;
import entity.RequestType;

public class MainServer {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Scanner token;
        String line = "";
        while(true){
            line = scanner.nextLine();   
            String[] instructions = line.trim().split(";"); 
            for (String str: instructions){
                String[] words = str.split("[^\\w|\\s]+");
                parse(words);
                System.out.println("end of instruction");
                //System.out.println(Arrays.deepToString(words));
            }
            
            //System.out.println(Arrays.deepToString(instructions));
        }
    }
    
    public static Request parse(String[] words) throws IOException{
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
            return new Request(form(words[2]), form(words[1]), RequestType.WRITE, null);                                
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
                return new Request(RequestType.DUMP, form(words[1]));                
            default:
                throw new IllegalArgumentException("wrong number of arguments");
            }
        }
        
        return null;
    }
    
    public static boolean validWordLength(String[] words, String word, int length) throws IOException{        
        String firstWord = words[0].trim().toLowerCase();
        if (firstWord.equals(word)){
            if (words.length == length){
                return true;
            }
            else{
                throw new IOException("begin need " + length + " arguments");
            }
        }
        else{
            return false;
        }
    }
    
    public static String form(String str){
        return str.trim().toLowerCase();
    }
}
