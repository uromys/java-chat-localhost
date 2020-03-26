package utils;

public interface Utils {
    public final static String EXIT = "Exit";
    
    
    
    public  static class fonctionUtile {

  public  static boolean  chainControl (String toTest )  {
        return  !("Exit".equals(toTest)||"list users".equals(toTest)||"my name".equals(toTest) );
           
        
    }
}
    
}


