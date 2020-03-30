package utils;

public interface Utils {
    public final static String EXIT = "Exit";
    public final static String CONNECTE="liste co";
    public final static String QUISUISJE="mon nom";
    
    public  static class fonctionUtile {

        
        /**
         * 
         * @param toTest
         * @return false si il  contient  la chaine et false si il  ne contient  pas la chaine 
         */
  public  static boolean  chainControl (String toTest )  {
        return  !("Exit".equals(toTest)||"liste co".equals(toTest)||"mon nom".equals(toTest)||toTest.contains("@") );
           
        
    }
}
    
}


