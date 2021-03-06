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
 * 20.05.2008 - Version 0.6
 * - neue Methoden getExternsion & checkExtension
 * 09.04.2008 - Version 0.5.3
 * - Ausgabe in print verändert
 * 29.01.2008 - Version 0.5.2
 * - Debug-Statement für die Ausgabe des Spezialfalles eingefügt
 * 28.01.2008 - Version 0.5.1
 * - Fallüberprüfung für die Lösungen erweitert (mehr Abstufungen)
 * 26.01.2008 - Version 0.5
 * - Operatoren-Handling-Funktionen gelöscht, da nicht mehr benötigt
 * - Lösung-Handling-Funktionen gelöscht, werden auch nicht mehr benötigt
 * - Neue Konstanten TARGET & VECTOR
 * - Überprüfung, ob LOP gelöst ist, entfernt, da LOP immer gelöst sein muss.
 * - Kopfzeile bei Ausgabe des Problems auf der Konsole hinzugefügt
 * 25.01.2008 - Version 0.4.1
 * - Variablennamen für Spezialfälle angepasst
 * 28.12.2007 - Version 0.4
 * - Neue Überladung für getURL, mit der man sprachabhängige Dateien findet.
 * - Nicht mehr benötigte Methoden und Konstanten gelöscht.
 * - generateXMLContent in saveLOP umgewandelt, ist jetzt auch fürs Speichern
 *    zuständig
 * 19.12.2007 - Version 0.3.5
 * - Auf neues ExceptionHandling umgestellt
 * - DebugAusgabe optimiert
 * 17.12.2007 - version 0.3.4
 * - Unnötige Ausgaben entfernt
 * - Ausgabe der Lösungsflächen hinzugefügt
 * 08.11.2007 - Version 0.3.3.2
 * - BugFix: NullPointer in print behoben (falls lop nicht gelöst)
 * 04.11.2007 - Version 0.3.3.1
 * - BugFix: Doppelte Anführungszeichen bei den Operatoren entfernt.
 * 09.10.2007 - Version 0.3.3
 * - Fehler in getURL behoben
 * - Lösung des LOP nicht mehr speichern
 * 03.10.2007 - Version 0.3.2
 * - Fehler beim Speichern des Problems behoben (ich habe die Quotes vergessen,
 *    dadurch kam es zu ungültigem XML-Code)
 * - XML-Encoding-Tag gesetzt
 * 24.09.2007 - Version 0.3.1
 * - An verändertes Speicherformat angepasst
 * 23.08.2007 - Version 0.3
 * - Weitere Methoden hinzugefuegt
 * 30.07.2007 - Version 0.2
 * - Weitere Methoden hinzugefuegt
 * 29.07.2007 - Version 0.1
 *  - Datei hinzugefuegt
 */
package info.kriese.sopra.io;

import info.kriese.sopra.SoPraLOP;
import info.kriese.sopra.gui.MessageHandler;
import info.kriese.sopra.gui.lang.Lang;
import info.kriese.sopra.lop.LOP;
import info.kriese.sopra.lop.LOPSolution;
import info.kriese.sopra.lop.LOPSolutionArea;
import info.kriese.sopra.math.Fractional;
import info.kriese.sopra.math.Vector3Frac;
import info.kriese.sopra.math.impl.FractionalFactory;
import info.kriese.sopra.math.impl.Vector3FracFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import org.w3c.dom.NamedNodeMap;

/**
 * 
 * @author Michael Kriese
 * @version 0.6
 * @since 29.07.2007
 * 
 */
public final class IOUtils {

    /**
     * Speicherformat für die Zielfunktion.
     */
    private static final String TARGET = "<target  x=\"{0}\" y=\"{1}\" z=\"{2}\" />";

    /**
     * Speicherformat für die Variablen.
     */
    private static final String VECTOR = "\t<vector x=\"{0}\" y=\"{1}\" z=\"{2}\"  />";

    /**
     * Überprüft, ob die gegebene Datei die entsprechende Erweitrung besitzt.
     * 
     * @param file -
     *                Datei, die überprüft werden soll
     * @param ext -
     *                Dateierweiterung, mit der getestet werden soll
     * @return "TRUE" falls die Datei die Erweiterung hat, sonst "FALSE"
     */
    public static boolean checkExtension(File file, String ext) {
	if (ext != null && file != null
		&& ext.equals(IOUtils.getExtension(file)))
	    return true;
	return false;
    }

