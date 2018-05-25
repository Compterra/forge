package forge.deck.io;

import forge.game.GameFormat;
import forge.item.PaperCard;
import forge.properties.ForgeConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maustin on 11/05/2017.
 */
public class CardThemedLDAIO {

    /** suffix for all gauntlet data files */
    public static final String SUFFIX_DATA = ".lda.dat";
    public static final String RAW_SUFFIX_DATA = ".raw.dat";

    public static void saveRawLDA(String format, List<List<Pair<String, Double>>> lda){
        File file = getRAWLDAFile(format);
        ObjectOutputStream s = null;
        try {
            FileOutputStream f = new FileOutputStream(file);
            s = new ObjectOutputStream(f);
            s.writeObject(lda);
            s.close();
        } catch (IOException e) {
            System.out.println("Error writing matrix data: " + e);
        } finally {
            if(s!=null) {
                try {
                    s.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<List<Pair<String, Double>>> loadRawLDA(String format){
        try {
            FileInputStream fin = new FileInputStream(getRAWLDAFile(format));
            ObjectInputStream s = new ObjectInputStream(fin);
            List<List<Pair<String, Double>>> matrix = (List<List<Pair<String, Double>>>) s.readObject();
            s.close();
            return matrix;
        }catch (Exception e){
            System.out.println("Error reading LDA data: " + e);
            return null;
        }

    }

    public static void saveLDA(String format, Map<String,List<List<Pair<String, Double>>>> map){
        File file = getLDAFile(format);
        ObjectOutputStream s = null;
        try {
            FileOutputStream f = new FileOutputStream(file);
            s = new ObjectOutputStream(f);
            s.writeObject(map);
            s.close();
        } catch (IOException e) {
            System.out.println("Error writing matrix data: " + e);
        } finally {
            if(s!=null) {
                try {
                    s.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String,List<List<Pair<String, Double>>>> loadLDA(String format){
        try {
            FileInputStream fin = new FileInputStream(getLDAFile(format));
            ObjectInputStream s = new ObjectInputStream(fin);
            Map<String,List<List<Pair<String, Double>>>> matrix = (Map<String,List<List<Pair<String, Double>>>>) s.readObject();
            s.close();
            return matrix;
        }catch (Exception e){
            System.out.println("Error reading LDA data: " + e);
            return null;
        }

    }

    public static File getLDAFile(final String name) {
        return new File(ForgeConstants.DECK_GEN_DIR, name + SUFFIX_DATA);
    }

    public static File getRAWLDAFile(final String name) {
        return new File(ForgeConstants.DECK_GEN_DIR, name + RAW_SUFFIX_DATA);
    }

    public static File getMatrixFolder(final String name) {
        return new File(ForgeConstants.DECK_GEN_DIR, name);
    }

    public static File getLDAFile(final GameFormat gf) {
        return getLDAFile(gf.getName());
    }
}
