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
 * 26.01.2008 - Version 0.5.8
 * - Operator-Handling entfernt, da nicht mehr benötigt
 * 25.01.2008 - Version 0.5.7
 * - Variablennamen für Spezialfälle angepasst
 * - Fallprüfung für Spezialfälle erweitert
 * 28.12.2007 - Version 0.5.6
 * - Speichern des LOP an IOUtils weitergereicht
 * 19.12.2007 - Version 0.5.5
 * - Auf neues ExceptionHandling umgestellt
 * 17.12.2007 - Version 0.5.4
 * - BugFix: Fehler beim Speichern in Pfaden mit Leerzeichen behoben, LOP
 *    konnte dadurch nicht gespeichert werden.
 * 09.11.2007 - Version 0.5.3
 * - Aufruf von showSolution entfernt, da nicht mehr nötig
 * 08.11.2007 - Version 0.5.2
 * - solve() nochmals überarbeitet, sollte jetzt korrekt funktionieren
 * 07.11.2007 - Version 0.5.1
 * - An neues GaussVerfahren angepasst
 * 01.11.2007 - Version 0.5
 * - An LOPEditor angepasst
 * 24.10.2007 - Version 0.4.0.1
 * - BugFix: Mehrere Lösungen wurden als eine erkannt
 * 23.10.2007 - Version 0.4
 * - An neuen Quickhull-Algorithmus angepasst
 * 16.10.2007 - Version 0.3.7.2
 * - NullPointer behoben, falls zu öffnendes LOP nicht existiert
 * - Methode solve() überarbeitet, Gausselimination vereinheitlicht
 * 12.10.2007 - Version 0.3.7.1
 * - Optimum wird auch bei Unlimited & NoSolution gesetzt
 * 11.10.2007 - Version 0.3.7
 * - Spezialfallbehandlung überarbeitet, sie war fehlerhaft
 * 09.10.2007 - Version 0.3.6
 * - Speichern der optimalen Vektoren überarbeitet
 * - Lösung wird nicht mehr aus Datei geladen (Live Berechnung)
 * 07.10.2007 - Version 0.3.5
 * - spezielle Lösungen eines LOP werden ermittelt
 * - Optimum und optimale Vektoren werden gespeichert
 * 03.10.2007 - Version 0.3.4
 * - Behandlung von Speziellen LOP's
 * 24.09.2007 - Version 0.3.3
 * - An verändertes Speicherformat angepasst
 * 17.09.2007 - Version 0.3.2
 * - Kann einfache Probleme lösen
 * 11.09.2007 - Version 0.3.1
 *  - problem.createSolution -> problem.getSolution()
 *  - HTML-Kram ausgelagert -> HTMLLinearOptimizingProblem
 *  - Visualisierung in MainFrame ausgelagert
 * 23.08.2007 - Version 0.3
 *  - Das Grundproblem in eigene Klasse extrahiert
 *  - Diverse Methoden geloescht
 *  - Umbenannt
 *  - Weitere Aenderungen folgen
 * 30.07.2007 - Version 0.2
 * - Problem laden implementiert
 * - Weitere Methoden hizugefuegt
 * 29.07.2007
 * - Problem kann teilweise gespeichert werden
 * - Getter & Setter hinzugefuegt
 * 10.05.2007 - Version 0.1
 * - Datei hinzugefuegt
 */
package info.kriese.soPra.math;

import info.kriese.soPra.gui.MessageHandler;
import info.kriese.soPra.io.IOUtils;
import info.kriese.soPra.io.impl.SettingsFactory;
import info.kriese.soPra.lop.LOP;
import info.kriese.soPra.lop.LOPEditor;
import info.kriese.soPra.lop.LOPEditorAdapter;
import info.kriese.soPra.lop.LOPSolution;
import info.kriese.soPra.math.quickhull.QuickHull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Michael Kriese
 * @version 0.5.8
 * @since 10.05.2007
 * 
 */
public final class LOPSolver {

    private final QuickHull hull;

    public LOPSolver() {
	this.hull = new QuickHull();
    }

