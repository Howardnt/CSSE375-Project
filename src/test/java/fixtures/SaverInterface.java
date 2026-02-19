package fixtures;

public interface SaverInterface {
    public boolean writeNext(String data);
    public boolean closeSaver();    
}
