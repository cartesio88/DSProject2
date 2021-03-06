package DSProject;


import java.util.Iterator;
import java.util.LinkedList;

public class BulletinBoard {
	LinkedList<Article> articles;
	private int version=0;
	
	public int GetVersion(){
		return version;
	}
	
	public BulletinBoard(){
		articles = new LinkedList<Article>();
	}
	
	public void addArticle(Article a){
		articles.add(a);
		version++;
	}
	
	public String ReadArticlesList(){
		return ReadArticlesList(articles, 0);
	}
	
	private String ReadArticlesList(LinkedList<Article> articles, int tab){
		String list = "";
		
		// Generates the preview of articles
		Iterator<Article> it = articles.iterator();
		while(it.hasNext()){
			Article a = it.next();
			for(int i=0; i<tab; i++) list += "   ";
			list += a.partialArticle() + "\n";
			list += ReadArticlesList(a.getReplies(), tab+1);			
		}
		return list;
	}
	
	public Article getArticle(int id){
		return getArticle(articles, id);
	}
	
	private Article getArticle(LinkedList<Article> articles, int id){
		Iterator<Article> it = articles.iterator();
		while(it.hasNext()){
			Article a = it.next();
			if(a.getID() == id) return a;
			a = getArticle(a.getReplies(), id);
			if(a != null) return a;
		}
		//System.out.println("Could not find the selected article...");
		return null;
	}
	
	public boolean reply(int responseId, int postId, String content){
		Article mainArticle = getArticle(postId);
		if(mainArticle == null){
			System.out.println("[BulletinBoard] Can not find article with id: "+postId);
			return false;
		}else{
			Article response = new Article(responseId, postId, "Re: "+mainArticle.getTitle(), content);
			mainArticle.getReplies().add(response);
			version++;
		}
		return true;
	}
}
