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
 * 27.01.2008 - Verison 0.1.1
 * - Anderer TestCase
 * - Quickhull Debug-Modus aktiviert
 * 26.01.2008 - Version 0.1
 *  - Datei hinzugefuegt
 */
package info.kriese.sopra.test;

import info.kriese.sopra.io.impl.SettingsFactory;
import info.kriese.sopra.math.Vector3Frac;
import info.kriese.sopra.math.Vertex;
import info.kriese.sopra.math.impl.Vector3FracFactory;
import info.kriese.sopra.math.quickhull.QuickHull;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Michael Kriese
 * @version 0.1.1
 * @since 26.01.2008
 * 
 */
public final class TestQuickhull {

    public static void main(String[] args) {

	// Parse commandline arguments
	SettingsFactory.parseArgs(args);

	SettingsFactory.initJava();

	SettingsFactory.showTitle("QuickhullTest");

	List<Vector3Frac> vecs = new LinkedList<Vector3Frac>(), vecsnew = new LinkedList<Vector3Frac>();
	vecs.add(Vector3FracFactory.getInstance(0, 0, 8));
	vecs.add(Vector3FracFactory.getInstance(4, 4, 4));
	vecs.add(Vector3FracFactory.getInstance(2, -2, 2));
	vecs.add(Vector3FracFactory.getInstance(2, 0, 2));

	QuickHull hull = new QuickHull();
	hull.setDebug(true);

	hull.build(vecs);

	for (Vertex vtx : hull.getVerticesList()) {
	    if (!vecsnew.contains(vtx.p1))
		vecsnew.add(vtx.p1);
	    if (!vecsnew.contains(vtx.p2))
		vecsnew.add(vtx.p2);
	    if (!vecsnew.contains(vtx.p3))
		vecsnew.add(vtx.p3);
	}

	System.out.println(vecs);
	System.out.println(vecsnew);

	System.out.println();

	for (Vertex vtx : hull.getVerticesList())
	    System.out.println(vtx);
    }
}
