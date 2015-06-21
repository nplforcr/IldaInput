package aceProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.DocFlavor.INPUT_STREAM;
import javax.swing.text.html.parser.Entity;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import readers.SgmReader;
import xmlconversion.TagTypes;
import xmlconversion.XmlHelper;
import edu.stanford.nlp.fsm.TransducerGraph.OutputCombiningProcessor;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class IldaInput {
	// file --> word and its count
	public HashMap<Integer, HashMap<String, Integer>> dw;
	public HashMap<String, Integer> wn; // word --> sum of this word in corpus
	public HashMap<String, Integer> wmn; // word --> sum of files including
	public HashMap<Integer, String> wid; // word --> id(according to decending

	public HashMap<String, String> mt; // file --> title
	public HashMap<Integer, String[]> afid; // fileid --> authors
	public HashMap<String, Integer> an; // authors(ascending)
	public HashMap<String, String> md; // file --> postDate
	public HashMap<String, Integer> dn; // postDates(ascending)
	public HashMap<Integer, String> lfid; // fileid --> label
	public HashMap<Integer, String> lid; // lid --> label;
	public HashMap<String, Integer> ln; // labels(decending)

	public IldaInput(String dirName, String outDir) throws IOException,
			ParserConfigurationException, SAXException {
		dw = new HashMap<Integer, HashMap<String, Integer>>();
		wn = new HashMap<String, Integer>();
		wmn = new HashMap<String, Integer>();
		wid = new HashMap<Integer, String>();
		mt = new HashMap<String, String>();
		afid = new HashMap<Integer, String[]>();
		an = new HashMap<String, Integer>();
		md = new HashMap<String, String>();
		dn = new HashMap<String, Integer>();
		lfid = new HashMap<Integer, String>();
		lid = new HashMap<Integer, String>();
		ln = new HashMap<String, Integer>();
		this.process(dirName, outDir);
	}

	private void process(String dirName, String outDir) throws IOException,
			ParserConfigurationException, SAXException {
		File dFile = new File(dirName);
		File outputFile = new File(outDir);
		FileWriter fw1 = null; // nips.corpus
		FileWriter fw2 = null; // nips.vocab
		FileWriter fw3 = null; // nips.docnames
		FileWriter fw4 = null; // nips.docs
		FileWriter fw5 = null; // nips.authors.key
		FileWriter fw6 = null; // nips.authors
		FileWriter fw7 = null; // nips.vols.key
		FileWriter fw8 = null; // nips.vols
		FileWriter fw9 = null; // nips.labels.key
		FileWriter fw10 = null; // nips.labels
		if (!dFile.exists()) {
			dFile.mkdirs();
		}
		if (!outputFile.exists()) {
			outputFile.mkdirs();
		}

		if (dFile.isDirectory()) {
			File files[] = dFile.listFiles();
			fw1 = new FileWriter(outDir + "/nips.corpus.txt");
			fw2 = new FileWriter(outDir + "/nips.vocab.txt");
			fw3 = new FileWriter(outDir + "/nips.docnames.txt");
			fw4 = new FileWriter(outDir + "/nips.docs.txt");
			fw5 = new FileWriter(outDir + "/nips.authors.key.txt");
			fw6 = new FileWriter(outDir + "/nips.authors.txt");
			fw7 = new FileWriter(outDir + "/nips.vols.key.txt");
			fw8 = new FileWriter(outDir + "/nips.vols.txt");
			fw9 = new FileWriter(outDir + "/nips.labels.key.txt");
			fw10 = new FileWriter(outDir + "/nips.labels.txt");

			HashMap<String, Integer> map = null; // word ---> count
			HashMap<Integer, String> tmpMap = null; // id --> word
			int sgmId = 0;
			int fileId = 0;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					String filename = files[i].getName();
					if (filename.endsWith(".sgm")) {
						fw3.write(filename + "\r\n");
						System.out.println("sgm file name: " + filename);
						StringBuffer docBuf = SgmReader.readDoc2(dirName + "/"
								+ filename);
						if (docBuf != null) {
							// titile
							if (!SgmReader.titles.isEmpty()) {
								String title = SgmReader.titles;
								fw4.write(title + "\r\n");
								mt.put(filename, SgmReader.titles);
							}
							// authors
							if (SgmReader.authors != null) {
								Arrays.sort(SgmReader.authors);
								afid.put(sgmId, SgmReader.authors);
								for (int n = 0; n < SgmReader.authors.length; n++) {
									an.put(SgmReader.authors[n], sgmId);
								}
							}
							// date
							if (!SgmReader.date.isEmpty()) {
								md.put(filename, SgmReader.date);
								dn.put(SgmReader.date, sgmId);
							}
							// label
							if (!SgmReader.label.isEmpty()) {
								lfid.put(sgmId, SgmReader.label);
								if (!ln.containsKey(SgmReader.label)) {
									ln.put(SgmReader.label, 1);
								} else {
									int count = ln.get(SgmReader.label);
									count++;
									ln.put(SgmReader.label, count);
								}
							}
							sgmId++;
						}
					} else if (filename.endsWith(".apf.xml")) {
						System.out.println("apf.xml name: " + filename);
						HashMap<String, Integer> mp = processDocument(files[i]
								.getAbsolutePath());
						dw.put(fileId, mp);
						for (Map.Entry<String, Integer> entry : mp.entrySet()) {
							String str = entry.getKey();
							if (wmn.containsKey(str)) {
								int count = wmn.get(str);
								wmn.put(str, count + 1);
							} else {
								wmn.put(str, 1);
							}
						}
						fileId++;
					} else {
						System.out.println("file is useless!");
						continue;
					}
				}
				System.out.println("\n");
			}
			fw3.close();
			fw4.close();
			/*-------------- -----nips.corpus----  --------------------*/
			sortByCount(wn, wid);
			for (int i = 0; i < dw.size(); i++) {
				HashMap<String, Integer> m = dw.get(i);
				fw1.write(dw.get(i).size() + " ");
				for (Entry<Integer, String> entry : wid.entrySet()) {
					String str = entry.getValue();
					if (m.containsKey(str)) {
						fw1.write(entry.getKey() + ":" + m.get(str) + " ");
					}
				}
				fw1.write("\r\n");
			}
			fw1.close();
			/*----------------------nips.vocab-----------------------*/
			for (int i =0; i < wid.size(); i++) {
				String str = "";
				String tmp = wid.get(i);
				String[] split = tmp.split("(\r\n|\r|\n|\n\r)");                
				for(String s:split)
				{
				     str += s;
				}
				int count = wn.get(tmp);
				int countm = wmn.get(tmp);
				fw2.write(str + " = " + i + " = " + count + " " + countm
						+ "\r\n");
			}

			fw2.close();
			/*------------------nips.authors.key-----------------------*/
			String[] sa = sortStringArray(an);
			for (int i = 0; i < sa.length; i++) {
				fw5.write(sa[i] + "\r\n");
			}
			fw5.close();
			/*-------------------nips.authors---------------------------*/
			for (Entry<Integer, String[]> entry : afid.entrySet()) {
				String[] tmp = entry.getValue();
				for (int i = 0; i < tmp.length; i++) {
					fw6.write(an.get(tmp[i]) + " ");
				}
				fw6.write("\r\n");
			}
			fw6.close();
			/*-------------------nips.vols.key--------------------------*/
			sa = sortStringArray(dn);
			for (int i = 0; i < sa.length; i++) {
				fw7.write("NIPS " + i + "," + sa[i] + "\r\n");
			}
			fw7.close();
			/*-------------------nips.vols-----------------------------*/
			for (Entry<String, String> entry : md.entrySet()) {
				String tmp = entry.getValue();
				fw8.write(dn.get(tmp) + "\r\n");
			}
			fw8.close();
			/*------------------nips.labels.key------------------------*/
			sortByCount(ln, lid);
			for (Entry<Integer, String> entry : lid.entrySet()) {
				fw9.write(entry.getValue() + "\r\n");
			}
			fw9.close();
			/*------------------nips.labels---------------------------*/
			for (int i = 0; i < lfid.size(); i++) {
				for (int j = 0; j < lid.size(); j++) {
					if (lid.get(j).equals(lfid.get(i))) {
						fw10.write(j + "\r\n");
						break;
					}
				}
			}
			fw10.close();
		}
	}

	private void sortByCount(HashMap<String, Integer> m1,
			HashMap<Integer, String> m2) {
		int id = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.putAll(m1);
		while (!map.isEmpty()) {
			int maxCount = 0;
			String str = "";
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() > maxCount) {
					str = entry.getKey();
					maxCount = entry.getValue();
				}
			}
			map.remove(str);
			m2.put(id, str);
			id++;
		}
	}

	private String[] sortStringArray(HashMap<String, Integer> m) {
		String[] sa = new String[m.size()];
		int count = 0;
		for (Map.Entry<String, Integer> entry : m.entrySet()) {
			sa[count++] = entry.getKey();
		}
		Arrays.sort(sa);
		for (int i = 0; i < count; i++) {
			m.put(sa[i], i);
		}
		return sa;
	}

	private HashMap<String, Integer> processDocument(String annotFname)
			throws ParserConfigurationException, SAXException, IOException {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new File(annotFname));
		NodeList entList = document.getElementsByTagName(TagTypes.ENT);

		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (int i = 0; i < entList.getLength(); i++) {
			NodeList mentionList = entList.item(i).getChildNodes();
			for (int j = 0; j < mentionList.getLength(); j++) {
				Node childN = mentionList.item(j);
				if (childN.getNodeName().equals(TagTypes.ENTMEN)) {
					Node headNode = XmlHelper.getFirstChildByTagName(childN,
							TagTypes.EXTENT);
					Node entity = XmlHelper.getFirstChildByTagName(headNode,
							TagTypes.CHARSEQ);
					String head = entity.getTextContent();
					// single file
					if (!ret.containsKey(head)) {
						ret.put(head, 1);
					} else {
						int count = ret.get(head);
						ret.put(head, count + 1);
					}
					// all files
					if (!wn.containsKey(head)) {
						wn.put(head, 1);
					} else {
						int count = wn.get(head);
						wn.put(head, count + 1);
					}
				}
			}
		}
		return ret;
	}

	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		String inputDir = "input";
		String outputDir = "output";
		IldaInput obj = new IldaInput(inputDir, outputDir);
	}

}
