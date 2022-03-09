package zezombye.bide;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class Picture extends JPanel {

    int type, size;
    int zoom;
    String name;
    JScrollPane jsp = new JScrollPane(this);
    JPanel namePanel = new JPanel();
    JTextField nameJtf;
    JPanel sizePanel = new JPanel();
    JTextField sizeJtf;
    PictPanel pictPanel = new PictPanel(0);
    PictPanel pictPanel2 = new PictPanel(1);
    JButton setSizeButton = new JButton("Set size");
    JButton exportPictButton = new JButton("Export to .png");
    JLabel pictTutorial = new JLabel("Left click for black, right click for white, ctrl+scroll to zoom");
    JLabel pictWarning = new JLabel("Do not edit the picture below unless you know what you are doing!");

    public Picture(int type, String name, int size, byte[] data) {
        this(type, name, size);

        pictPanel.pixels = Arrays.copyOfRange(data, 0, 0x400);
        if (size > 0x400 && data.length > 0x400) {
            pictPanel2.pixels = Arrays.copyOfRange(data, 0x400, size);
        }
    }

    public Picture(int type, String name, int size) {
        this.setLayout(null); // Layouts are pure evil, absolute is at least consistent

        this.add(namePanel);
        namePanel.add(new JLabel("Name:"));
        nameJtf = new JTextField(name);
        nameJtf.setPreferredSize(new Dimension(50, 20));
        namePanel.add(nameJtf);
        this.name = name;

        this.setPreferredSize(new Dimension(1000, 1000));

        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jsp.getHorizontalScrollBar().setUnitIncrement(16);
        jsp.getVerticalScrollBar().setUnitIncrement(16);
        jsp.setBorder(BorderFactory.createEmptyBorder());

        this.add(pictTutorial);

        this.add(pictPanel);
        this.add(pictWarning);
        this.add(pictPanel2);
        this.add(sizePanel);
        sizePanel.add(new JLabel("Size (hex):"));
        sizeJtf = new JTextField(Integer.toHexString(size));
        sizeJtf.setPreferredSize(new Dimension(25, 20));
        sizePanel.add(sizeJtf);
        this.add(setSizeButton);
        this.add(exportPictButton);

        if (type == BIDE.TYPE_CAPT) {
            sizePanel.setVisible(false);
            setSizeButton.setVisible(false);
        }

        this.addMouseWheelListener(event -> {
            event.consume();
            if (event.isControlDown()) {
                if (event.getWheelRotation() < 0) {
                    setZoom(zoom + 1);
                } else if (zoom > 2){
                    setZoom(zoom - 1);
                }
                jsp.paintComponents(jsp.getGraphics());
            } else {
                getParent().dispatchEvent(event);
            }

        });

        setSizeButton.addActionListener(event -> {
            try {
                setPictSize(Integer.parseInt(sizeJtf.getText(), 16));
            } catch (Exception e) {
                BIDE.error("Invalid size!");
            }
        });

        exportPictButton.addActionListener(event -> exportPict());

        positionComponents();
        setZoom(Integer.parseInt(BIDE.options.getProperty("pictZoom")));
        setPictSize(size);
        this.type = type;
    }

    public void exportPict() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                try {
                    String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
                    if (extension.equals(".png")) {
                        return true;
                    }
                } catch (Exception ignored) {}
                return false;
            }

            @Override
            public String getDescription() {
                return "PNG files (.png)";
            }
        });

        jfc.setSelectedFile(new File(this.name+".png"));
        File input = null;
        if (jfc.showSaveDialog(BIDE.ui.window) == JFileChooser.APPROVE_OPTION) {
            input = jfc.getSelectedFile();
        }

        if (input != null) {
            try {
                BufferedImage image = new BufferedImage(128, (this.size+15)/16, BufferedImage.TYPE_INT_RGB);

                if (this.size <= 0x400) {
                    for (int i = 0; i < this.size*8; i++) {
                        if ((pictPanel.pixels[i/8] & (0b10000000 >> i%8)) == 0) {
                            image.setRGB(i%128, i/128, Color.WHITE.getRGB());
                        }
                    }
                } else {
                    for (int i = 0; i < 0x400*8; i++) {
                        if ((pictPanel.pixels[i/8] & (0b10000000 >> i%8)) == 0) {
                            image.setRGB(i%128, i/128, Color.WHITE.getRGB());
                        }
                    }
                    for (int i = 0; i < (this.size-0x400)*8; i++) {
                        if ((pictPanel2.pixels[i/8] & (0b10000000 >> i%8)) == 0) {
                            image.setRGB(i%128, i/128+64, Color.WHITE.getRGB());
                        }
                    }
                }

                ImageIO.write(image, "png", input);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        pictPanel.setZoom(zoom);
        pictPanel2.setZoom(zoom);
        if (size > 0x400) {
            this.setPreferredSize(new Dimension(pictPanel2.getX()+pictPanel2.getWidth(), pictPanel2.getY()+pictPanel2.getHeight()));
        } else {
            this.setPreferredSize(new Dimension(pictPanel.getX()+pictPanel.getWidth(), pictPanel.getY()+pictPanel.getHeight()));

        }
    }

    public void setPictSize(int size) {

        if (size%4 != 0) {
            BIDE.error("Size must be a multiple of 4");
        } else if (size < 4 || size > 0x800) {
            BIDE.error("Size must be between 0x4 and 0x800");
        } else {
            this.size = size;
            pictPanel.pictSize = Math.min(size, 0x400);
            pictPanel2.pictSize = size <= 0x400 ? 0 : size-0x400;
            pictPanel.repaint();
            pictPanel2.repaint();
            if (size <= 0x400) {
                pictPanel2.setVisible(false);
                pictWarning.setVisible(false);
            } else {
                pictPanel2.setVisible(true);
                pictWarning.setVisible(true);
            }

            if (size > 0x400) {
                this.setPreferredSize(new Dimension(pictPanel2.getX()+pictPanel2.getWidth(), pictPanel2.getY()+pictPanel2.getHeight()));
            } else {
                this.setPreferredSize(new Dimension(pictPanel.getX()+pictPanel.getWidth(), pictPanel.getY()+pictPanel.getHeight()));

            }
        }

    }

    public void positionComponents() {
        namePanel.setBounds(1, 1, namePanel.getPreferredSize().width, namePanel.getPreferredSize().height);
        sizePanel.setBounds(namePanel.getX()+100, namePanel.getY(), sizePanel.getPreferredSize().width, sizePanel.getPreferredSize().height);
        setSizeButton.setBounds(sizePanel.getX()+100, sizePanel.getY()+3, setSizeButton.getPreferredSize().width, setSizeButton.getPreferredSize().height);
        exportPictButton.setBounds(setSizeButton.getX()+setSizeButton.getWidth()+10, sizePanel.getY()+3, exportPictButton.getPreferredSize().width, exportPictButton.getPreferredSize().height);
        pictTutorial.setBounds(namePanel.getX()+5, namePanel.getY()+34, pictTutorial.getPreferredSize().width, pictTutorial.getPreferredSize().height);
        pictPanel.setBounds(pictTutorial.getX(), pictTutorial.getY()+16, pictPanel.getPreferredSize().width, pictPanel.getPreferredSize().height);
        pictWarning.setBounds(pictTutorial.getX(), pictPanel.getY()+pictPanel.getHeight()+12, pictWarning.getPreferredSize().width, pictWarning.getPreferredSize().height);
        pictPanel2.setBounds(pictTutorial.getX(), pictWarning.getY()+16, pictPanel2.getPreferredSize().width, pictPanel2.getPreferredSize().height);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        positionComponents();
    }

}


