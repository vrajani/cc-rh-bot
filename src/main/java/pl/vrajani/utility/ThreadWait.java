package pl.vrajani.utility;

public class ThreadWait {

    public static void waitFor(long milis){
        try{
            Thread.sleep(milis);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
