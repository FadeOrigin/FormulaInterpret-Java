# FormulaInterpret-Java
Interpret a plain text formula, parse variables, then get result.

four operator supported:
1:+
2:-
3:*
4:/
parentheses are supported

Sample Code:

Formula formula=new Formula("(Var1+Var2)*2+Var3*5"); //instantialize a formula object
HashMap<String,String> variableCollection=new HashMap<String,String>(); //create a variable name-value Map
variableCollection.put("Var1","10");
variableCollection.put("Var2","20");
variableCollection.put("Var3","30");
Float resultFloat=formula.resultAsFloat(variableCollection);  //get float result
Integer resultInteger=formula.resultAsInteger(variableCollection);  //get integer result
