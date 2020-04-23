package jmri.jmrit.ampmeter;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.MultiMeter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

/**
 * Frame providing a simple lcd-based display of track current.
 * <p>
 * A Run/Stop button is built into this, but because I don't like the way it
 * looks, it's not currently displayed in the GUI.
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 *
 * This was a direct steal form the LCDClock code by Ken Cameron, which was a
 * direct steal from the Nixie clock code, ver 1.2. Thank you Bob Jacobsen and
 * Ken Cameron.
 */
public class AmpMeterFrame extends JmriJFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    ArrayList<JLabel> digitIcons;
    JLabel percent;
    JLabel decimal;
    JLabel milliAmp;
    JLabel amp;

    private int startWidth;
    private int startHeight;

    MultiMeter meter;

    NamedIcon digits[] = new NamedIcon[10];
    NamedIcon percentIcon;
    NamedIcon decimalIcon;
    NamedIcon milliAmpIcon;
    NamedIcon ampIcon;

    public AmpMeterFrame() {
        super(Bundle.getMessage("TrackCurrentMeterTitle"));

        meter = InstanceManager.getDefault(MultiMeter.class);
    }

    @Override
    public void initComponents() {
        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            digits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        percentIcon = new NamedIcon("resources/icons/misc/LCD/percentb.gif", "resources/icons/misc/LCD/percentb.gif");
        decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalb.gif", "resources/icons/misc/LCD/decimalb.gif");
        milliAmpIcon = new NamedIcon("resources/icons/misc/LCD/milliampb.gif", "resources/icons/misc/LCD/milliampb.gif");
        ampIcon = new NamedIcon("resources/icons/misc/LCD/ampb.gif", "resources/icons/misc/LCD/ampb.gif");

        // listen for changes to the meter parameters
        meter.addPropertyChangeListener(this);

        // init GUI
        digitIcons = new ArrayList<JLabel>(3); // 1 decimal place precision.
        for(int i = 0;i<3;i++) {
           digitIcons.add(i,new JLabel(digits[0]));
        }
        percent = new JLabel(percentIcon);
        decimal = new JLabel(decimalIcon);
        milliAmp = new JLabel(milliAmpIcon);
        amp = new JLabel(ampIcon);

        buildContents();

        // Initially we want to scale the icons to fit the previously saved window size
        startHeight = digits[0].getIconHeight();
        startWidth = (int)(digits[0].getIconWidth() * 4.5);
        scaleImage();
        buildContents();
        
        meter.enable();

        update();

        // request callback to update time
        // Again, adding updates.
        java.beans.PropertyChangeListener du_listener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        };
        meter.addPropertyChangeListener(MultiMeter.CURRENT, du_listener);

        // Add component listener to handle frame resizing event
        this.addComponentListener(
                new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleImage();
            }
        });

    }

    // Added method to scale the clock digit images to fit the
    // size of the display window
    synchronized public void scaleImage() {
        int frameHeight = this.getContentPane().getSize().height;
        int frameWidth = this.getContentPane().getSize().width;

        double hscale = ((double)frameHeight)/((double)startHeight);
        double wscale = ((double)frameWidth)/((double)startWidth);
        double scale = hscale < wscale? hscale:wscale;

        for (int i = 0; i < 10; i++) {
            digits[i].scale(scale,this);
        }
        percentIcon.scale(scale,this);
        decimalIcon.scale(scale,this);
        ampIcon.scale(scale, this);
        milliAmpIcon.scale(scale,this);
    }

    private void buildContents(){
        // clear the contents
        getContentPane().removeAll();

        // build the actual multimeter display.
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        for(int i=0;i<digitIcons.size()-1;i++){
            getContentPane().add(digitIcons.get(i));
        }
        switch (meter.getCurrentUnits()) {
            case CURRENT_UNITS_MILLIAMPS:
                getContentPane().add(milliAmp);
                break;
            case CURRENT_UNITS_AMPS:
                getContentPane().add(decimal);
                getContentPane().add(digitIcons.get(digitIcons.size()-1));
                getContentPane().add(amp);
                break;
            case CURRENT_UNITS_PERCENTAGE:
            default:
                getContentPane().add(decimal);
                getContentPane().add(digitIcons.get(digitIcons.size()-1));
                getContentPane().add(percent);
                break;
        }

        pack();
    }

    synchronized void update() {
        float val = meter.getCurrent();
        int value = (int)Math.floor(val *10); // keep one decimal place.
        boolean scaleChanged = false;
        // autoscale the array of labels.
        while( (value) > (Math.pow(10,digitIcons.size()-1))) {
           digitIcons.add(0,new JLabel(digits[0]));
           scaleChanged = true;
        }

        if (scaleChanged){
            // clear the content pane and rebuild it.
            buildContents();
        }

        value = (int)Math.floor(val *10); // keep one decimal place.
        for(int i = digitIcons.size()-1; i>=0; i--){
            digitIcons.get(i).setIcon(digits[value%10]);
            value = value / 10;
        }
    }

    @Override
    public void dispose() {
        meter.disable();
        meter.removePropertyChangeListener(this);
        super.dispose();
    }

    /**
     * Handle a change to clock properties
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        /*
        boolean now = clock.getRun();
        if (now) {
            b.setText("Stop");
        } else {
            b.setText("Run");
        }
         */
    }

    private final static Logger log = LoggerFactory.getLogger(AmpMeterFrame.class);

}
