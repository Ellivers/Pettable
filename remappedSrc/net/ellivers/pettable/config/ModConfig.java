package net.ellivers.pettable.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.io.*;

import static net.ellivers.pettable.Pettable.MOD_ID;

@Config(name = MOD_ID)
public class ModConfig implements ConfigData {

    public static boolean heal_owner = true;

    public static Screen init(Screen parent) {
        ConfigBuilder builder =  ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("title.pettable.config"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.pettable.general"));

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.pettable.heal_owner"), heal_owner)
                .setDefaultValue(heal_owner)
                .setTooltip(new TranslatableText("option.pettable.heal_owner.description"))
                .setSaveConsumer(newValue -> heal_owner = newValue)
                .build());

        builder.setSavingRunnable(ModConfig::save);

        return builder.build();
    }

    public static void save() {
        FileWriter fileWriter;
        PrintWriter printWriter;
        try {
            fileWriter = new FileWriter("config/pettable.cfg");
            printWriter = new PrintWriter(fileWriter);
            printWriter.printf("heal_owner %b\n", heal_owner);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() throws IOException {
        FileReader fileReader;
        BufferedReader bufferedReader;
        try {
            fileReader = new FileReader("config/pettable.cfg");
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("heal_owner ")) {
                    heal_owner = Boolean.parseBoolean(line.split("heal_owner ")[1]);
                }
            }
        } catch (FileNotFoundException ignored) {}
    }
}
