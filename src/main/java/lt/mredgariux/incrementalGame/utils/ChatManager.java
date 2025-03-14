package lt.mredgariux.incrementalGame.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {
    /**
     * Dekoduoja žinutę su senesniais „legacy“ formatais (& simboliais)
     * ir paverčia į Component spalvas.
     *
     * @param message Žinutė su "&" spalvų kodais.
     * @return Adventure Component
     */
    public static Component decodeLegacyMessage(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Dekoduoja žinutę su RGB spalvų kodais ({#...}) ir legacy (&) spalvų kodais.
     * @param message Žinutė su RGB ir "&" spalvų kodais.
     * @return Adventure Component
     */
    public static Component decodeMessage(String message) {
        // Pakeičiame RGB spalvų kodus į temporary formatą, kad juos galėtume apdoroti
        message = message.replaceAll("\\{#([0-9a-fA-F]{6})\\}", "%%$1%%");  // RGB kodai {#123456}

        // Dekoduojame senus & spalvų kodus
        Component decodedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);

        // Pakeičiame RGB spalvas į Component pagal TextColor
        String decodedString = decodedMessage.toString();

        // Sukuriame pattern ir matcher RGB kodų ieškojimui
        Pattern pattern = Pattern.compile("%%([0-9a-fA-F]{6})%%");
        Matcher matcher = pattern.matcher(decodedString);

        // Apdorojame visus RGB kodus
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String colorCode = matcher.group(1);
            TextColor textColor = TextColor.color(Integer.parseInt(colorCode.substring(0, 2), 16),
                    Integer.parseInt(colorCode.substring(2, 4), 16),
                    Integer.parseInt(colorCode.substring(4, 6), 16));
            matcher.appendReplacement(result, textColor.toString());  // Pakeičiame su RGB komponentu
        }
        matcher.appendTail(result);  // Pridedame likusią dalį

        // Grąžiname sujungtą Component su RGB spalvomis
        return LegacyComponentSerializer.legacyAmpersand().deserialize(result.toString());
    }

    /**
     * Siunčia žinutę su prefiksu (pridedant PREFIX su spalvomis).
     *
     * @param player  Žaidėjas, kuriam siunčiame žinutę.
     * @param message Žinutė su "&" spalvų kodais.
     */
    public static void sendMessageNoPrefix(Player player, String message) {
        Component finalMessage = decodeLegacyMessage(message);
        player.sendMessage(finalMessage);
    }

    public static void sendMessage(Player player, String message) {
        sendMessage(player, message, "&2Incremental &6>"); // Numatytoji reikšmė
    }

    /**
     * Siunčia tik žinutę (be prefikso).
     *
     * @param player  Žaidėjas, kuriam siunčiame žinutę.
     * @param message Žinutė su "&" spalvų kodais.
     */
    public static void sendMessage(Player player, String message, String prefix) {
        Component finalMessage;
        if (prefix != null) {
            finalMessage = decodeLegacyMessage( prefix + " ").append(decodeLegacyMessage(message));
        } else {
            finalMessage = decodeLegacyMessage("&2Incremental &6> ").append(decodeLegacyMessage(message));
        }
        player.sendMessage(finalMessage);
    }

    /**
     * Siunčia RGB spalvotą žinutę Component API pagalba.
     *
     * @param player  Žaidėjas
     * @param message Žinutė su savo tekstu
     * @param rgbColor RGB spalvos kodas (pvz., `TextColor.color(255, 0, 0)`).
     */
    public static void sendMessageWithRGB(Player player, String message, TextColor rgbColor) {
        Component finalMessage = Component.text(message, rgbColor);
        player.sendMessage(finalMessage);
    }

    /**
     * Siunčia žinutę su formatavimu ir pridėtu prefiksu.
     *
     * @param player  Žaidėjas, kuriam siunčiame žinutę.
     * @param messageFormat Žinutės formatas (pvz., "Sveikas, %s! Tavo balansas: %s").
     * @param params  Parametrai, kurie įterps į formatuotą žinutę.
     */
    public static void sendFormatMessage(Player player, String messageFormat, Object... params) {
        // Suformatuojame žinutę naudojant String.format
        String formattedMessage = String.format(messageFormat, params);
        // Pridedame prefix ir siunčiame su dekodavimu
        Component finalMessage = decodeLegacyMessage("&2Incremental &6> " + formattedMessage);
        player.sendMessage(finalMessage);
    }

    /**
     * Siunčia žinutę visam serveriui (broadcast) su prefiksu.
     *
     * @param message Žinutė su "&" spalvų kodais.
     */
    public static void sendBroadcast(String message) {
        Component finalMessage = decodeLegacyMessage("&2Incremental &6> ").append(decodeLegacyMessage(message));
        Bukkit.getServer().sendMessage(finalMessage);
    }

    /**
     * Siunčia formatuotą žinutę visam serveriui su prefiksu.
     *
     * @param messageFormat Žinutės formatas (pvz., "Sveikiname! %s prisijungė prie serverio.").
     * @param params        Parametrai, kurie pakeis formatuojamą žinutę.
     */
    public static void sendFormatBroadcast(String messageFormat, Object... params) {
        // Formatuojame žinutę naudojant String.format
        String formattedMessage = String.format(messageFormat, params);
        // Pridedame prefiksą ir dekoduojame
        Component finalMessage = decodeLegacyMessage("&2Incremental &6> " + formattedMessage);
        // Siunčiame visam serveriui
        Bukkit.getServer().sendMessage(finalMessage);
    }

    /**
     * Siunčia žinutę visam serveriui (broadcast) be prefikso.
     *
     * @param message Žinutė su "&" spalvų kodais.
     */
    public static void sendBroadcastNoPrefix(String message) {
        Component finalMessage = decodeLegacyMessage(message);
        Bukkit.getServer().sendMessage(finalMessage);
    }

    /**
     * Siunčia action bara žaidėjui xD
     *
     * @param player  Žaidėjas, kuriam siunčiame žinutę.
     * @param message Žinutė su "&" spalvų kodais.
     */
    public static void actionBar(Player player, String message) {
        Component finalMessage = decodeLegacyMessage(message);
        player.sendActionBar(finalMessage);
    }
}