    /**
     * Formatiert einen Vektor mittels gegebenen Format in einen String zum
     * speichern.
     * 
     * @param pattern -
     *                Format-String, welcher benutzt werden soll.
     * @param vec -
     *                Vektor, welcher formatiert werden soll.
     * @return Der übergebene Vektor als formatierter String.
     */
    public static String format(String pattern, Vector3Frac vec) {
	return MessageFormat.format(pattern, new Object[] { vec.getCoordX(),
		vec.getCoordY(), vec.getCoordZ() });
    }

    /**
     * Gibt die Dateierweiterung zurück.
     * 
     * @param file -
     *                Datei, von der die Erweiterung zurückgegeben werden soll.
     * @return Dateierweiterung falls vorhanden, sonst "NULL"
     */
    public static String getExtension(File file) {
	String f = file.getName();
	int i = f.lastIndexOf(".");

	if (i > 0 && i < f.length() - 1)
	    return f.substring(i + 1).toLowerCase();
	return null;
    }

    /**
     * Findet eine Datei und gibt die URL zurück.
     * 
     * @param file -
     *                Datei, welche gefunden werden soll.
     * @return URL-Objekt, welches auf die gesuchte Datei zeigt. null, falls die
     *         Datei nicht existiert.
     */
    public static URL getURL(String file) {
	URL res = SoPraLOP.class.getResource(file);
	return res;
    }

    /**
     * Findet eine sprachabhängige Datei und gibt die URL zurück.
     * 
     * @param path -
     *                Pfad, in welchem sich die Datei befindet.
     * @param file -
     *                Datei, welche gefunden werden soll.
     * @param l -
     *                Locale-Objekt, welches die Sprache enthält.
     * @return URL-Objekt, welches auf die gesuchte Datei zeigt. null, falls die
     *         Datei nicht existiert.
     */
    public static URL getURL(String path, String file, Locale l) {
	URL url;
	url = getURL(path + "_" + l.getLanguage() + "_" + l.getCountry() + "/"
		+ file);

	if (url == null)
	    url = getURL(path + "_" + l.getLanguage() + "/" + file);

	if (url == null)
	    url = getURL(path + "/" + file);
	return url;
    }

    /**
     * Extrahiere einen Vector aus den xml-Daten.
     * 
     * @param map -
     *                NamedNodeMap aus xml-Daten
     * @return einen Vector3Frac
     */
    public static Vector3Frac getVector(NamedNodeMap map) {
	if (map == null)
	    return null;

	try {
	    Fractional x, y, z;
	    x = FractionalFactory.getInstance(map.getNamedItem("x")
		    .getNodeValue());
	    y = FractionalFactory.getInstance(map.getNamedItem("y")
		    .getNodeValue());
	    z = FractionalFactory.getInstance(map.getNamedItem("z")
		    .getNodeValue());

	    if (x != null && y != null && z != null)
		return Vector3FracFactory.getInstance(x, y, z);
	} catch (Exception e) {
	    MessageHandler.exceptionThrown(e);
	}
	return null;
    }