    public void setEditor(LOPEditor editor) {
	editor.addListener(new LOPEditorAdapter() {
	    @Override
	    public boolean open(LOP lop, URL file) {
		return LOPSolver.this.open(lop, file);
	    }

	    @Override
	    public void save(LOP lop, URL file) {
		LOPSolver.this.save(lop, file);
	    }

	    @Override
	    public void solve(LOP lop) {
		LOPSolver.this.solve(lop);
	    }
	});
    }

    private boolean open(LOP lop, URL file) {
	if (file == null)
	    return false;
	try {
	    Node n = null;
	    NamedNodeMap att = null;
	    NodeList ndList = null;

	    boolean bMax = true;
	    Vector3Frac vTarget = null;
	    Vector<Vector3Frac> vecs = new Vector<Vector3Frac>();
	    // ---- Parse XML file ----
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document document = builder.parse(file.openStream());

	    // ---- Get list of nodes to given element tag name ----
	    ndList = document.getElementsByTagName("lop");
	    n = ndList.item(0);

	    att = n.getAttributes();
	    if (att != null) {
		n = att.getNamedItem("type");
		if (n != null && n.getNodeValue().equals("min"))
		    bMax = false;
	    }

	    ndList = document.getElementsByTagName("target");
	    if (ndList.getLength() == 1) {
		n = ndList.item(0);
		att = n.getAttributes();
		if (att != null)
		    vTarget = IOUtils.getVector(att);
	    }

	    ndList = document.getElementsByTagName("vectors");
	    if (ndList.getLength() == 1) {
		ndList = ndList.item(0).getChildNodes();

		if (ndList.getLength() > 2)
		    for (int i = 0; i < ndList.getLength(); i++) {
			if (!ndList.item(i).getNodeName().equals("vector"))
			    continue;
			Vector3Frac tmp = IOUtils.getVector(ndList.item(i)
				.getAttributes());
			if (tmp != null)
			    vecs.add(tmp);
			else {
			    vecs.clear();
			    break;
			}
		    }
	    }

	    if (vTarget != null && vecs.size() >= LOP.MIN_VECTORS
		    && vecs.size() <= LOP.MAX_VECTORS) {
		lop.setMaximum(bMax);
		lop.setTarget(vTarget);
		lop.setVectors(vecs);
		return true;
	    }

	    // ---- Error handling ----
	} catch (Exception e) {
	    MessageHandler.exceptionThrown(e);
	}
	return false;
    }

    /**
     * Speichert das LOP in die angegebene Datei
     * 
     * @param lop -
     *                LOP, welches gespeichert werden soll.
     * @param file -
     *                Datei, in die gespeichert werden soll.
     * @return true, falls das Speichern erfolgreich war, sonst false.
     */
    private boolean save(LOP lop, URL file) {
	try {
	    IOUtils.saveLOP(lop, new File(file.toURI()));
	    return true;
	} catch (IOException e) {
	    MessageHandler.exceptionThrown(e);
	} catch (URISyntaxException e) {
	    MessageHandler.exceptionThrown(e);
	}
	return false;
    }

