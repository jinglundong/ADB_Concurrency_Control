package mainserver;

import java.io.IOException;
import java.util.Scanner;

import entity.Request;
import entity.RequestType;

/**
 * 
 * @author jinglun
 *
 */
public class MainServer {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);        
        String line = "";
        while(true){
            line = scanner.nextLine();   
            String[] instructions = line.trim().split(";"); 
            for (String str: instructions){
                String[] words = str.split("[^\\w|\\s]+");
                System.out.println(parse(words));                
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
                throw new IllegalArgumentException("wrong number of arguments");
            }
        }
        if (validWordLength(words, "end", 2)){            
            return new Request(null, form(words[1]), RequestType.END, null);                                
        }
        throw new IOException("Instruction is not supported ");
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
                throw new IOException("begin need " + length + " arguments");
            }
        }
        else{
            return false;
        }
    }

    
    private static String form(String str){
        return str.trim().toLowerCase();
    }
}
