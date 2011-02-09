package com.github.eclipsecolortheme.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.eclipsecolortheme.ColorThemeMapping;
import com.github.eclipsecolortheme.ColorThemeSetting;
import com.github.eclipsecolortheme.ColorThemeSemanticHighlightingMapping;

public class GenericMapper extends ThemePreferenceMapper {
	
    private Map<String, ColorThemeMapping> mappings = new HashMap<String, ColorThemeMapping>();

    public GenericMapper(String pluginId) {
        super(pluginId);
        InputStream input =  Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/github/eclipsecolortheme/mappings/"
                                     + pluginId + ".xml");
        try {
            parseMapping(input);
        } catch (Exception e) {
            System.err.println("Failed to parse mapping for " + pluginId);
            e.printStackTrace();
        }
    }

    private void parseMapping(InputStream input)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(input);
        Element root = document.getDocumentElement();
        parseMappings(root);
        parseSemanticHighlightingMappings(root);
    }
    
    private void parseMappings(Element root) {
    	Node mappingsNode = root.getElementsByTagName("mappings").item(0);
    	NodeList mappingNodes = mappingsNode.getChildNodes();
        for (int i = 0; i < mappingNodes.getLength(); i++) {
            Node mappingNode = mappingNodes.item(i);
            if (mappingNode.hasAttributes()) {
            	String pluginKey = extractAttribute(mappingNode, "pluginKey");
            	String themeKey = extractAttribute(mappingNode, "themeKey");
            	mappings.put(pluginKey, createMapping(pluginKey, themeKey));
            }
        }
    }
    
    private void parseSemanticHighlightingMappings(Element root) {
    	Node mappingsNode = root.getElementsByTagName("semanticHighlightingMappings").item(0);
    	if (mappingsNode != null) {
	    	NodeList mappingNodes = mappingsNode.getChildNodes();
	        for (int i = 0; i < mappingNodes.getLength(); i++) {
	            Node mappingNode = mappingNodes.item(i);
	            if (mappingNode.hasAttributes()) {
	            	String pluginKey = extractAttribute(mappingNode, "pluginKey");
	            	String themeKey = extractAttribute(mappingNode, "themeKey");
	            	mappings.put(pluginKey, createSemanticHighlightingMapping(pluginKey, themeKey));
	            }
	        }
    	}
    }
    
    protected ColorThemeMapping createMapping(String pluginKey, String themeKey) {
    	return new ColorThemeMapping(pluginKey, themeKey);
    }

    protected ColorThemeSemanticHighlightingMapping createSemanticHighlightingMapping(String pluginKey, String themeKey) {
    	return new ColorThemeSemanticHighlightingMapping(pluginKey, themeKey);
    }
    
    private static String extractAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    @Override
    public void map(Map<String, ColorThemeSetting> theme) {
    	
    	// Add those text editor specific dependencies
    	// TODO: bad location, move to somewhere else...
    	preferences.putBoolean("AbstractTextEditor.Color.Background.SystemDefault", false);
    	preferences.putBoolean("AbstractTextEditor.Color.Foreground.SystemDefault", false);
    	preferences.putBoolean("AbstractTextEditor.Color.SelectionBackground.SystemDefault", false);
    	preferences.putBoolean("AbstractTextEditor.Color.SelectionForeground.SystemDefault", false);
    	
    	// put preferences according to mappings
    	for (String pluginKey : mappings.keySet()) {
    		ColorThemeMapping mapping = mappings.get(pluginKey);
    		ColorThemeSetting setting = theme.get(mapping.getThemeKey());
    		if (setting != null) {
    			mapping.putPreferences(preferences, setting);
    		}
        }
    	
    }

    @Override
    public void clear() {
        for (String pluginKey : mappings.keySet()) {
        	ColorThemeMapping mapping = mappings.get(pluginKey);
            mapping.removePreferences(preferences);
        }
    }
}