    /**
     * Dient zur Ausgabe eines LOP.
     * 
     * @param lop -
     *                LOP, welches ausgegeben werden soll
     * @param out -
     *                PrintStream, auf dem das LOP ausgegeben werden soll.
     */
    public static void print(LOP lop, PrintStream out) {

	out.println("--------------------------------------------------------");

	StringBuffer x = new StringBuffer(), y = new StringBuffer(), z = new StringBuffer(), t = new StringBuffer();

	Vector3Frac vec = lop.getVectors().get(0);

	t.append("\tX1\t");
	x.append("\t" + vec.getCoordX() + "\t");
	y.append("\t" + vec.getCoordY() + "\t");
	z.append("\t" + vec.getCoordZ() + "\t");

	for (int i = 1; i < lop.getVectors().size(); i++) {
	    vec = lop.getVectors().get(i);
	    if (vec.getCoordX().toDouble() >= 0)
		x.append("+");
	    if (vec.getCoordY().toDouble() >= 0)
		y.append("+");
	    if (vec.getCoordZ().toDouble() >= 0)
		z.append("+");

	    t.append("X" + (i + 1) + "\t");
	    x.append(vec.getCoordX() + "\t");
	    y.append(vec.getCoordY() + "\t");
	    z.append(vec.getCoordZ() + "\t");
	}

	vec = lop.getTarget();

	t.append("= Z");
	x.append("= " + vec.getCoordX());
	y.append("= " + vec.getCoordY());
	z.append("= " + (lop.isMaximum() ? "max" : "min"));

	out.println(t.toString());
	out.println(x.toString());
	out.println(y.toString());
	out.println(z.toString());

	LOPSolution sol = lop.getSolution();

	out.println();
	out.println("Lösung: " + sol.getValue() + " (" + sol.getSpecialCase()
		+ ")");

	switch (sol.getSpecialCase()) {
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_EMPTY
		    | LOPSolution.SOLUTION_AREA_EMPTY | LOPSolution.TARGET_FUNCTION_EMPTY):
		out.println(Lang.getString("Strings.NoSolution"));
		break;
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_EMPTY
		    | LOPSolution.SOLUTION_AREA_UNLIMITED | LOPSolution.TARGET_FUNCTION_UNLIMITED):
		out.println(Lang.getString("Strings.UnlimitedSol"));
		break;
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_POINT
		    | LOPSolution.SOLUTION_AREA_UNLIMITED | LOPSolution.TARGET_FUNCTION_LIMITED):
		out.println(Lang.getString("Strings.UnlimitedButSolution"));
		break;
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_MULTIPLE
		    | LOPSolution.SOLUTION_AREA_LIMITED | LOPSolution.TARGET_FUNCTION_LIMITED):
		out.println(Lang.getString("Strings.MoreSolutions",
			new Object[] { sol.countAreas() }));
		// out.println(sol.getAreas());
		break;
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_MULTIPLE
		    | LOPSolution.SOLUTION_AREA_UNLIMITED | LOPSolution.TARGET_FUNCTION_LIMITED):
		out.println(Lang.getString("Strings.UnlimitedButMoreSolutions",
			new Object[] { sol.countAreas() }));
		// out.println(sol.getAreas());
		break;
	    case (LOPSolution.OPTIMAL_SOLUTION_AREA_POINT
		    | LOPSolution.SOLUTION_AREA_LIMITED | LOPSolution.TARGET_FUNCTION_LIMITED):
		out.println(Lang.getString("Strings.SimpleSolution"));
		break;
	    default:
		out.println("Error! Wrong special case. ("
			+ sol.getSpecialCase() + ")");
		break;
	}

	for (LOPSolutionArea area : lop.getSolution().getAreas())
	    out.println("[ X" + (lop.getVectors().indexOf(area.getL1()) + 1)
		    + ", X" + (lop.getVectors().indexOf(area.getL2()) + 1)
		    + " ] = [ " + area.getL1Amount() + ", "
		    + area.getL2Amount() + " ]");

	out.println();
    }

    /**
     * Generieren und Speichern des LOP&#039;s.
     * 
     * @param lop -
     *                LOP, welches gespeichert werden soll.
     * @param file -
     *                File-Objekt, welches die Datei repräsentiert, in das
     *                gespeichert wird
     * @throws IOException -
     *                 Falls Datei nicht existiert oder nicht geschrieben werden
     *                 kann.
     * 
     */
    public static void saveLOP(LOP lop, File file) throws IOException {

	FileOutputStream fout = new FileOutputStream(file, false);
	PrintStream out = new PrintStream(fout, false, "UTF-8");

	// xml-Header
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<lop type=\""
		+ (lop.isMaximum() ? "max" : "min") + "\">");

	// die Vektordaten der Variablen xi
	out.println("<vectors>");
	for (Vector3Frac vec : lop.getVectors())
	    out.println(format(VECTOR, vec));
	out.println("</vectors>");

	// die Vektordaten der Zielfunktion
	out.println(format(TARGET, lop.getTarget()));

	// schliessendes xml-Tag
	out.println("</lop>");

	out.close();
    }

    /**
     * Privater Konstruktor, da keine Instanz dieser Klasse erlaubt.
     */
    private IOUtils() {
    }
}
