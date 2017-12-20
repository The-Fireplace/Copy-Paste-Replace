package the_fireplace.cpr;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.*;

@SuppressWarnings("serial")
public class CopyPasteReplace extends JFrame {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;
	
	private JLabel replaceL, withL;
	private JTextField replaceTF, withTF;
	private JButton replaceB, exitB;
	private JCheckBox outputNoChange;
	private JProgressBar progressBar;
	
	private ReplaceButtonHandler rbHandler;
	private ExitButtonHandler ebHandler;
	
	private boolean isReplacing = false;
	
	public CopyPasteReplace(){
		replaceL = new JLabel("String to replace: ", SwingConstants.TRAILING);
		withL = new JLabel("<html>Strings to replace it with: <br /><i>Separate these with colons</i></html>", SwingConstants.TRAILING);
		
		replaceTF = new JTextField(10);
		replaceTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			  	if(!replaceTF.getText().isEmpty())
					replaceB.setEnabled(true);
				else
					replaceB.setEnabled(false);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(!replaceTF.getText().isEmpty())
					replaceB.setEnabled(true);
				else
					replaceB.setEnabled(false);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if(!replaceTF.getText().isEmpty())
					replaceB.setEnabled(true);
				else
					replaceB.setEnabled(false);
			}
		});
		withTF = new JTextField(10);
		
		replaceB = new JButton("Replace");
		rbHandler = new ReplaceButtonHandler();
		replaceB.addActionListener(rbHandler);
		replaceB.setEnabled(false);
		exitB = new JButton("Exit");
		ebHandler = new ExitButtonHandler();
		exitB.addActionListener(ebHandler);
		
		outputNoChange = new JCheckBox("Output with no content changes");
		
		progressBar = new JProgressBar();
		
		setTitle("Copy, Paste, Replace. Java Edition.");
		Container pane = getContentPane();
		pane.setLayout(new GridLayout(4, 2));
		
		pane.add(replaceL);
		pane.add(replaceTF);
		pane.add(withL);
		pane.add(withTF);
		pane.add(outputNoChange);
		pane.add(progressBar);
		pane.add(exitB);
		pane.add(replaceB);
		
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

    public static void main(String[] args) throws IOException {
    	new CopyPasteReplace();
    }
    
    private class ReplaceButtonHandler implements ActionListener {
    	@Override
		public void actionPerformed(ActionEvent e) {
			if(!isReplacing) {//Only let this activate if the program is not currently making copies.
				isReplacing = true;
				String oldName = replaceTF.getText();
				if(oldName.isEmpty()) {//Sanity check to make sure we aren't trying to replace nothing.
					isReplacing = false;
					return;
				}
				
				String[] args = withTF.getText().split(":");
				File currentPath = new File(".");
				File[] fileList = currentPath.listFiles();
				progressBar.setMaximum(fileList.length-1);
				int prog = 0;
				progressBar.setValue(prog);
				for(File file:fileList) {
					if(file.isFile()){
						//System.out.println("Found regular file: "+file.getName());
						for(int i=0;i<args.length;i++) {
							File newFile;
							if(file.getName().contains(oldName)) {
								newFile = new File(".", file.getName().replace(oldName, args[i]));
								try {
									while(!newFile.createNewFile()) {//If the file exists, append _alt to the filename
										String prevName = newFile.getName();
										int extensionIndex = prevName.lastIndexOf('.');
										newFile = new File(".", prevName.substring(0, extensionIndex)+"_alt"+prevName.substring(extensionIndex));
									}
								} catch(IOException ex) {
									ex.printStackTrace();
								}
								//Copy file contents
								FileReader fr = null;
								FileWriter fw = null;
								try {
									fr = new FileReader(file);
									fw = new FileWriter(newFile);
									int c = fr.read();
									while(c!=-1) {
										fw.write(c);
										c = fr.read();
									}
								} catch(IOException ex) {
									ex.printStackTrace();
								} finally {
									close(fr);
									close(fw);
								}
							} else
								newFile = file;
							//Replace the contents of the new file
							try {
								String content = new String(Files.readAllBytes(newFile.toPath()));
								String newContent = content.replaceAll(oldName, args[i]);
								if(outputNoChange.getModel().isSelected() || !content.equals(newContent))//Only output files that have changed, unless the checkbox is selected
									Files.write(newFile.toPath(), newContent.getBytes());
							} catch(IOException ex) {
								ex.printStackTrace();
							}
						}
					}
					progressBar.setValue(++prog);
				}
				isReplacing = false;
			}
		}
    }
    
    public static void close(Closeable stream) {
        try {
            if (stream != null)
                stream.close();
        } catch(IOException ex) {
            //...
        }
    }
    
    public class ExitButtonHandler implements ActionListener {
    	@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
}
