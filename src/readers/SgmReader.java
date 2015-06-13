package readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SgmReader {
	public static String[] authors = null;
	public static String titles = null;
	public static String date = null;
	public static String label = null;

	public static int[] findByteSpan(StringBuffer textBuf, String phrase,
			int index) {
		int[] bytespan = new int[2];
		int start = 0, end = 0;
		// System.out.println("phrase in findByteSpan: "+phrase);
		String[] phraseArray = phrase.split(" ");
		if (phrase.isEmpty()) {
			bytespan[0] = start;
			bytespan[1] = end;
			return bytespan;
		} else {
			if (index == 0) {
				start = textBuf.indexOf(phraseArray[0]);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
			} else {
				start = textBuf.indexOf(phraseArray[0], index);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
				// System.out.println("from index in findBytesSpan: "+ index);
			}
		}
		// System.out.println("start and end: "+start+" "+end);

		bytespan[0] = start;
		bytespan[1] = end;
		return bytespan;
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc(String docName) {
		File inputFile = new File(docName);
		System.out.println(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				sbAce.append(child.getTextContent());
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbAce;
	}

	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList list = new ArrayList();
		String line;
		while ((line = in.readLine()) != null) {
			list.add(line);
		}
		return (String[]) list.toArray(new String[0]);
	}

	/**
	 * method to convert a byte to a hex string.
	 *
	 * @param data
	 *            the byte to convert
	 * @return String the converted byte
	 */
	public static String byteToHex(byte data) {

		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));

		return buf.toString();
	}

	/**
	 * Convenience method to convert an int to a hex char.
	 *
	 * @param i
	 *            the int to convert
	 * @return char the converted char
	 */
	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc2(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			try {
				try {
					InputStream is = new FileInputStream(inputFile);
					byte[] bytes = new byte[(int) inputFile.length()];
					// Read in the bytes
					int offset = 0;
					int numRead = 0;
					while ((offset < bytes.length)
							&& ((numRead = is.read(bytes, offset, bytes.length
									- offset)) >= 0)) {

						offset += numRead;

					}
					// Ensure all the bytes have been read in
					if (offset < bytes.length) {
						throw new IOException("Could not completely read file "
								+ inputFile.getName());
					}
					is.close();
					String byteStr = new String(bytes).replace("&", ":");
					// System.out.println(byteStr);
					builder = factory.newDocumentBuilder();
					document = builder.parse(new InputSource(new StringReader(
							byteStr)));
					Node child = document.getFirstChild();
					sbAce.append(child.getTextContent());
					// -------------------author, title, time,
					// type------------------//
					if (document.getElementsByTagName("POSTER").getLength() > 0) {
						String author = document.getElementsByTagName("POSTER")
								.item(0).getTextContent();
						String[] tmp = author.split(" ");
						int len = tmp.length;
						for (int i = 0; i < tmp.length; i++) {
							tmp[i].replaceAll(" ", "");
							if (tmp[i].isEmpty())
								len--;
						}
						if (len != 0)
							authors = new String[len];
						for (int i = 0, j = 0; i < tmp.length; i++) {
							if (!tmp[i].isEmpty()) {
								authors[j] = tmp[i];
								j++;
							}
						}
					}
					if (document.getElementsByTagName("HEADLINE").getLength() > 0) {
						String title = document
								.getElementsByTagName("HEADLINE").item(0)
								.getTextContent();
						titles = title.replaceAll("\\n", "");
					}

					if (document.getElementsByTagName("DATETIME").getLength() > 0) {
						String postDate = document
								.getElementsByTagName("DATETIME").item(0)
								.getTextContent();
						date = postDate.split("-")[0];
					}
					if (document.getElementsByTagName("DOCTYPE").getLength() > 0) {
						label = document.getElementsByTagName("DOCTYPE")
								.item(0).getTextContent();
						label = label.trim();
					}

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbAce;
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc3(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			BufferedReader brXml;
			try {
				try {
					brXml = new BufferedReader(new FileReader(inputFile));
					StringBuffer sbXml = new StringBuffer();
					String line = "";
					byte[] bytes = new byte[(int) inputFile.length()];

					while ((line = brXml.readLine()) != null) {
						/*
						 * if (line.startsWith("time")) { line.replace("time",
						 * "time/"); continue; } else
						 */
						if (line.contains("&")) {
							line.replace("&", ":");
							sbXml.append(line);
						} else {
							sbXml.append(line);
						}
					}
					System.out.println(sbXml.toString());

					builder = factory.newDocumentBuilder();

					document = builder.parse(new InputSource(new StringReader(
							sbXml.toString())));
					Node child = document.getFirstChild();
					sbAce.append(child.getTextContent());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbAce;
	}

}
