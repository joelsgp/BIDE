package zezombye.BIDE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.*;

import java.awt.*;

public class UI {
	
	Window window = new Window();
	JPanel sidebar = new JPanel();
	JTabbedPane jtp;
	JFileChooser jfc;
	JTextArea stdout = new JTextArea();
	PrintStream printStream;
	
	public void createAndDisplayUI() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		UIManager.getDefaults().put("TabbedPane.tabRunOverlay", 0);
		UIManager.getDefaults().put("TabbedPane.focus", new Color(0, 0, 0, 0));

		jtp = new JTabbedPane() {
			@Override public void addTab(String name, Component comp) {
				super.addTab(name, comp);
				this.setTabComponentAt(jtp.getTabCount()-1, new ButtonTabComponent(jtp));
			}
		};
		
		BasicTabbedPaneUI btpUi = new BasicTabbedPaneUI();
		jtp.setUI(btpUi);

		jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
		
		window = new Window();
		window.setTitle("BIDE v"+BIDE.VERSION+" by Zezombye");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(1200, 800);
		window.setLocationRelativeTo(null);
		try {
			window.setIconImage(ImageIO.read(BIDE.class.getResourceAsStream("/images/BIDEicon.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		window.add(jtp);

		// Because writing sidebar.getWidth() causes bugs...
		int sidebarWidth = 350;	
		sidebar.setPreferredSize(new Dimension(sidebarWidth, window.getHeight()));
		window.add(sidebar, BorderLayout.EAST);
		stdout.setWrapStyleWord(true);
		printStream = new PrintStream(new CustomOutputStream(stdout), false, StandardCharsets.UTF_8);
		if (!BIDE.debug) {
			System.setOut(printStream);
		}
		stdout.setBackground(Color.ORANGE);
		stdout.setCaretColor(stdout.getBackground());
		stdout.setFont(new Font("DejaVu Avec Casio", Font.PLAIN, 12));
		stdout.setLineWrap(true);
				
		JScrollPane jsp2 = new JScrollPane(stdout);
		jsp2.setPreferredSize(new Dimension(sidebarWidth, 200));
		sidebar.add(new JLabel("Console output"));
		sidebar.add(jsp2);
		
		sidebar.add(new JLabel("                                                                                                                                      "));
		sidebar.add(new JLabel("Character Picker"));
		sidebar.add(new CharPicker());
		if (!BIDE.debug) window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				try {
					String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
					if (extension.equals(".bide") || extension.matches("\\.g[123][mr]")) {
						return true;
					}
				} catch (Exception ignored) {}
				return false;
			}

			@Override
			public String getDescription() {
				return "Basic Casio files (.g1m, .g2m, .g1r, .g2r, .g3m, .bide)";
			}
		});

		JMenuBar menuBar = new JMenuBar();
		ToolbarButton open = new ToolbarButton("openFile.png", "Open file (ctrl+O)");
		open.addActionListener(event -> openFile(false));
		

		window.getRootPane().registerKeyboardAction(
				open.getActionListeners()[0],
				KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW
		);
		
		ToolbarButton save = new ToolbarButton("saveFile.png", "Save file (ctrl+S)");
		save.addActionListener(event -> saveFile(true, false, false));
		
		window.getRootPane().registerKeyboardAction(
				save.getActionListeners()[0],
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW
		);
		
		
		ToolbarButton newProg = new ToolbarButton("newProg.png", "New Basic Casio program");
		newProg.addActionListener(event -> createNewTab(BIDE.TYPE_PROG));
		
		ToolbarButton newPict = new ToolbarButton("newPict.png", "New Picture");
		newPict.addActionListener(event -> createNewTab(BIDE.TYPE_PICT));
		
		ToolbarButton newCapt = new ToolbarButton("newCapt.png", "New Capture");
		newCapt.addActionListener(event -> createNewTab(BIDE.TYPE_CAPT));
		
		ToolbarButton run = new ToolbarButton("run.png", "Run file (ctrl+R)");
		run.addActionListener(event -> saveFile(true, false, true));

		window.getRootPane().registerKeyboardAction(
				run.getActionListeners()[0],
				KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW
		);
		
		menuBar.add(open);
		menuBar.add(save);
		menuBar.add(newProg);
		menuBar.add(newPict);
		menuBar.add(newCapt);
		if (!BIDE.options.getProperty("runOn").equals("none")) {
			menuBar.add(run);
		}
		
		menuBar.setPreferredSize(new Dimension(100, 25));
		window.add(menuBar, BorderLayout.NORTH);
		
		JMenuBar menuBar2 = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menuBar2.add(fileMenu);
		JMenuItem importFile = new JMenuItem("Open Basic Casio file");
		importFile.addActionListener(e -> openFile(false));
		fileMenu.add(importFile);
		JMenuItem addToFile = new JMenuItem("Open & add to current file");
		addToFile.addActionListener(e -> openFile(true));
		fileMenu.add(addToFile);
		
		JMenuItem saveg1m = new JMenuItem("Save to g1m");
		saveg1m.addActionListener(e -> {
			try {
				if (BIDE.pathToSavedG1M.isEmpty()) {
					BIDE.pathToSavedG1M = BIDE.pathToG1M;
				}
				BIDE.pathToSavedG1M = BIDE.pathToSavedG1M.substring(0, BIDE.pathToSavedG1M.lastIndexOf("."))+".g1m";
			} catch (Exception ignored) {}
			saveFile(true, true, false);
		});
		fileMenu.add(saveg1m);
		JMenuItem saveTxt = new JMenuItem("Save to .bide file");
		saveTxt.addActionListener(event -> {
			try {
				if (BIDE.pathToSavedG1M.isEmpty()) {
					BIDE.pathToSavedG1M = BIDE.pathToG1M;
				}
				BIDE.pathToSavedG1M = BIDE.pathToSavedG1M.substring(0, BIDE.pathToSavedG1M.lastIndexOf("."))+".bide";
			} catch (Exception ignored) {}
			saveFile(false, true, false);
		});
		fileMenu.add(saveTxt);
		
		JMenu editMenu = new JMenu("Edit");
		menuBar2.add(editMenu);
		
		JMenuItem showReplaceDialog = new JMenuItem(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.replaceDialog.setVisible(true);
			}
			
		});
		((AbstractAction) showReplaceDialog.getActionListeners()[0]).putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, window.getToolkit().getMenuShortcutKeyMaskEx())
		);
		showReplaceDialog.setText("Find/Replace");
		editMenu.add(showReplaceDialog);
		
		JMenu toolsMenu = new JMenu("Tools");
		menuBar2.add(toolsMenu);
		JMenuItem multiDrawstat = new JMenuItem("Multi Drawstat Generator");
		multiDrawstat.addActionListener(event -> new MultiDrawstatGenerator());
		toolsMenu.add(multiDrawstat);
		JMenuItem imgToPict = new JMenuItem("Image to picture");
		imgToPict.addActionListener(event -> importImage(true));
		toolsMenu.add(imgToPict);
		JMenuItem imgToMultiDrawstat = new JMenuItem("Image to Multi Drawstat");
		imgToMultiDrawstat.addActionListener(event -> importImage(false));
		JMenuItem showOptions = new JMenuItem("Show/Edit options");
		showOptions.addActionListener(event -> {
			try {
				Desktop.getDesktop().open(new File(BIDE.pathToOptions));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		toolsMenu.add(showOptions);
		JMenuItem showOpcodes = new JMenuItem("Show list of opcodes");
		showOpcodes.addActionListener(event -> createNewTab(BIDE.TYPE_OPCODE));
		toolsMenu.add(showOpcodes);
		JMenuItem showChars = new JMenuItem("Show characters list");
		showChars.addActionListener(event -> createNewTab(BIDE.TYPE_CHARLIST));
		toolsMenu.add(showChars);
		JMenuItem showColoration = new JMenuItem("Show syntax coloration test");
		showColoration.addActionListener(event -> createNewTab(BIDE.TYPE_COLORATION));
		toolsMenu.add(showColoration);
		
		JMenuItem cleanup = new JMenuItem("Clean up strings");
		cleanup.addActionListener(event -> BIDE.cleanupStrings());
		toolsMenu.add(cleanup);
		
		JMenu emulatorMenu = new JMenu("Emulator");
		
		if (BIDE.options.getProperty("runOn").equals("emulator")) {
			menuBar2.add(emulatorMenu);
		}
		
		JMenuItem takeEmuScreenshot = new JMenuItem("Take emulator screenshot");
		takeEmuScreenshot.addActionListener(event -> BIDE.autoImport.storeEmuScreenshot());
		emulatorMenu.add(takeEmuScreenshot);
		JMenuItem takeEmuScreenScreenshot = new JMenuItem("Take emulator screen screenshot");
		takeEmuScreenScreenshot.addActionListener(event -> BIDE.autoImport.storeEmuScreen());
		emulatorMenu.add(takeEmuScreenScreenshot);
		JMenuItem benchmark = new JMenuItem("Run benchmark");
		benchmark.addActionListener(event -> new Thread(() -> BIDE.autoImport.benchmark()).start());
		emulatorMenu.add(benchmark);
		window.setJMenuBar(menuBar2);

		window.setVisible(true);
	}
	public ProgramTextPane getTextPane() {
		try {
			return ((ProgScrollPane)this.jtp.getSelectedComponent()).textPane;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	public void createNewTab(int type) {
		
		if (type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
			String name;
			String size;
			if (type == BIDE.TYPE_PICT) {
				name = "PICT"+JOptionPane.showInputDialog(BIDE.ui.window, "Picture number (1-20):", "New picture", JOptionPane.QUESTION_MESSAGE);
				size = "800";
			} else {
				name = "CAPT"+JOptionPane.showInputDialog(BIDE.ui.window, "Capture number (1-20):", "New capture", JOptionPane.QUESTION_MESSAGE);
				size = "400";
			}
			if (name.endsWith("null")) return;
			
			BIDE.g1mParts.add(new G1MPart(name, size, new Byte[0], type));
			jtp.addTab(name, BIDE.g1mParts.get(BIDE.g1mParts.size()-1).comp);
			
		} else {
			String content = "";
			String option = "";
			String name = "";
			if (type == BIDE.TYPE_PROG) {
				name = JOptionPane.showInputDialog(BIDE.ui.window, "Program name:", "New program", JOptionPane.QUESTION_MESSAGE);
				option = "<no password>";
			} else if (type == BIDE.TYPE_OPCODE) {
				name = "Opcodes List";
				content = "#\n#DO NOT EDIT THIS TAB, changes won't be saved!\n#\n";
				BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getResourceAsStream("/opcodes.json"), StandardCharsets.UTF_8));
				String line;
				StringBuilder stringBuilder = new StringBuilder();

				try {
					while((line = reader.readLine()) != null) {
						stringBuilder.append(line);
						stringBuilder.append("\n");
					}

					content += stringBuilder.toString();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} else if (type == BIDE.TYPE_CHARLIST) {
				name = "All characters";
				BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getResourceAsStream("/characters.txt"), StandardCharsets.UTF_8));
				String line;
				StringBuilder stringBuilder = new StringBuilder();

				try {
					while((line = reader.readLine()) != null) {
						stringBuilder.append(line);
						stringBuilder.append("\n");
					}

					content += stringBuilder.toString();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} else if (type == BIDE.TYPE_COLORATION) {
				name = "Syntax coloration test";
				BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getResourceAsStream("/testColoration.txt"), StandardCharsets.UTF_8));
				String line;
				StringBuilder stringBuilder = new StringBuilder();

				try {
					while((line = reader.readLine()) != null) {
						stringBuilder.append(line);
						stringBuilder.append("\n");
					}

					content += stringBuilder.toString();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} else {
				BIDE.error("Unknown type "+type);
			}
			if (name == null || name.endsWith("null")) {
				return;
			}
			BIDE.g1mParts.add(new G1MPart(name, option, content, type));
			jtp.addTab(name, BIDE.g1mParts.get(BIDE.g1mParts.size()-1).comp);
		}
		
		selectLastTab();
	}
	
	public void selectLastTab() {
		jtp.setSelectedIndex(jtp.getTabCount()-1);
	    try {
	    	getTextPane().setCaretPosition(0);
	    } catch (NullPointerException ignored) {
	    }
	}
	
	public void openFile(boolean addToCurrentFile) {
		jfc.setCurrentDirectory(new File(BIDE.pathToG1M));
		
		File[] input = null;
		if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			input = jfc.getSelectedFiles();
		}
		if (input != null) {
			openFile(addToCurrentFile, input);
		}
	}
	
	public void openFile(boolean addToCurrentFile, File[] input) {
		
		new Thread(() -> {
			if (!addToCurrentFile) {
				BIDE.g1mParts = new ArrayList<>();
			}
			for (File file : input) {
				BIDE.pathToG1M = file.getPath();


				try {
					G1MParser g1mparser = new G1MParser(BIDE.pathToG1M);
					g1mparser.readG1M();

					if (g1mparser.isValid()) {
						BIDE.readFromG1M(BIDE.pathToG1M);
					} else {
						BIDE.readFromTxt(BIDE.pathToG1M);
					}

					if (!addToCurrentFile) {
						BIDE.pathToSavedG1M = BIDE.pathToG1M;
					}

					BIDE.g1mParts.sort((part0, part1) -> {
						if (part0.type == part1.type) {
							return part0.name.compareTo(part1.name);
						} else {
							return Integer.compare(part0.type, part1.type);
						}
					});


				} catch (NullPointerException e) {
					if (BIDE.debug) {
						System.err.print("debug exception: ");
						e.printStackTrace();
					}
				} catch (NoSuchFileException e) {
					BIDE.error("The file at \"" + BIDE.pathToG1M + "\" does not exist.");
				} catch (AccessDeniedException e) {
					BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToG1M + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (BIDE.g1mParts.size() != 0) {
					System.out.println("Finished loading g1m");
				}
			}

			jtp.removeAll();
			new SwingWorker<Void, G1MPart>() {
				@Override
				protected Void doInBackground() {
					for (int i = 0; i < BIDE.g1mParts.size(); i++) {
						jtp.addTab(BIDE.g1mParts.get(i).name, BIDE.g1mParts.get(i).comp);
					}
					try {
						getTextPane().setCaretPosition(0);
					} catch (NullPointerException e) {
						if (BIDE.debug) {
							System.err.print("debug exception: ");
							e.printStackTrace();
						}
					}
					return null;
				}
			}.execute();
		}).start();
    	
    }
	
	public void saveFile(boolean saveToG1M, boolean saveAs, boolean runFile) {
		
		new Thread(() -> {

			try {
				if (saveAs || BIDE.pathToSavedG1M.isEmpty()) {
					if (BIDE.pathToSavedG1M.isEmpty()) {
						BIDE.pathToSavedG1M = BIDE.pathToG1M;
					}
					jfc.setSelectedFile(new File(BIDE.pathToSavedG1M));
					File input = null;
					if (jfc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
						input = jfc.getSelectedFile();
					}
					if (input == null) {
						BIDE.pathToSavedG1M = "";
						return;
					}

					BIDE.pathToSavedG1M = input.getAbsolutePath();

					// Check for extension
					try {
						BIDE.pathToSavedG1M.substring(BIDE.pathToSavedG1M.lastIndexOf('.'));
					} catch (StringIndexOutOfBoundsException e) {
						BIDE.error("Please input an extension (.bide or .g1m)");
						BIDE.pathToSavedG1M = "";
						return;
					}

				}


				if (runFile) {
					BIDE.runOn = BIDE.options.getProperty("runOn");
					BIDE.writeToG1M(BIDE.pathToSavedG1M);

				} else {
					BIDE.runOn = "none";

					if (saveToG1M && !BIDE.pathToSavedG1M.endsWith(".bide") && !BIDE.pathToSavedG1M.endsWith(".txt")) {
						BIDE.writeToG1M(BIDE.pathToSavedG1M);

					} else {
						BIDE.writeToTxt(BIDE.pathToSavedG1M);
					}
				}


				// Update names
				for (int i = 0; i < jtp.getTabCount(); i++) {
					jtp.setTitleAt(i, BIDE.g1mParts.get(i).name);
				}
			} catch (NullPointerException e) {
				if (BIDE.debug) e.printStackTrace();
			} catch (NoSuchFileException e) {
				BIDE.error("The file at \"" + BIDE.pathToSavedG1M + "\" does not exist.");
			} catch (AccessDeniedException e) {
				BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToSavedG1M + "\"");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	
	
	public void importImage(boolean convertToPict) {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				try {
					String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
					if (extension.equals(".png") || extension.equals(".bmp")) {
						return true;
					}
				} catch (Exception ignored) {}
				return false;
			}

			@Override
			public String getDescription() {
				return "Image files (.png, .bmp)";
			}
		});
		
		File input = null; 
		if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			input = jfc.getSelectedFile();
		}
				
	    if (input != null) {
	    	try {
				BufferedImage img = ImageIO.read(input);
				
				if (convertToPict) {
					if (img.getWidth() != 128) {
						BIDE.error("Image must be 128 pixels wide!");
						return;
					}
					if (img.getHeight() > 128) {
						BIDE.error("Image must be a maximum of 128 pixels high!");
						return;
					}
					Byte[] binary = new Byte[0x800];
					Arrays.fill(binary, (byte)0);
					for (int j = 0; j < img.getHeight(); j++) {
						for (int i = 0; i < 128; i++) {
							if (img.getRGB(i, j) == Color.BLACK.getRGB()) {
								int calculatedIndex = i/8+16*j;
								binary[calculatedIndex] = (byte) (binary[calculatedIndex] | (0b10000000 >> (i%8)));
							}
						}
					}
					int size = 128*img.getHeight()/8;
					if (size == 0x400) {
						int option = JOptionPane.showConfirmDialog(BIDE.ui.window, "Do you want to import this picture with a size of 0x800?\nIf you don't understand the consequences, click yes.", "BIDE", JOptionPane.YES_NO_OPTION);
						size = 0x800;
				        if (option == JOptionPane.NO_OPTION) {
				        	size = 0x400;
				        }
					}
					
					String imgName = input.getName().substring(0, input.getName().lastIndexOf('.'));
					BIDE.g1mParts.add(new G1MPart(imgName, Integer.toHexString(size), binary, BIDE.TYPE_PICT));
					jtp.addTab(imgName, BIDE.g1mParts.get(BIDE.g1mParts.size()-1).comp);
					selectLastTab();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public void removeTab(int i) {
		int option = JOptionPane.showConfirmDialog(BIDE.ui.window, "Are you sure you want to close this tab?", "BIDE", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
        	jtp.remove(i);
        	BIDE.g1mParts.remove(i);
        }
	}
	
}

class ToolbarButton extends JButton {
	public ToolbarButton(String iconName, String toolTip) {
		super();
		try {
			this.setIcon(new ImageIcon(ImageIO.read(BIDE.class.getResourceAsStream("/images/"+iconName))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		this.setContentAreaFilled(false);
		this.setFocusPainted(false);
		this.setToolTipText(toolTip);
	}
}

class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
        
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        label.setFont(new Font(BIDE.options.getProperty("progFontName"), Font.PLAIN, 12));

        add(label);
        //tab button
        add(Box.createRigidArea(new Dimension(8,17)));
        JButton button = new TabButton();
        add(button);
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            setPreferredSize(new Dimension(9, 17));
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorderPainted(true);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
            	BIDE.ui.removeTab(i);
            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            g2.setStroke(new BasicStroke(1));
            
          
            g2.setColor(Color.GRAY);
            if (getModel().isRollover()) {
                g2.setColor(Color.BLACK);
            }
            int marginL = 0;
            int marginR = 2;
            int marginU = 4;
            int marginN = 6;
            
            g2.drawLine(marginL, marginU, getWidth()-marginR+1, getHeight()-marginN+1);
            g2.drawLine(marginL+1, marginU, getWidth()-marginR+1, getHeight()-marginN);
            g2.drawLine(marginL, marginU+1, getWidth()-marginR, getHeight()-marginN+1);

            g2.drawLine(marginL, getHeight()-marginN+1, getWidth()-marginR+1, marginU);
            g2.drawLine(marginL+1, getHeight()-marginN+1, getWidth()-marginR+1, marginU+1);
            g2.drawLine(marginL, getHeight()-marginN, getWidth()-marginR, marginU);
            g2.dispose();
        }
    }
}
