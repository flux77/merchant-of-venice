package org.mov.main;

import java.awt.*;
import javax.swing.*;

import org.mov.util.*;

public class Analyser extends JFrame {
    
    private JDesktopPane desktop;
    private AnalyserMenu menu;

    public Analyser() {
	setTitle("Venice");
	setSize(800, 600);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	desktop = new JDesktopPane();
	menu = new AnalyserMenu(this, desktop);
	setContentPane(desktop);
	Progress.setDesktop(desktop);
    }

    public static void main(String[] args) {
	Analyser analyser = new Analyser();

	analyser.setVisible(true);
    }
}
