package remindme.Json.Adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import remindme.Entities.TimeInterval;

public class TimeIntervalAdapter extends TypeAdapter<TimeInterval> {

    @Override
    public void write(JsonWriter out, TimeInterval value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public TimeInterval read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String value = in.nextString();
        return TimeInterval.getTimeIntervalFromString(value);
    }
}
