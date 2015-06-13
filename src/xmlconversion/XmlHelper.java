package xmlconversion;

/*
 * @author Dingcheng Li lidi00000
 */

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper {
	public static Node getFirstChildByTagName(Node parent, String tagName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equalsIgnoreCase(tagName))
				return node;
		}
		return null;
	}
}