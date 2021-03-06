/**
 * @version		$Id$
 * @copyright	(c)2007-2008 Michael Kriese & Peer Sterner
 * 
 * This file is part of SoPraLOP Project.
 *
 *  SoPraLOP Project is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  SoPraLOP Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SoPraLOP Project; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * ChangeLog:
 * 
 * 20.05.2008 - Version 0.5.10
 * - An neuen FileChooser angepasst
 * 26.01.2008 - Version 0.5.9
 * - Aufruf von LOP.problemChanged entfernt, da überflüssig.
 * - An neue SettingsFactory angepasst.
 * 17.01.2008 - Version 0.5.8
 * - Panel für Duales Problem entfernt
 * 28.12.2007 - Version 0.5.7
 * - Komandozeilenparameterhandling in SettingsFactory ausgelagert
 * 27.12.2007 - Version 0.5.6
 * - Hilfe-Fenster hinzugefügt
 * - Übergabe für Funktionsmenü hinzugefügt
 * 19.12.2007 - Version 0.5.5
 * - StartParameter werden verarbeitet
 * 04.12.2007 - Version 0.5.4
 * - An neuen HelpProvider angepasst
 * 03.12.2007 - Version 0.5.3
 * - An ErrorHandler angepasst
 * 26.11.2007 - Version 0.5.2
 * - Panel zur Visualisierung des Dualen Problems hinzugefügt
 * 04.11.2007 - Version 0.5.1
 * - Unnötige Ausgabe entfernt
 * 31.10.2007 - Version 0.5
 * - An LOPEditor angepasst
 * - Boot Meldungen angepasst (Reihenfolge)
 * 24.10.2007 - Version 0.4
 *  - ActionListener Verhalten geändert
 *  - Startnachrichten multisprachfähig gemacht
 * 19.10.2007 - Version 0.3
 * - Funktionalität aus GUI hirhin ausgelagert
 * 14.09.2007 - Version 0.2.1
 * - Property für die RenderEngine hizugefügt (Unter Vista schlechte
 *    Performance mit OpenGL)
 * 11.09.2007- Version 0.2
 * - Default DateiCodierung auf UTF-8 geändert
 * 29.07.2007 - Version 0.1.1
 * - unnoetige Variablen geloescht
 * 12.05.2007 - Version 0.1
 * - Datei hinzugefuegt
 */
package info.kriese.sopra;

import info.kriese.sopra.engine3D.Engine3D;
import info.kriese.sopra.gui.AboutDialog;
import info.kriese.sopra.gui.ActionHandler;
import info.kriese.sopra.gui.HelpDialog;
import info.kriese.sopra.gui.InputPanel;
import info.kriese.sopra.gui.MainFrame;
import info.kriese.sopra.gui.MessageHandler;
import info.kriese.sopra.gui.SplashDialog;
import info.kriese.sopra.gui.Visual3DFrame;
import info.kriese.sopra.gui.filechooser.FileChooserFactory;
import info.kriese.sopra.gui.lang.Lang;
import info.kriese.sopra.io.impl.SettingsFactory;
import info.kriese.sopra.lop.LOP;
import info.kriese.sopra.lop.LOPEditor;
import info.kriese.sopra.lop.impl.LOPFactory;
import info.kriese.sopra.math.LOPSolver;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Michael Kriese
 * @version 0.5.10
 * @since 12.05.2007
 * 
 */
public final class SoPraLOP {

    public static AboutDialog ABOUT;

    public static LOPEditor EDITOR;

    public static Engine3D ENGINE;

    public static InputPanel INPUT;

    public static MainFrame MAIN;

    public static LOPSolver SOLVER;

    public static Visual3DFrame VISUAL;

    /**
     * @param args
     */
    public static void main(String[] args) {

	// Parse commandline arguments
	SettingsFactory.parseArgs(args);

	SettingsFactory.initJava();

	SettingsFactory.showTitle();

	SplashDialog splash = SplashDialog.getInstance();
	splash.setVisible(true);

	splash.setMessage(Lang.getString("Boot.LOP"));
	LOP lop = LOPFactory.newLinearOptimizingProblem();
	EDITOR = LOPFactory.newLOPEditor(lop);
	ActionHandler.INSTANCE.setLOP(lop);

	splash.setMessage(Lang.getString("Boot.MainFrame"));
	MAIN = new MainFrame();
	MAIN.setLOP(lop);
	MAIN.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentHidden(ComponentEvent e) {
		ActionHandler.exit();
	    }
	});
	MessageHandler.setParent(MAIN);
	MessageHandler.setHelp(MAIN);

	splash.setMessage(Lang.getString("Boot.VisualFrame"));
	VISUAL = new Visual3DFrame(MAIN);
	VISUAL.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentHidden(ComponentEvent e) {
		ENGINE.addConnection(MAIN);
	    }

	    @Override
	    public void componentShown(ComponentEvent e) {
		ENGINE.addConnection(VISUAL);
	    }
	});

	splash.setMessage(Lang.getString("Boot.About"));
	ABOUT = AboutDialog.getInstance(MAIN);

	splash.setMessage(Lang.getString("Boot.Help"));
	HelpDialog.setParent(MAIN);
	HelpDialog.getInstance().setHelp("index.htm");

	splash.setMessage(Lang.getString("Boot.FileChooser"));
	FileChooserFactory.setParent(MAIN);

	splash.setMessage(Lang.getString("Boot.InputPanel"));
	INPUT = new InputPanel();
	INPUT.setEditor(EDITOR);
	MAIN.setContent(SoPraLOP.INPUT);
	MAIN.setFunctions(INPUT.getFunctions());

	splash.setMessage(Lang.getString("Boot.3DEngine"));
	ENGINE = new Engine3D();
	ENGINE.addConnection(MAIN);
	ENGINE.setLOPEditor(EDITOR);

	splash.setMessage(Lang.getString("Boot.Solver"));
	SOLVER = new LOPSolver();
	SOLVER.setEditor(EDITOR);
	EDITOR.solve();

	splash.setMessage(Lang.getString("Boot.ShowMain"));
	MAIN.setVisible(true);
	splash.setMessage("");
	splash.setVisible(false);
    }
}
