package DSProject;

public class UpdateOperation {
	private String type;
	private Article article;
	
	public UpdateOperation(Article article, String type){
		if(!type.equalsIgnoreCase("Post") && !type.equalsIgnoreCase("Response")){
			System.out.println("ERROR Building an update operation");
		}
		this.article =  article;
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	public Article getArticle(){
		return article;
	}
	
	public String toString(){
		String string = "[Update Operation] ";
		
		if(type.equalsIgnoreCase("Post")){
			string += "Post: ";
		}else if(type.equalsIgnoreCase("Response")){
			string += "Response: ";
		}
		
		string += article.partialArticle();
		
		return string;
	}
}
