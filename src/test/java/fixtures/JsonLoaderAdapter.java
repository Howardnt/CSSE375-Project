package fixtures;

import jakarta.json.JsonWriter;
import jakarta.json.stream.*;
import jakarta.json.stream.JsonParser.*;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import java.io.*;

public class JsonLoaderAdapter implements LoaderInterface {

    private final JsonParser parser;
    String next;

    public JsonLoaderAdapter(String dataFileName, String directoryName) throws FileNotFoundException {
        JsonReader reader = Json.createReader(new FileReader(directoryName + dataFileName));
        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.write(reader.read());
        writer.close();

        parser = Json.createParser(new StringReader(sw.toString()));
        getNextLine(); //reads up to the first piece of valuable information
    }

    public boolean hasNext(){
        return parser.hasNext();
    }

    public String next(){
        String val = this.next;
        getNextLine();
        return val;
    }

    public boolean getNextLine(){
        if (!parser.hasNext()) return false;

        Event next = parser.next();
        if (next == Event.VALUE_STRING || next == Event.VALUE_NUMBER){
            this.next = parser.getString();
            return true;
        } else {
            return getNextLine();
        }
    }
}
