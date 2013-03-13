package DSProject;

import java.util.LinkedList;

/* Encapsulates an article */
public class Article {
	private int id;
	private String title = "";
	private String content = "";
	
	LinkedList<Article> replies = null;
	

	public Article(int id, String title, String content) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.replies = new LinkedList<Article>();
	}

	public int getID(){ return id; }
	public String getTitle(){ return title; }
	public String getContent() {return content; }
	public LinkedList<Article> getReplies() { return replies; }

	public void setID(int id){ this.id = id; }
	public void setTitle(String title){this.title = title; }
	public void setContent(String content){ this.content = content; }
	
	public String partialArticle() {
		String string = "";
		string = id + ". "+title+": "+content.substring(0, 50)+"...";
		return string;
	}
	
	public String completeArticle(){
		String string = "";
		string = id + ". "+title+":\n "+content;
		return string;
	}

	public boolean equals(Object o) {
		Article a = (Article) o;		
		return id == a.getID();
	}
}
