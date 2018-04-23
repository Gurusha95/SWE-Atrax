package application;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.util.ArrayList;
import java.io.*;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

public class FileMetaData {
	
	private String Author;
	private String Title;
	private String Subject;
	private String Keywords;
	private File file;
	private PDDocument Document;
	private PDDocumentInformation PDoc;
	
	public FileMetaData(File File) throws IOException
	{
		file = File;
		Document = PDDocument.load(file);
		PDoc = Document.getDocumentInformation();
		Author = "NO AUTHOR";
		Title = "NO TITLE";
		Subject = "NO SUBJECT";
	}
	
	public void ExtractMetaData() throws IOException
	{
		
		//to write new keywords into bufferedWriter
		List<String> Writer = new ArrayList<String> ();
		//To save the keywords in the file
		BufferedReader in = null; 
		BufferedWriter out = null;
		String[]  Keyword_ExtractText;
		InputStream Stream_input = new FileInputStream("en-pos-maxent.bin");
		
		POSModel model = new POSModel(Stream_input);
		POSTaggerME tagger = new POSTaggerME(model);
		
		WhitespaceTokenizer whitespaceTokenizer = WhitespaceTokenizer.INSTANCE;
		String[] tokens, keywordTags;
		String KeywordExtract_Text;
		int occurance =0;
		List<String> MetaData_Keyword = new ArrayList<String> (); 
		List<String> tag_keyword = new ArrayList<String> ();
		
		if(PDoc.getKeywords() == null)
		{
			in = new BufferedReader(new FileReader("Keywords.txt"));
			String Line;
			while((Line = in.readLine()) != null)
				{
					if(file.getName().equals(Line))
					{
						Line = in.readLine();
						PDoc.setKeywords(Line);
						System.out.println(PDoc.getKeywords());
						break;
					}
				}
				in.close();
			
			if(PDoc.getKeywords() == null)
			{
			//	
				KeywordExtract_Text = new PDFTextStripper().getText(Document);
				
				tokens = whitespaceTokenizer.tokenize(KeywordExtract_Text);
				keywordTags = tagger.tag(tokens);
				
				InputStream Stream = new FileInputStream("en-sent.bin");
				SentenceModel Model = new SentenceModel(Stream);
				SentenceDetectorME Detector = new SentenceDetectorME(Model);
				
				Keyword_ExtractText = Detector.sentDetect(KeywordExtract_Text);
				
				
				for(int index =0;index < keywordTags.length;index++)
				{
					if((keywordTags[index].equals("NN"))| (keywordTags[index].equals("VB")) |(keywordTags[index].equals("NNS")) |(keywordTags[index].equals("NNP")))
					{
						tag_keyword.add(tokens[index]);
					}
				}
				
				for(int index1 =0;index1 < tag_keyword.size();index1++)
				{
					for(int index2 =0;index2 < Keyword_ExtractText.length;index2++)
					{
					if(Keyword_ExtractText[index2].contains(tag_keyword.get(index1)))
						{
							occurance++;
							if(occurance >= 20)
							{
								if(!MetaData_Keyword.contains(tag_keyword.get(index1)))
								{
								MetaData_Keyword.add(tag_keyword.get(index1));
								occurance = 0;
								break;
							}
							}
						}
					}
				}
				
					PDoc.setKeywords( MetaData_Keyword.toString());
					Writer.add(file.getName());
					Writer.add(PDoc.getKeywords());

					MetaData_Keyword.clear();
					System.out.println(PDoc.getKeywords());
			}
		}
		
		if(PDoc.getAuthor() != null)
		{
			Author = PDoc.getAuthor();
		}
		
		if(PDoc.getKeywords() != null)
		{
			Keywords = PDoc.getKeywords();
		}
		
		if(PDoc.getSubject() != null)
		{
			Author = PDoc.getSubject();
		}
		
		if(PDoc.getTitle() != null)
		{
			Keywords = PDoc.getTitle();
		}
		
		out = new BufferedWriter(new FileWriter("Keywords.txt",true));
		for(int i=0;i<Writer.size();i++)
		{
			out.append(Writer.get(i));
			out.newLine();
		}
		out.close();
	//	Writer.clear();
	    Document.close();
		
	}
	
	public String getKeyword()
	{
		return Keywords;
	}
	
	public String getTitle()
	{
		return Title;
	}
	
	public String getSubject()
	{
		return Subject;
	}
	
	
	public String getAuthor()
	{
		return Author;
	}
	
	public String getName()
	{
		return file.getName();
	}
	

}
