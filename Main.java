import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class Main {
	
	static int[][] grid;
	static boolean[][] vis;
	static int dim = 9;
	
	public static void main(String[] args) throws Exception {
		String gridText = getGridText("https://nine.websudoku.com/");
		Document doc = convertStringToXMLDocument(fix(gridText.replaceAll("READONLY", "")));
		NodeList list = doc.getElementsByTagName("INPUT");
		
		grid = new int[dim][dim];
		vis = new boolean[dim][dim];
		
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				Node node = list.item(i*dim + j).getAttributes().getNamedItem("VALUE");
				if(node == null)
					grid[i][j] = 0;
				else
					grid[i][j] = Integer.parseInt(node.getNodeValue());
				if(grid[i][j] != 0)
					vis[i][j] = true;
			}
		}
		
		solve(0, 0);
		for (int i = 0; i < dim; i++) {
			String out = "";
			for (int j = 0; j < dim; j++) {
				out += grid[i][j] + " ";
			}
			System.out.println(out.trim());
		}
	}

	public static String getGridText(String lien) throws Exception {
		URL url;
		String line = "";

		url = new URL(lien);
		URLConnection conn = url.openConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			if(inputLine.startsWith("<TABLE")){
				line = inputLine;
				break;
			}
				
		}
		br.close();

		return line;
	}

	private static Document convertStringToXMLDocument(String xmlString) {
		// Parser that produces DOM object trees from XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try {
			// Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			// Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// <TABLE id="puzzle_grid" CELLSPACING=0 CELLPADDING=0 CLASS=t><TR><TD
	// CLASS=g0 ID=c00>
	public static String fix(String xml) {
		String out = "";

		int j = 0;

		while (j < xml.length()) {
			// System.out.println(out);
			if (xml.charAt(j) == '=' && xml.charAt(j + 1) != '"') {
				out += xml.charAt(j);
				out += "\"";
				j++;
				while (xml.charAt(j) != ' ' && xml.charAt(j) != '>') {
					out += xml.charAt(j);
					j++;
				}
				out += "\" ";
				continue;
			}
			if (xml.charAt(j) == '>') {
				boolean isInput = isInput(xml, j);
				if (isInput)
					out += "/";
			}
			out += xml.charAt(j);
			j++;
		}
		return out;
	}

	public static boolean isInput(String xml, int n) {
		boolean answer = false;
		for (int i = n - 1; i >= 0; i--) {
			if (xml.charAt(i) == '<') {
				if (xml.substring(i + 1, i + 6).equals("INPUT")) {
					answer = true;
				}
				break;
			}
		}
		return answer;
	}
	
	
	static boolean check(int x, int y, int choix) {
		// check la ligne
		for (int i = 0; i < dim; i++) {
			if (i != y) {
				if (grid[x][i] == choix)
					return false;
			}
		}
		// check la colonne
		for (int i = 0; i < dim; i++) {
			if (i != x) {
				if (grid[i][y] == choix)
					return false;
			}
		}

		// chcek le carré
		int rac = (int) Math.sqrt(dim);

		int a = (x / rac) * rac;
		int b = (y / rac) * rac;

		for (int i = a; i < a + rac; i++) {
			for (int j = b; j < b + rac; j++) {
				if (i == x && j == y)
					continue;
				if (grid[i][j] == choix) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean solve(int row, int col) {
		if (col == dim)
			return true;
		
		if (row == dim)
			return solve(0, col + 1);

		if (vis[row][col])
			return solve(row + 1, col);

		for (int i = 1; i <= dim; i++) {
			if (check(row, col, i)) {
				grid[row][col] = i;
				if (solve(row + 1, col)){
					return true;
				}
				grid[row][col] = 0;
			}
		}

		return false;
	}
}