    private void solve(LOP lop) {
	LOPSolution sol = lop.getSolution();

	this.hull.build(lop.getVectors());

	Vector3Frac sln, opt_vector = lop.getTarget().clone();

	boolean max = lop.isMaximum(), unlimited = false;
	Fractional value_high, value_low, opt = null, value_unlimit = null;

	value_high = Fractional.MAX_VALUE;
	value_low = Fractional.MIN_VALUE;

	for (Vertex vertex : this.hull.getVerticesList()) {
	    sln = Gauss.eliminate(vertex, lop.getTarget());

	    if (vertex.p1.equals(Vector3Frac.ZERO)) {
		opt_vector.setCoordZ(sln.getCoordZ());

		if (sln.getCoordX().is(Fractional.GEQUAL_ZERO)
			&& sln.getCoordY().is(Fractional.GEQUAL_ZERO)) {

		    if (opt == null) {
			opt = sln.getCoordZ();
			sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(), sln
				.getCoordY());
		    }

		    if (max && sln.getCoordZ().equals(opt)
			    && !value_high.equals(Fractional.MAX_VALUE)) {
			sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(), sln
				.getCoordY());
			break;
		    }
		    if (!max && sln.getCoordZ().equals(opt)
			    && !value_low.equals(Fractional.MIN_VALUE)) {
			sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(), sln
				.getCoordY());
			break;
		    }
		    if (sln.getCoordZ().compareTo(value_high) < 0) {
			if (!max) {
			    sol.clearAreas();
			    sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(),
				    sln.getCoordY());
			}
			value_low = sln.getCoordZ();
		    }
		    if (sln.getCoordZ().compareTo(value_low) > 0) {
			if (max) {
			    sol.clearAreas();
			    sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(),
				    sln.getCoordY());
			}
			value_high = sln.getCoordZ();
		    }
		    if (sln.getCoordZ().compareTo(opt) > 0) {
			if (max) {
			    opt = sln.getCoordZ();
			    sol.clearAreas();
			    sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(),
				    sln.getCoordY());
			}
			value_high = sln.getCoordZ();
		    }
		    if (sln.getCoordZ().compareTo(opt) < 0) {
			if (!max) {
			    opt = sln.getCoordZ();
			    sol.clearAreas();
			    sol.addArea(vertex.p2, vertex.p3, sln.getCoordX(),
				    sln.getCoordY());
			}
			value_low = sln.getCoordZ();
		    }
		}
	    } else {
		// Überprüfe ob Zielvektor den Kegelboden durchstößt
		// Falls ja ist MAX oder MIN unendlich

		opt_vector.setCoordZ(sln.getCoordZ());

		if (vertex.isPointInVertex(opt_vector)) {
		    unlimited = true;
		    value_unlimit = sln.getCoordZ();
		}
	    }
	}

	if (unlimited && opt != null) {
	    if (max && opt.compareTo(value_unlimit) < 0) {
		sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_EMPTY
			| LOPSolution.SOLUTION_AREA_EMPTY
			| LOPSolution.TARGET_FUNCTION_EMPTY);
		opt = Fractional.MAX_VALUE;
	    }

	    if (!max && opt.compareTo(value_unlimit) > 0) {
		sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_EMPTY
			| LOPSolution.SOLUTION_AREA_EMPTY
			| LOPSolution.TARGET_FUNCTION_EMPTY);
		opt = Fractional.MIN_VALUE;
	    }

	    if (max && opt.compareTo(value_unlimit) > 0)
		sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_POINT
			| LOPSolution.SOLUTION_AREA_UNLIMITED
			| LOPSolution.TARGET_FUNCTION_LIMITED);

	    if (!max && opt.compareTo(value_unlimit) < 0)
		sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_POINT
			| LOPSolution.SOLUTION_AREA_UNLIMITED
			| LOPSolution.TARGET_FUNCTION_LIMITED);

	    if (sol.countAreas() >= 2)
		sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_MULTIPLE
			| LOPSolution.SOLUTION_AREA_UNLIMITED
			| LOPSolution.TARGET_FUNCTION_LIMITED);
	} else if (!unlimited && opt != null && sol.countAreas() >= 2)
	    sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_MULTIPLE
		    | LOPSolution.SOLUTION_AREA_LIMITED
		    | LOPSolution.TARGET_FUNCTION_LIMITED);

	else if (opt == null) {
	    sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_EMPTY
		    | LOPSolution.SOLUTION_AREA_EMPTY
		    | LOPSolution.TARGET_FUNCTION_EMPTY);
	    opt = Fractional.MAX_VALUE;
	} else
	    sol.setSpecialCase(LOPSolution.OPTIMAL_SOLUTION_AREA_POINT
		    | LOPSolution.SOLUTION_AREA_LIMITED
		    | LOPSolution.TARGET_FUNCTION_LIMITED);

	sol.setValue(opt);

	lop.problemSolved();

	if (SettingsFactory.getInstance().isDebug())
	    IOUtils.print(lop, System.err);
    }
}
