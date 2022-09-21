package za.co.ominsure.synapse.content.scribe.backend.autocomms.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class TIAKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        return AutoCommsUtil.gson.toJson(key);
        //return new Genson().deserialize(key, TIASearchIDs.class);
    }

}
