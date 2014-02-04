package org.or1k.vm.gui.term;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Terminal {
		
	private char[][] screen;
	
	private int widthInColumns;
	private int heightInRows;
	
	private int cursorColumn;
	private int cursorRow;
	
	private JFrame terminalFrame;
	private Font font;
	private KeyListener keyListener;
	
	public Terminal(int widthInColumns, int heightInRows) {
		
		this.font = new Font("Monospaced",Font.TRUETYPE_FONT, 9);
		
		this.widthInColumns = widthInColumns;
		this.heightInRows = heightInRows;
		
		this.screen = new char[widthInColumns][heightInRows];
		
		clear();
	}
	
	
	public void addKeyListener(KeyListener keyListener) {
		this.keyListener = keyListener;
	}
	
	public void clear() {
		setCursor(0, 0);
		for(int i=0; i<widthInColumns; i++) {
			Arrays.fill(screen[i],' ');
		}
	}
	
    private void setCursor(int i, int j) {
		cursorColumn = i;
		cursorRow = j;		
	}

	public void enterPrivateMode() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                terminalFrame = new JFrame("Terminal");
                                                
                FontMetrics fontMetrics = terminalFrame.getFontMetrics(font);
                int charWidth = fontMetrics.charWidth(' ');
                int charHeight = fontMetrics.getHeight();                
                terminalFrame.setPreferredSize(new Dimension(charWidth * widthInColumns, charHeight * heightInRows));
                                
                //terminalFrame.addComponentListener(new FrameResizeListener());
                terminalFrame.getContentPane().setLayout(new BorderLayout());
				//terminalFrame.getContentPane().add(terminalRenderer, BorderLayout.CENTER);
                terminalFrame.addKeyListener(keyListener);
                
                terminalFrame.pack();
                terminalFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                terminalFrame.setLocationByPlatform(true);
                terminalFrame.setVisible(true);
                terminalFrame.setFocusTraversalKeysEnabled(false);
                //terminalEmulator.setSize(terminalEmulator.getPreferredSize());
                terminalFrame.pack();
                //blinkTimer.start();                             
            }
        };
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            }
            catch(Exception e) {
                throw new RuntimeException(
                        "Unexpected " + e.getClass().getSimpleName() + 
                            " while creating SwingTerminal JFrame", e);
            }
        }
    }

    public void exitPrivateMode() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (terminalFrame == null) {
                    return;
                }

                //blinkTimer.stop();
                terminalFrame.setVisible(false);
                terminalFrame.dispose();
                terminalFrame = null;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            }
            catch (Exception e) {
                throw new RuntimeException(
                        "Unexpected " + e.getClass().getSimpleName()
                        + " while disposing SwingTerminal JFrame", e);
            }
        }
    }




	public void print(char arg) {
		
		switch(arg) {
			case '\n':
				cursorColumn = 0;
				cursorRow++;
				break;
			case '\t':
				cursorColumn += 8;
				break;
			default:
				screen[cursorColumn][cursorRow] = arg;
				cursorColumn++;
				break;
		}

		if (cursorColumn >= widthInColumns) {
			cursorColumn = 0;
			cursorRow++;
		}
				
		if (cursorRow >= heightInRows) {			
			for(int w=0; w<widthInColumns; w++) {
				for(int h=1; h<heightInRows; h++) {
					screen[w][h - 1] = screen[w][h];
					screen[w][h] = ' ';
				}
			}
			cursorRow = heightInRows - 1;
		}
		
		paintComponent();
	}
	
	 protected void paintComponent() {
		 
		 BufferedImage buffer = new BufferedImage(terminalFrame.getWidth(), terminalFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
		 Graphics2D graphics2D = buffer.createGraphics();
         
         graphics2D.setFont(font);
         graphics2D.setColor(java.awt.Color.BLACK);
         graphics2D.fillRect(0, 0, terminalFrame.getWidth(), terminalFrame.getHeight());
         
         FontMetrics fontMetrics = terminalFrame.getFontMetrics(font);
         int charWidth = fontMetrics.charWidth(' ');
         int charHeight = fontMetrics.getHeight();                
         terminalFrame.setPreferredSize(new Dimension(charWidth * widthInColumns, charHeight * heightInRows));
         
         graphics2D.setColor(java.awt.Color.WHITE);
         for(int column=0; column<widthInColumns; column++) {
        	 for (int row=0; row<heightInRows; row++) {
        		 if (screen[column][row] != ' ') {
        			 graphics2D.drawString(String.valueOf(screen[column][row]), column * charWidth, ((row + 1) * charHeight) - fontMetrics.getDescent());
        		 }
        	 }
         }
         
        terminalFrame.getGraphics().drawImage(buffer, 0, 0, null);
        
        graphics2D.dispose();
        
     } 
		
	
}
