package main.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBacked(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }

    public static Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(Duration.class, new DurationTypeAdapter()).registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }

    public static class DurationTypeAdapter extends TypeAdapter<Duration> {


        @Override
        public void write(JsonWriter writer, Duration duration) throws IOException {
            if (duration == null) {
                duration = Duration.ZERO;
            } else {
                writer.value(duration.toString());
            }
        }

        @Override
        public Duration read(JsonReader reader) throws IOException {
            if (reader.peek() == null) {
                return Duration.ZERO;
            }
            return Duration.parse(reader.nextString());
        }
    }

    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter writer, LocalDateTime value) throws IOException {
            if (value == null) {
                writer.nullValue();
            } else {
                writer.value(formatter.format(value)); // "2023-01-01T12:00:00"
            }
        }

        @Override
        public LocalDateTime read(JsonReader reader) throws IOException {
            return LocalDateTime.parse(reader.nextString(), formatter);
        }
    }


}
