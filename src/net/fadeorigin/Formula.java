package net.fadeorigin;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**  powered by fadeOrigin **/
public class Formula {


	private String formula;
	private HashMap<String,Float> variableCollection;
	private ArrayList<Object[]> operationCollection;

	public Formula(String formula_Parameter) throws IOException {
		this.formula=formula_Parameter.replace(" ","");
        //
        //turn formula to data structure
		String operators="+\\-\\*/";
		String formulaInterpretRegex="(["+operators+"]{0,1})([^"+operators+"]+)";
		Pattern pattern=Pattern.compile(formulaInterpretRegex);
        this.operationCollection=this.convertToOperationList(pattern);
	}

	//
    //output as float
	public Float resultAsFloat(HashMap<String,String> variableCollection){
	    String resultString=doTheJob(variableCollection);
        return Float.parseFloat(resultString);
    }

    //
    //output as integer,ignore decimal part
    public int resultAsInteger(HashMap<String,String> variableCollection){
        String resultString=doTheJob(variableCollection);
        return Integer.parseInt(resultString.substring(0,resultString.indexOf(".")));
    }

    //
	//check the variables and output the result as string
    private String doTheJob(HashMap<String,String> variableCollection){
        this.variableCollection=new HashMap<String,Float>();
        //
        //check if the variables are integer or float
        Iterator iterator=variableCollection.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String> entry=(Map.Entry<String,String>) iterator.next();
            String variableName=entry.getKey();
            String variableValue=entry.getValue();
            if(isNumber(variableValue)){
                this.variableCollection.put(variableName,Float.parseFloat(variableValue));
            }else{
                throw new ClassCastException();
            }
        }
        //
        //
        Float resultFloat=calculate(this.operationCollection);
        return String.valueOf(resultFloat);
    }

	//
    //parse variables into formula data structure and return a float
	private Float calculate(ArrayList<Object[]> operationCollection){
	    float returnValue=0f;
	    for(Object[] objectInstance:operationCollection){
            Object operator=objectInstance[0];
            Object operand=objectInstance[1];
            Float operandFloat=0f;
            if(operand instanceof String){
                String operandString=(String)operand;
                if(variableCollection.containsKey(operandString)){
                    operandFloat=variableCollection.get(operandString);
                }else{
                    operandFloat=Float.parseFloat(operandString);
                }
            }
            else if(operand instanceof ArrayList){
                operandFloat=calculate((ArrayList<Object[]>)operand);
            }else{
                throw new NullPointerException();
            }
            switch ((String)operator){
                case "+":
                    returnValue+=operandFloat;
                    break;
                case "-":
                    returnValue-=operandFloat;
                    break;
                case "*":
                    returnValue*=operandFloat;
                    break;
                case "/":
                    returnValue/=operandFloat;
                    break;
                default:
                    throw new NullPointerException();
            }
        }
        //System.out.println(operationCollection.size()+" "+returnValue);
        return returnValue;
    }

    private ArrayList<Object[]> convertToOperationList(Pattern pattern){
        Stack<ArrayList<Object[]>> operationListStack=new Stack<ArrayList<Object[]>>();   //store operation list
        operationListStack.push(new ArrayList<Object[]>());
        Stack<String> operatorStack=new Stack<String>();    //Whenever an element pushed to operationListStack,an operator pushed to this stack
        operatorStack.push("+");

        String currentOperationString="";
        boolean readyToCreateAnOperation=true;
        int loopInt_1=0;    //this variable indicate current position while looping
        while(loopInt_1<this.formula.length()){
            String currentCharacter=this.formula.substring(loopInt_1,loopInt_1+1);
            if(currentCharacter.equals("(")){
                if(currentOperationString.equals("")){
                    operatorStack.push("+");
                }else{
                    operatorStack.push(currentOperationString);
                    currentOperationString="";
                    readyToCreateAnOperation=true;
                }
                operationListStack.push(new ArrayList<Object[]>());
            }else if(currentCharacter.equals(")")){
                if(currentOperationString.equals("")==false){
                    //
                    //create an operation
                    operationListStack.peek().add(findOperatorAndOperand(currentOperationString,pattern));
                    currentOperationString="";
                    readyToCreateAnOperation=true;
                }
                ArrayList<Object[]> newlyFinishedOperationCollection=operationListStack.peek();
                String operator=operatorStack.peek();
                operationListStack.pop();
                operatorStack.pop();
                Object[] currentOperation=new Object[2];
                currentOperation[0]=operator;
                currentOperation[1]=newlyFinishedOperationCollection;
                operationListStack.peek().add(currentOperation);
            }else if(this.isOperator(currentCharacter)){
                if(currentOperationString.equals("")){
                    //
                    //this is the beginning of an operation
                    currentOperationString=currentCharacter;
                    readyToCreateAnOperation=false;
                }else{
                    //
                    //this is the end of an operation
                    operationListStack.peek().add(findOperatorAndOperand(currentOperationString,pattern));
                    //
                    //and another operation's beginning
                    currentOperationString=currentCharacter;
                }
            }else{
                //
                //variable or number
                if(readyToCreateAnOperation){
                    currentOperationString="+"+currentCharacter;
                    readyToCreateAnOperation=false;
                }else{
                    currentOperationString+=currentCharacter;
                }
                //
                //string end,create a operationMao
                if(loopInt_1==this.formula.length()-1){
                    operationListStack.peek().add(findOperatorAndOperand(currentOperationString,pattern));
                    currentOperationString="";
                }
            }
            loopInt_1++;
        }
        //
        //analyse operation priority
        ArrayList<Object[]> returnOperationCollection=operationListStack.peek();
        loopInt_1=1;
        if(returnOperationCollection.size()>1){
            while(loopInt_1<returnOperationCollection.size()){
                //
                //get operator and operand from this operation
                String thisOperator=(String) returnOperationCollection.get(loopInt_1)[0];
                Object thisOperand= returnOperationCollection.get(loopInt_1)[1];
                //
                //get operator and operand from last operation
                String lastOperator=(String) returnOperationCollection.get(loopInt_1-1)[0];
                Object lastOperand= returnOperationCollection.get(loopInt_1-1)[1];
                //
                if(thisOperator.equals(lastOperator)){
                    //
                    //Nothing to do
                }else{
                    if(operationLevel(thisOperator)<operationLevel(lastOperator)){
                        //
                        //This operation has priority over last operation
                        //
                        //Remove this operation and last operation from operation list
                        returnOperationCollection.remove(loopInt_1-1);
                        returnOperationCollection.remove(loopInt_1-1);
                        //
                        //Create a new nested operation and add to former position
                        Object[] lastOperationMap=new Object[2];
                        lastOperationMap[0]="+";
                        lastOperationMap[1]=lastOperand;
                        Object[] thisOperationMap=new Object[2];
                        thisOperationMap[0]=thisOperator;
                        thisOperationMap[1]=thisOperand;
                        ArrayList<Object[]> nestOperationList=new ArrayList<Object[]>();
                        nestOperationList.add(lastOperationMap);
                        nestOperationList.add(thisOperationMap);
                        Object[] nestOperation=new Object[2];
                        nestOperation[0]=lastOperator;
                        nestOperation[1]=nestOperationList;
                        returnOperationCollection.add(loopInt_1-1,nestOperation);
                        //
                        //adjust the position
                        loopInt_1--;
                    }
                }
                loopInt_1++;
            }
        }
        //printOperationList(returnOperationCollection);
        return returnOperationCollection;
    }

    //
    //Interpret a string,return operator and operand
    private Object[] findOperatorAndOperand(String operationString,Pattern pattern){
        Object[] returnObject=new Object[2];
        Matcher matcherInstance=pattern.matcher(operationString);
        if(matcherInstance.find()){
            String operator=matcherInstance.group(1);
            String operand=matcherInstance.group(2);
            returnObject[0]=operator;
            returnObject[1]=operand;
            return returnObject;
        }else{
            throw new NullPointerException();
        }
    }

    //
    //print the operation
    private void printOperation(Object[] operation,int level){
        Object operator=operation[0];
        Object operand=operation[1];
        if(operand instanceof String){
            for(int forInt_1=0;forInt_1<level-1;forInt_1++){
                System.out.print("    ");
            }
            System.out.println((String) operator+(String) operand);
        }
        else if(operand instanceof ArrayList){
            for(Object[] operationInstance:(ArrayList<Object[]>)operand){
                this.printOperation(operationInstance,level+1);
            }
        }else{
            System.out.println(operand.toString());
        }
    }

    //
    //print the formula data structure
    private void printOperationList(ArrayList<Object[]> operationCollection){
        for(Object[] operationInstance:operationCollection){
            printOperation(operationInstance,1);
        }
    }


    //
    //return priority level of a specific operator
    private int operationLevel(String operator){
        if(operator.equals("*") || operator.equals("/")) {
            return 2;
        }else if(operator.equals("+") || operator.equals("-")){
            return 3;
        }else{
            throw new ClassCastException();
        }
    }

    //
    //check if the string is an integer or a float
    private boolean isNumber(String string){
        Pattern numberCheckPattern=Pattern.compile("[+-]{0,1}[0-9]+\\.*[0-9]*");
        Matcher numberCheckMatcher=numberCheckPattern.matcher(string);
        if(numberCheckMatcher.matches()){
            return true;
        }else{
            return false;
        }
    }

    //
    //check if the string is an operator
    private boolean isOperator(String string){
        if(string.equals("+") || string.equals("-") || string.equals("*") || string.equals("/")){
            return true;
        }else{
            return false;
        }

    }
}