class PictPanel extends JPanel {


    int zoom = 6;
    int id;
    Dimension size = new Dimension(128*zoom+1, 64*zoom+1);
    int pictSize;
    byte[] pixels = new byte[16*64];

    int xClick=-1, yClick=-1;

    public PictPanel(int id) {
        super();
        Arrays.fill(pixels, (byte)0);
        this.id = id;
        setZoom(6);
        this.setBackground(new Color(0xFFFFFF));

        this.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent event) {}
            @Override public void mouseEntered(MouseEvent event) {}
            @Override public void mouseExited(MouseEvent event) {}
            @Override public void mousePressed(MouseEvent event) {
                xClick = event.getX()/zoom;
                yClick = event.getY()/zoom;
                if (xClick < 0 || xClick > 127 || yClick < 0 || yClick > 63) return;
                if (SwingUtilities.isLeftMouseButton(event)) {
                    setPixel(xClick, yClick, 1);
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    setPixel(xClick, yClick, 0);
                }

                repaint();
            }
            @Override public void mouseReleased(MouseEvent event) {}
        });

        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent event) {
                xClick = event.getX()/zoom;
                yClick = event.getY()/zoom;
                if (xClick < 0 || xClick > 127 || yClick < 0 || yClick > 63) return;
                if (SwingUtilities.isLeftMouseButton(event)) {
                    setPixel(xClick, yClick, 1);
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    setPixel(xClick, yClick, 0);
                }

                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
            }

        });

    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        size = new Dimension(128*zoom+2, 64*zoom+2);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        this.setSize(size);
    }

    public void setPixel(int x, int y, int color) {
        if (color == 1) {
            pixels[x/8+16*y] = (byte)(pixels[x/8+16*y] | (0b10000000 >> (x%8)));
        } else {
            pixels[x/8+16*y] = (byte)(pixels[x/8+16*y] & ~(0b10000000 >> (x%8)));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 64; j++) {
                if (i + 128*j >= pictSize*8) {
                    g.setColor(Color.GRAY);
                } else if ((pixels[i/8+16*j] & (0b10000000 >> i%8)) == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(i*zoom+1, j*zoom+1, zoom, zoom);
            }
        }

        // Draw borders
        g.setColor(Color.GRAY);
        g.drawLine(0, 0, 128*zoom+1, 0);
        g.drawLine(0, 0, 0, 64*zoom+1);
        g.drawLine(128*zoom+1, 0, 128*zoom+1, 64*zoom+1);
        g.drawLine(0, 64*zoom+1, 128*zoom+1, 64*zoom+1);
    }
}
