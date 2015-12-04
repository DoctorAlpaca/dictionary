package de.eric_wiltfang.dictionary.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;

import de.eric_wiltfang.dictionary.Entry;
import de.eric_wiltfang.dictionary.Exporter;

public class HTMLExporter implements Exporter {
	private String langName;
	private OutputStreamWriter writer;
	private StringBuilder wordBuilder;
	private StringBuilder definitionBuilder;
	private StringBuilder notesBuilder;
	private StringBuilder tagsBuilder;
	private StringBuilder categoryBuilder;
	private String exampleWord;
	private Random r;

	public HTMLExporter(File target) throws IOException {
		writer = new OutputStreamWriter(new FileOutputStream(target), Charset.forName("UTF-8"));
		
		wordBuilder = new StringBuilder();
		wordBuilder.append("var words=[");
		definitionBuilder = new StringBuilder();
		definitionBuilder.append("var definitions=[");
		notesBuilder = new StringBuilder();
		notesBuilder.append("var notes=[");
		tagsBuilder = new StringBuilder();
		tagsBuilder.append("var tags=[");
		categoryBuilder = new StringBuilder();
		categoryBuilder.append("var categories=[");
		
		r = new Random();
	}
	
	@Override
	public void start(String langName) throws IOException {
		this.langName = langName;
		
		// Don't judge me
		String intro = "<!DOCTYPE>\n<html>\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n<title>" + langName + " Dictionary</title>\n<style type=\"text/css\">html{background-color:#b7b7dc}body{margin:25px 10% 25px 10%;padding-bottom:25px;-webkit-box-shadow:1px 1px 20px 1px #000;box-shadow:1px 1px 20px 1px #000;background-color:white;-webkit-border-radius:10px;border-radius:10px}h1{padding-top:50px;padding-bottom:30px;text-align:center}#searchContainer{vertical-align:center;background-color:#bae8ba;margin-left:10%;margin-right:10%;padding:5px 5% 5px 5%;border:1px solid #6bbc6b;-webkit-border-radius:5px;border-radius:5px;vertical-align:center}#searchBox{width:65%}input[type=\"button\"]{width:15%}.info{text-align:center;padding:20px;margin:50px}.entry{margin:50px 10% 50px 10%}.word{font-size:2.5em;margin-top:10px;margin-bottom:2px}.category{color:#6868a5;margin-top:2px;margin-bottom:2px;display:inline}.tags{list-style-type:none;margin:0;padding:0;display:inline}.tags li{background-color:#bd6b99;border:1px solid #1f0012;-webkit-border-radius:10px;border-radius:10px;padding-left:5px;padding-right:5px;color:white;display:inline}.definition{padding-left:20px}.notes{padding-left:20px;font-style:italic}</style>\n<script type=\"text/javascript\">/*<![CDATA[*/window.onload=function(){document.getElementById(\"searchButton\").onclick=search;document.getElementById(\"searchWordButton\").onclick=searchWord;document.getElementById(\"searchBox\").onkeydown=function(a){if(a.keyCode==13){if(a.ctrlKey){search()}else{searchWord()}}}};";
		writer.write(intro);
	}

	/**
	 * Escapes the string so it can be inserted without causing problems.
	 */
	private String escape(String s) {
		return StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(s));
	}
	@Override
	public void addEntry(Entry entry) throws IOException {
		if (exampleWord == null) {
			exampleWord = entry.getWord();
		}
		if (r.nextInt(100) == 1) {
			exampleWord = entry.getWord();
		}
		
		wordBuilder.append("'" + escape(entry.getWord()) + "',");
		definitionBuilder.append("'" + escape(entry.getDefinition()) + "',");
		notesBuilder.append("'" + escape(entry.getNotes()) + "',");
		categoryBuilder.append("'" + escape(entry.getCategory()) + "',");
		
		tagsBuilder.append("[");
		for (String tag : entry.getTags()) {
			tagsBuilder.append("'" + escape(tag) + "',");
		}
		tagsBuilder.append("],");
	}

	@Override
	public void finish() throws IOException {
		wordBuilder.append("];");
		writer.write(wordBuilder.toString());
		definitionBuilder.append("];");
		writer.write(definitionBuilder.toString());
		notesBuilder.append("];");
		writer.write(notesBuilder.toString());
		tagsBuilder.append("];");
		writer.write(tagsBuilder.toString());
		categoryBuilder.append("];");
		writer.write(categoryBuilder.toString());
		
		String outro = "function search(){var b=document.getElementById(\"searchBox\").value;if(b==\"\"){return}document.getElementById(\"entries\").innerHTML=\"\";for(var a=0;a<words.length;a++){if(matchesEntry(a,b)){document.getElementById(\"entries\").appendChild(generateEntry(a))}}}function searchWord(){var d=document.getElementById(\"searchBox\").value.toLowerCase();if(d==\"\"){return}document.getElementById(\"entries\").innerHTML=\"\";var g=0;var c=words.length-1;var e=-1;while(g<=c){var a=(g+c)/2|0;var f=words[a].toLowerCase();if(matcheword(f,d)){e=a;break}else{if(f<d){g=a+1}else{c=a-1}}}if(e<0){return}while(e-1>=0&&matcheword(words[e-1].toLowerCase(),d)){e--}for(var b=e;b<words.length&&matcheword(words[b].toLowerCase(),d);b++){document.getElementById(\"entries\").appendChild(generateEntry(b))}}function matchesEntry(a,b){return words[a].toLowerCase().indexOf(b.toLowerCase())!=-1||definitions[a].toLowerCase().indexOf(b.toLowerCase())!=-1}function matcheword(b,a){return b.indexOf(a)==0}function generateEntry(b){var h=document.createElement(\"div\");h.className=\"entry\";var g=document.createElement(\"p\");g.innerHTML=words[b];g.className=\"word\";h.appendChild(g);var d=document.createElement(\"p\");d.innerHTML=categories[b];d.className=\"category\";h.appendChild(d);var e=document.createElement(\"ul\");for(var f=0;f<tags[b].length;f++){var c=document.createElement(\"li\");c.innerHTML=tags[b][f];e.appendChild(c)}e.className=\"tags\";h.appendChild(e);var j=document.createElement(\"p\");j.innerHTML=definitions[b];j.className=\"definition\";h.appendChild(j);var a=document.createElement(\"p\");a.innerHTML=notes[b];a.className=\"notes\";h.appendChild(a);return h};/*]]>*/</script>\n</head>\n<body>\n<h1>" + langName + " Dictionary</h1>\n<div id=\"searchContainer\">\n<input type=\"text\" id=\"searchBox\"/>\n<input type=\"button\" id=\"searchWordButton\" value=\"Search\" title=\"Or just press enter while typing\"/>\n<input type=\"button\" id=\"searchButton\" value=\"Search fulltext\" title=\"Or just press ctrl+enter while typing\"/>\n</div>\n<div id=\"entries\">\n<div class=\"info\">\nEnter something above to start searching! For example the word \"" + escape(exampleWord) + "\".\n</div>\n</body>\n</html>";
		writer.write(outro);
		
		writer.flush();
		writer.close();
	}

	@Override
	public EnumSet<ExporterSettings> getSettings() {
		return EnumSet.of(ExporterSettings.ALPHABETICAL);
	}

}
