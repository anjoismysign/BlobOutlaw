package io.github.anjoismysign.bloboutlaw.law;

import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableSnippet;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface Law {
    int getMaxStars();

    enum BountyClaim {
        DEAD,
        ALIVE
    }

    enum Status {
        NONE("BlobOutlaw.Status-None", 0),
        PROTECTED("BlobOutlaw.Status-Protected", 1),
        KILLER("BlobOutlaw.Status-Killer", 2),
        MENACE("BlobOutlaw.Status-Menace", 3);

        private final @NotNull String snippetKey;
        private final int ordinal;

        private static final Map<Integer, Status> ORDINAL_MAP;

        static {
            Map<Integer, Status> map = new HashMap<>();
            for (Status status : values()) {
                map.put(status.ordinal, status);
            }
            ORDINAL_MAP = Collections.unmodifiableMap(map);
        }

        Status(@NotNull String snippetKey,
               int ordinal) {
            this.snippetKey = snippetKey;
            this.ordinal = ordinal;
        }

        public int getOrdinal(){
            return ordinal;
        }

        @NotNull
        public String title(@NotNull Player player) {
            TranslatableSnippet snippet = Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatableSnippet(snippetKey, player),
                    "'" + snippetKey + "' TranslatableSnippet does not exist!");
            return snippet.get();
        }

        public static Status ofOrdinal(int ordinal) {
            Status status = ORDINAL_MAP.get(ordinal);
            if (status == null) {
                throw new IllegalArgumentException("No Status with ordinal: " + ordinal);
            }
            return status;
        }
    }

    enum Role {
        SHERIFF,
        BARKEEP,
        DOCTOR,
        HITMAN,
        MAYOR
    }

    enum Crime implements io.github.anjoismysign.outlaw.Crime {
        BORN_EVIL("Spawn as an Outlaw"),
        COMPLICITY("Attempt to free an Outlaw who is being arrested"),
        ASSAULT("Harm a Citizen or Lawman who isn't a Danger while you are hostile"),
        MURDER("Kill a Citizen or Lawman outside of a duel."),
        ANIMAL_CRUELTY("Kill a Citizen or Lawman's Horse."),
        HITMAN("Accepting a contract to kill a lawful civilian or the Mayor will automatically charge you with \"Hitman\" and you will gain a $70 bounty, regardless of completion. The bounty will not apply if hunting a player that took on a civilian job, such as the Sheriff, a Barkeep, or a Doctor."),
        THEFT("Any kind of theft without involving violence"),
        HEIST("Any kind of theft while involving violence"),
        MENACE("Engage in indiscriminate killing sprees, targeting multiple citizens or lawmen without provocation.");

        private final String description;

        Crime(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public @NotNull String getName() {
            return name();
        }

        public long getTerm() {
            CrimeData crimeData = getData(this);
            return crimeData.term;
        }

        public double getWarrant(){
            CrimeData crimeData = getData(this);
            return crimeData.warrant;
        }

        record CrimeData(double warrant,
                         long term,
                         boolean isDirty){

            static void save(@NotNull Law.Crime crime,
                                  @NotNull ConfigurationSection configuration){
                String path = crime.name();
                String warrantPath = path+".Warrant";
                String termPath = path+".Term";

                BigDecimal warrant = BigDecimal.valueOf(crime.getWarrant());
                long term = crime.getTerm();

                configuration.set(warrantPath, warrant);
                configuration.set(termPath, term);
            }

            static CrimeData read(@NotNull Law.Crime crime,
                                  @NotNull ConfigurationSection configuration){
                boolean isDirty = false;
                String path = crime.name();
                String warrantPath = path+".Warrant";
                String termPath = path+".Term";
                double warrant;
                if (configuration.isDouble(warrantPath)) {
                    warrant = configuration.getDouble(warrantPath);
                } else {
                    warrant = 10.0d;
                    configuration.set(warrantPath, BigDecimal.valueOf(warrant));
                    isDirty = true;
                }
                long term;
                if (configuration.isLong(termPath)) {
                    term = configuration.getLong(termPath);
                } else {
                    term = 5L;
                    configuration.set(termPath, BigDecimal.valueOf(warrant));
                    isDirty = true;
                }
                return new CrimeData(warrant, term, isDirty);
            }
        }

        private static CrimeData getData(@NotNull Law.Crime crime){
            if (data.isEmpty())
                readData();
            return data.get(crime);
        }

        private static final Map<Crime, CrimeData> data = new HashMap<>();
        private static final File file = new File(BlobOutlaw.getInstance().getDataFolder(), "crimes.yml");

        private static File getFile(){
            if (!file.isFile()){
                try {
                    file.createNewFile();
                } catch ( IOException exception ) {
                    throw new RuntimeException(exception);
                }
            }
            return file;
        }

        public static void readData(){
            data.clear();
            File file = getFile();
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            boolean isDirty = false;
            for (Crime crime : Crime.values()){
                CrimeData data = CrimeData.read(crime,configuration);
                Crime.data.put(crime, data);
                if (data.isDirty)
                    isDirty = true;
            }
            if (!isDirty)
                return;
            try {
                configuration.save(file);
            } catch ( IOException exception ) {
                throw new RuntimeException(exception);
            }
        }

        public static void saveData(){
            File file = getFile();
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            data.forEach((crime, data)->{
                CrimeData.save(crime, configuration);
            });
            try {
                configuration.save(file);
            } catch ( IOException exception ) {
                throw new RuntimeException(exception);
            }
        }
    }
}
