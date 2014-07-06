package com.lucidworks.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


public class AutoPhrasingQParserPlugin extends QParserPlugin implements ResourceLoaderAware {
	
  private static final Logger Log = LoggerFactory.getLogger( AutoPhrasingQParserPlugin.class );
  private CharArraySet phraseSets;
  private String phraseSetFiles;
  
  private String parserImpl = "lucene";
	
  @Override
  public void init( NamedList initArgs ) {
    Log.info( "init ..." );
	SolrParams params = SolrParams.toSolrParams(initArgs);
	phraseSetFiles = params.get( "phrases" );
	
	String pImpl = params.get( "defType" );
	if (pImpl != null) {
	  parserImpl = pImpl;
	}
  }

  @Override
  public QParser createParser( String qStr, SolrParams localParams, SolrParams params,
			                   SolrQueryRequest req) {
    Log.info( "createParser" );
	ModifiableSolrParams modparams = new ModifiableSolrParams( params );
	String modQ = filter( qStr );

	modparams.set( "q", modQ );
	return req.getCore().getQueryPlugin( parserImpl )
	                    .createParser(modQ, localParams, modparams, req);
  }

  private String filter( String qStr ) {	
    // 1) collapse " :" to ":" to protect field names
    // 2) expand ":" to ": " to free terms from field names
    // 3) expand "+" to "+ " to free terms from "+" operator
    // 4) expand "-" to "- " to free terms from "-" operator
	// 5) Autophrase with whitespace tokenizer
	// 6) collapse "+ " and "- " to "+" and "-" to glom operators.
		
    String query = qStr;
    while( query.contains( " :" ))
      query = query.replaceAll( "\\s:", ": " );

    query = query.replaceAll( "\\+", "+ " );
    query = query.replaceAll( "\\-", "- " );
        
    try {
      query = autophrase( query );
    }
    catch (IOException ioe ) {  }
        
    query = query.replaceAll( "\\+ ", "+" );
    query = query.replaceAll( "\\- ", "-" );
		
    return query;
  }
	
  private String autophrase( String input ) throws IOException {
    WhitespaceTokenizer wt = new WhitespaceTokenizer(Version.LUCENE_46, new StringReader( input ));
    LowerCaseFilter lcf = new LowerCaseFilter(Version.LUCENE_46, wt );
    AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( Version.LUCENE_46, lcf, phraseSets, false );
    aptf.setReplaceWhitespaceWith( new Character( 'x' ) );
    CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
    aptf.reset();
        
    StringBuffer strbuf = new StringBuffer( );
    while( aptf.incrementToken( )) {
      strbuf.append( term.toString( ) ).append( " " );
    }
        
    return strbuf.toString();
  }

  @Override
  public void inform(ResourceLoader loader) throws IOException {
    if (phraseSetFiles != null) {
      phraseSets = getWordSet(loader, phraseSetFiles, true );
    }
  }
	
  private CharArraySet getWordSet( ResourceLoader loader,
		                           String wordFiles, boolean ignoreCase)
		                           throws IOException {
    List<String> files = splitFileNames(wordFiles);
	CharArraySet words = null;
    if (files.size() > 0) {
      // default stopwords list has 35 or so words, but maybe don't make it that
      // big to start
      words = new CharArraySet( Version.LUCENE_46,
    	                        files.size() * 10, ignoreCase);
      for (String file : files) {
        List<String> wlist = getLines(loader, file.trim());
    	words.addAll(StopFilter.makeStopSet(Version.LUCENE_46, wlist, ignoreCase));
      }
    }
    return words;
  }
	
  private List<String> getLines(ResourceLoader loader, String resource) throws IOException {
	return WordlistLoader.getLines(loader.openResource(resource), StandardCharsets.UTF_8);
  }

  private List<String> splitFileNames(String fileNames) {
    if (fileNames == null)
      return Collections.<String>emptyList();

    List<String> result = new ArrayList<>();
    for (String file : fileNames.split("(?<!\\\\),")) {
      result.add(file.replaceAll("\\\\(?=,)", ""));
    }

    return result;
  }
}