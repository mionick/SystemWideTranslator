import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author      Nicholas Mio <mio.clause@gmail.com>
 *
 * A simple Utility used to translate between the languages I am currently learning.
 * Just more convenient than using Google translate all the time.
 *
 * Usage: copy the text you wish to translate so that it is in the system clip board (Just highlight and press ctrl+c).
 *        Then press alt+n to get the translation in the currently selected language pairs.
 *
 *        You can change the base language and the languages to translate to by right clicking the icon in the system tray.
 *
 *        To make the translation box disappear press alt+m.
 *
 *        Languages can be easly added by appending them to the two lists in the source code.
 *        The array acceptedLanguages[] accepts language codes as described by yandex here:
 *        https://tech.yandex.com/translate/doc/dg/concepts/api-overview-docpage/#languages
 *
 *        the array acceptedLanguagesFullname[] lists the text that will show up in the menu in the system tray.
 *
 * First Time Setup:
 *        The application expects that you will get your own Yandex API key (I can't use mine for all instances of the application)
 *        And that you will put it in an ASCII encoded file called YKey.txt
 *
 *        This file must be located in the same folder as the jar file.
 *
 * Created by Nicholas on 2016-06-04.
 *
 * Copyright Nicholas Mio 2016
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class MainFrame extends JWindow implements HotkeyListener {

    public static void main(String[] args) {

        //This code allows the jar to launch itself with the necessary command line options.
        //This is otherwise inpossible with java.
        if (args.length == 0) {
            try {
                // re-launch the app itself with VM option passed
                Runtime.getRuntime().exec(new String[] {"javaw", "-Dfile.encoding=UTF8", "-jar", "SystemWideTranslator.jar", "test"});
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.exit(0);
        }

        //Setting Display characteristics
        Font lblFont = new Font("Helvetica", 0, 24);

        UIManager.put("Label.font", lblFont /* font of your liking */);
        UIManager.put("Label.foreground", new Color(255, 255, 255) /* font of your liking */);
        UIManager.put("Label.opaque", false);
        UIManager.put("Label.background", new Color(0,0,0,0));

        //Using JIntellitype Library to implement System wide keyboard shortcuts.
        JIntellitype.getInstance();
        if (JIntellitype.checkInstanceAlreadyRunning("NICKTRANSLATOR")) {
            System.out.println("An instance of this application is already running"); System.exit(1); }

        JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT, (int)'N');
        JIntellitype.getInstance().registerHotKey(2, JIntellitype.MOD_ALT, (int)'M');

        //Creating the GUI components.
        MainFrame mainFrame = new MainFrame();
        mainFrame.addToSystemTray();
        //System.exit(0);
    }

    //Fields====================================================================================
    //This is where the languages can be added.
    final String[] acceptedLanguages = {"en", "ru", "ko", "fr"};//, "ja"};
    final String[] acceptedLanguagesFullName = {"English", "Russian", "Korean", "French"};//, "Japanese"};
    final boolean[] currentLangs;
    String apiKey;
    int baseLangIndex = 0;

    JPanel contentPane;


    //Methods===================================================================================
    MainFrame() {
        super();

        //Loading the api key from the file in the working directory.
        //Create the file if it is not found.
        byte[] encoded;
        File keyFile = new File(System.getProperty("user.dir") + File.separator + "YKey.txt");
        if (!keyFile.exists()) {
            try {
                keyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            encoded = Files.readAllBytes(Paths.get(keyFile.getAbsolutePath()));
            apiKey = new String(encoded, "ASCII");
        } catch (IOException e) {
            System.out.println("You are missing the key file! Please palce your yandex api key in the file created.");

            e.printStackTrace();
        }
        //New line characters ruin urls.
        apiKey = apiKey.replace("\r\r\n", "");
        apiKey = apiKey.replace("\n", "");

        //assign this class to be a HotKeyListener
        JIntellitype.getInstance().addHotKeyListener(this);

        currentLangs = new boolean[acceptedLanguages.length];
        for (int i = 0; i < acceptedLanguages.length; i++)
            currentLangs[i] = true;

        //setUndecorated(true);
        //setOpacity(f);
        setBackground(new Color(0, 0, 0, 150));
        setAlwaysOnTop(true);

        contentPane = new JPanel(){
            protected void paintComponent(Graphics g)
            {
                g.setColor( getBackground() );
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        setContentPane(contentPane);
        contentPane.setBackground(new Color(40, 40, 40, 40));
        contentPane.setOpaque(false);

        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                JIntellitype.getInstance().cleanUp();
                System.exit(0);
            }
        });
    }

    void getAllTranslations(String theText) {
        //This method will clear the current window, retrieve all new translations, and populate the window.
        contentPane.removeAll();
        for (int i = 0; i < acceptedLanguages.length; i++) {
            if (i != baseLangIndex && currentLangs[i]) {
                /*
                Here need to parse xml
                and add to the panel.
                 */
                contentPane.add(new JLabel(acceptedLanguagesFullName[i]));
                contentPane.add(new JLabel(getTranslation(theText, i)));
                contentPane.add(new JSeparator(SwingConstants.HORIZONTAL));
            }
        }



        pack();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - getWidth() - 25;
        int y = (int) rect.getMaxY() - getHeight() - 50;
        setLocation(x, y);
        setVisible(true);

    }

    String getTranslation(String theText, int toLangIndex) {
        //This will get a single language pair translation and return the translated string.
        final String[] result = {"No Translation"};

        try {

            String url = "https://translate.yandex.net/api/v1.5/tr/translate?" +
                    "key=" + apiKey +
                    "&text=" + theText +
                    "&lang=" + acceptedLanguages[baseLangIndex] + "-" + acceptedLanguages[toLangIndex] ;
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            // the SAX way:
            XMLReader myReader = XMLReaderFactory.createXMLReader();
            myReader.setContentHandler(new DefaultHandler(){

                boolean needsToAddTranslation;

                @Override
                public void startElement (String uri, String localName,
                                          String qName, Attributes attributes) {
                    if (localName.equals("text")) {
                        needsToAddTranslation = true;
                    }
                }

                @Override
                public void characters (char ch[], int start, int length)
                        throws SAXException
                {
                    if (needsToAddTranslation) {
                        result[0] = new String(ch, start, length);
                    }
                }

                @Override
                public void endElement (String uri, String localName, String qName)
                        throws SAXException
                {
                    needsToAddTranslation = false;
                }
            });
            myReader.parse(new InputSource(new URL(url).openStream()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result[0];

    }

    // listen for hotkey
    public void onHotKey(int aIdentifier) {
        if (aIdentifier == 1) {
            getAllTranslations(getHighlightedText());
        } else if (aIdentifier == 2) {
            setVisible(false);
        }
    }

    public String getHighlightedText() {
        //Retrieves the text that is currently in the system clipboard.
        String result = null;
        try {
            //result = new String(((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)).getBytes(), "UTF8");
            result = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            result = result.replace("\n", " ").replace("\r", " ");
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    void addToSystemTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final JPopupMenu popup = new JPopupMenu();
        final TrayIcon trayIcon;
        try {
            trayIcon = new TrayIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.setLocation(e.getX(), e.getY());
                    popup.setInvoker(popup);
                    popup.setVisible(true);
                }
            }
        });

        // Create a pop-up menu components
        for (int i = 0; i < acceptedLanguages.length; i++) {
            JCheckBoxMenuItem curCheckBoxItem = new JCheckBoxMenuItem(acceptedLanguagesFullName[i], currentLangs[i]);
            final int finalI = i;
            curCheckBoxItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentLangs[finalI] = curCheckBoxItem.getState();
                }
            });

            popup.add(curCheckBoxItem);
        }
        popup.addSeparator();

        JMenu baseLangMenu = new JMenu("Base Language");

        popup.add(baseLangMenu);
        // Create a pop-up menu components
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < acceptedLanguages.length; i++) {
            JRadioButtonMenuItem curRadioItem = new JRadioButtonMenuItem(acceptedLanguagesFullName[i]);
            final int finalI = i;
            curRadioItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    baseLangIndex = finalI;
                }
            });
            if (i == baseLangIndex) {
                curRadioItem.setSelected(true);
            }

            baseLangMenu.add(curRadioItem);
            bg.add(curRadioItem);
        }
        popup.addSeparator();
        //Add components to pop-up menu
        JMenuItem showLastIem = new JMenuItem("Show Last");
        JMenuItem exitItem = new JMenuItem("Exit");
        showLastIem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JIntellitype.getInstance().cleanUp();
                System.exit(0);
            }
        });
        popup.add(showLastIem);
        popup.addSeparator();

        popup.add(exitItem);

        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }






}
