package tripattern;

public class CodeTable {
	
	private String WeakCharacters[];

	private String SemiStrongCharacters[];
	
	public void setTheWeakCha(String paraWeakCharacters[]){
		WeakCharacters = new String[paraWeakCharacters.length];
		for(int i = 0; i < WeakCharacters.length; i ++){
			WeakCharacters[i] = paraWeakCharacters[i];
		}
	}
	
	public void setTheSemiStrongCha(String paraSemiStrongCharacters[]){
		SemiStrongCharacters = new String[paraSemiStrongCharacters.length];
		for(int i = 0; i < paraSemiStrongCharacters.length; i ++){
			SemiStrongCharacters[i] = paraSemiStrongCharacters[i];
		}
	}
	
	public boolean isCharSemi(String paraChar){
		String SpecialSymbol = "~";
		if(SemiStrongCharacters[0].equals(SpecialSymbol)) return true;
	
		for(int i = 0; i < SemiStrongCharacters.length; i ++){
			if(paraChar.equals(SemiStrongCharacters[i])) return true;
		}
		
		return false;
	}

	public boolean isCharStrong(String paraChar){
		
		for(int i = 0 ;i < WeakCharacters.length; i ++){
			if(paraChar.equals(WeakCharacters[i])) return false;
		}
		
		for(int i = 0 ;i < SemiStrongCharacters.length; i ++){
			if(paraChar.equals(SemiStrongCharacters[i])) return false;
		}
		
		return true;
	}

}
