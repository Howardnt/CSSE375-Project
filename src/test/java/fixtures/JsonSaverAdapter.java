package fixtures;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class JsonSaverAdapter implements SaverInterface {

    private JsonGenerator gen;
    private int count;

    public JsonSaverAdapter(String dataFileName, String directoryName){
        count = 0;
        try {
            FileWriter writer = new FileWriter(directoryName + dataFileName);
            Map<String, Boolean> config = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
            JsonGeneratorFactory factory = Json.createGeneratorFactory(config);
            gen = factory.createGenerator(writer);
            gen.writeStartObject();
            gen.writeStartArray("inventory");
        } catch (IOException e) {
            System.out.println(">>Error trying to create output file to write inventory.");
            e.printStackTrace();
        }
    }

    public boolean writeNext(String data) {
        switch (count){
            case 0: gen.writeStartObject().write("serialNumber", data); break;
            case 1: gen.write("price", data); break;
            case 2: gen.write("builder", data); break;
            case 3: gen.write("model", data); break;
            case 4: gen.write("guitarType", data); break;
            case 5: gen.write("numStrings", data); break;
            case 6: gen.write("backWood", data); break;
            case 7: gen.write("topWood", data).writeEnd(); count = -1; break;
        }
        count++;
        return true;
    }

    public boolean closeSaver(){
        gen.writeEnd().writeEnd();
        gen.close();
        return true;
    }
}