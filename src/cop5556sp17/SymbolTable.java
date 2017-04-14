package cop5556sp17;


import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Type.TypeName;

import java.util.*;


public class SymbolTable {
	
	class Symbol{
		
		String name;
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		int scopeValue;
		
		public int getScopeValue() {
			return scopeValue;
		}

		public void setScopeValue(int scopeValue) {
			this.scopeValue = scopeValue;
		}

		Dec decNode;
		public Dec getDecNode() {
			return decNode;
		}

		public void setDecNode(Dec decNode) {
			this.decNode = decNode;
		}

		Symbol next;
		
		public Symbol(String name, int scopeValue, Dec decNode) {
			this.name = name;
			this.scopeValue = scopeValue;
			this.decNode = decNode;
		}
		
	}
	
	
	//TODO  add fields
	Map<String,Symbol> symbolMap;
	Stack<Integer> scopeStack;
	int currScope,nextScope;

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		currScope = nextScope++;
		scopeStack.push(currScope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		int scope = scopeStack.pop();
		if(scope==0){
			currScope = scope;
		}
		else{
			currScope = scopeStack.peek();
		}
	}
	
	public boolean insert(String ident, Dec dec){
		Symbol newSymbol = new Symbol(ident,currScope,dec);
		Symbol currSymbol,headSymbol;
		if(symbolMap.containsKey(ident)){
			headSymbol = symbolMap.get(ident);
			currSymbol = headSymbol;
			while(currSymbol!=null){
				int scope = currSymbol.getScopeValue();
				if(scope==currScope){
					return false;
				}
				currSymbol = currSymbol.next;
			}
			newSymbol.next = headSymbol;
		}
		symbolMap.put(ident, newSymbol);
		return true;
	}
	
	public Dec lookup(String ident){
		
		if(symbolMap.containsKey(ident)){
			Symbol node = symbolMap.get(ident);
			Symbol currNode;
			List<Integer> scopesList = new ArrayList(scopeStack);
			for(int i=scopesList.size()-1;i>=0;i--){
				currNode = node;
				while(currNode!=null){
					if(currNode.scopeValue ==scopesList.get(i) && currNode.getName().equals(ident)){
						return currNode.decNode;
					}
					currNode = currNode.next;
				}
			}
		}
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		symbolMap = new HashMap();
		scopeStack = new Stack();
		currScope=0;
		nextScope=0;
		
	}


	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		symbolMap.forEach((k,v) -> {
			Symbol curr = v;
			sb.append("Symbol: " + curr.name);
			while(curr != null){
				sb.append("    Scope no.: " + curr.scopeValue);
				curr = curr.next;	
			}
			sb.append("xxxxxxxxxxxxxxxxxxx");
		});
		return sb.toString();
	}
	

}
