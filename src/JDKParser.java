  
  
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;  
  
public class JDKParser {  
	
	
	String [] otherTableNames = {"workflow_requestbase","workflow_currentoperator","workflow_requestlog","workflow_forward","workflow_nownode"};
	private static class JDKParserHolder{
        private static final JDKParser jdkParser = new JDKParser();
    } 
	
    public static JDKParser getInstance(){
   	 return JDKParserHolder.jdkParser;
    }
    
    public List<String> getStrlist (String classPath){
    	// 扫描代码得到数据
    	try {
			ClassBean classOld = parseMethodDefs(classPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    } 
    
    // 对比的业务逻辑
    public List<CompMessage> Comparison(String oldClassPath,String newClassPath) throws Exception{
    	// 扫描代码得到数据
    	ClassBean classOld = parseMethodDefs(oldClassPath);
        ClassBean classNew = parseMethodDefs(newClassPath);
        
        boolean status6 = true; // 修改了类的修饰域
        List<CompMessage> list = new ArrayList<>();
        if(!comparison(classNew.getClassModifiers(),classOld.getClassModifiers())){
        	status6 =false;
        }else{
        	CompMessage result = new CompMessage(classNew.getName(),"类","将类的修饰域从大修改到小");
        	list.add(result);
        }
        // 方法对比
        List<MethodBean> listMethodNew = classNew.getListMethod();
        List<MethodBean> listMethodOld = classOld.getListMethod();
       
        for(MethodBean meBeanOld : listMethodOld){
            boolean status1 = true; // 减少了方法
            boolean status2 = true; // 返回值变了
            boolean status3 = true; // 参数个数改变了
            boolean status4 = true; // 参数改了
            boolean status5 = true; // 将方法的修饰域从大修改到小
            
        	for(MethodBean meBean : listMethodNew){
        		if(meBeanOld.getName().equals(meBean.getName())){
        			status1 = false;
        			if(meBeanOld.getReturnType()==null || meBeanOld.getReturnType().equals(meBean.getReturnType())){
        				status2 = false;
        			}
        			if(meBeanOld.getParamsize()==meBean.getParamsize()){
        				status3 = false;
        				if(meBean.getParams().containsAll(meBeanOld.getParams()) && meBeanOld.getParams().containsAll(meBean.getParams())){
        					status4 = false;
        				}
        			}else{
        				status4 = false;
        			}
        			if(!comparison(meBean.getModifiers(), meBeanOld.getModifiers())){
        				status5 = false;
        			}
        		}
        	}
        	if(status1){
        		list.add(new CompMessage(classNew.getName(),"类","减少了方法"+meBeanOld.getName()));
        	}else{
        		if(status2)list.add(new CompMessage(meBeanOld.getName(),"方法","返回值改变了"));
        		if(status3)list.add(new CompMessage(meBeanOld.getName(),meBeanOld.isConstruction()?"构造方法":"方法","参数个数改变了"));
        		if(status4)list.add(new CompMessage(meBeanOld.getName(),meBeanOld.isConstruction()?"构造方法":"方法","参数改了"));
        		if(status5)list.add(new CompMessage(meBeanOld.getName(),meBeanOld.isConstruction()?"构造方法":"方法","将方法的修饰域从大修改到小"));
        	}
        }
        
        // 抽象与接口类，对比关注增加的部份
        if(classNew.isAbstract() || classNew.isInterface()){
        	for(MethodBean meBean : listMethodNew){
        		boolean status7 = true; // 给interface增加了抽象方法（没有默认实现）
        		boolean status8 = true; // 给抽象类增加了抽象方法
            	for(MethodBean meBeanOld : listMethodOld){
            		if(meBeanOld.getName().equals(meBean.getName())){
            			status7 = false;
            			status8 = false;
            		}
            	}
            	if(status7 && meBean.isDefault() && classNew.isInterface()){
            		status7 = false;
            	}
            	if(status7 && classNew.isInterface())list.add(new CompMessage(classNew.getName(),"接口","给interface增加了抽象方法"+meBean.getName()+"（没有默认实现）"));
            	if(status8 && meBean.isAbstract())list.add(new CompMessage(classNew.getName(),"抽象类","给抽象类增加了抽象方法"+meBean.getName()));
            }
        }
        list.addAll(ComparisonSql(classOld,classNew));
        System.out.println(list.toString());  
        return list;
    }
    
    private List<CompMessage> ComparisonSql(ClassBean classOld,ClassBean classNew ){
    	CompMessage compMessage = null;
    	List<String> oldList = classOld.getSqlList();
    	List<String> newList = classNew.getSqlList();
    	List<CompMessage> list = new ArrayList<>();
		for(String newStr : newList){
			String str = newStr.toLowerCase();
			for(String otherTableName : otherTableNames){
				if(str.indexOf(otherTableName)!=-1){
					if(str.indexOf("delete")!=-1 && !oldList.contains(newStr)){
						compMessage = new CompMessage(classNew.getName(),"sql字符","新增delete语句:"+newStr.replace("\"", ""));
						list.add(compMessage);
					}
					if(str.indexOf("truncate")!=-1 && !oldList.contains(newStr)){
						compMessage = new CompMessage(classNew.getName(),"sql字符","新增truncate语句:"+newStr.replace("\"", ""));
						list.add(compMessage);
					}
				}
			}
		}
    	return list;
    }
    
    private ParserFactory factory;  
  
    private JDKParser() {  
        factory = getParserFactory();  
    }  
  
    private ClassBean parseMethodDefs(String file) throws IOException {  
        JCCompilationUnit unit = parse(file);  
        MethodScanner scanner = new MethodScanner();  
        return scanner.visitCompilationUnit(unit, new ClassBean());  
    }  
  
    JCCompilationUnit parse(String file) throws IOException {  
        Parser parser = factory.newParser(readFile(file), true, false, true);  
        return parser.parseCompilationUnit();  
    }  
  
    private ParserFactory getParserFactory() {  
        Context context = new Context();  
        JavacFileManager.preRegister(context);  
        ParserFactory factory = ParserFactory.instance(context);  
        return factory;  
    }  
  
    CharSequence readFile(String file) throws IOException {  
        FileInputStream fin = null;  
        FileChannel ch = null; 
        ByteBuffer buffer = null;
        try{
        	fin = new FileInputStream(file);
        	ch = fin.getChannel(); 
        	buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());  
        }catch (Exception e) {
		}
        if(fin!=null){
        	fin.close();
        }
        if(ch!=null){
        	ch.close();
        }
        return Charset.defaultCharset().decode(buffer);  
    }  
      
    //扫描方法时，把方法名加入到一个list中  
    static class MethodScanner extends  
            TreeScanner<ClassBean, ClassBean> {  
    	public MethodScanner(){
    		strMap = new HashMap<String, String>();
    	}
    	private Map<String,String> strMap = null;
        @Override  
        public ClassBean visitMethod(MethodTree node, ClassBean p) {  
            Tree tree = node.getReturnType();
            ModifiersTree mtree =  node.getModifiers();
            List<VariableTree> params = (List<VariableTree>) node.getParameters();
            List<String> paramsList = new ArrayList<>();
            for(VariableTree vt : params){
            	paramsList.add(vt.getType()+"");
            }
            MethodBean methodBean = new MethodBean();
            methodBean.setName(node.getName().toString());
            if(tree == null){
            	methodBean.setConstruction(true);
            	methodBean.setName(p.getName().toString());
            }else{
            	methodBean.setReturnType(tree.toString());
            }
            String modifier = mtree.toString().trim();
            if(modifier.indexOf("abstract")!=-1){
            	methodBean.setAbstract(true);
                modifier = modifier.replace("abstract", "");
            }
            if(modifier.indexOf("default")!=-1){
            	methodBean.setDefault(true);
            }
            methodBean.setModifiers(modifier);
            methodBean.setParams(paramsList);
            methodBean.setParamsize(params.size());
            p.setListMethod(methodBean);
            super.visitMethod(node, p);
            return p;  
        } 
        
        @Override
        public ClassBean visitClass(ClassTree node, ClassBean p) {
    		String modifiers = node.getModifiers()+"";
    		modifiers = modifiers.replace("final", "");
    		p.setClassModifiers(modifiers.trim());
    		if(modifiers.indexOf("abstract")!=-1){
    			p.setClassModifiers(modifiers.replace("abstract", "").trim());
    			p.setAbstract(true);
    		}
    		
    		if(modifiers.indexOf("interface")!=-1){
    			p.setClassModifiers(modifiers.replace("interface", "").trim());
    			p.setInterface(true);
    		}
    		p.setName(node.getSimpleName()+"");
    		super.visitClass(node, p);
        	return p;  
    	}
    	
    	public ClassBean visitExpressionStatement(ExpressionStatementTree arg0, ClassBean p) {
    		String vietStr = arg0.toString();
    		if(vietStr.indexOf("=")!=-1){
    			String[] strs = vietStr.split("=");
    			String etStrTmp = strs[1].toString();
    			String [] etStrs = strs[1].split("\\+");
        		for(String etStr : etStrs){
        			if(etStr.indexOf("\"")==-1){
        				String transtr = strMap.get(etStr.trim());
        				if(transtr!=null && !transtr.equals("")){
        					etStrTmp = etStrTmp.replace(etStr, transtr);
        				}
        			}
        		}
//        		System.out.println(etStrTmp);
        		p.setSqlList(etStrTmp);
    		}
    		return p;
    	}
    	public ClassBean visitBinary(BinaryTree arg0, ClassBean p) {
    		p.setSqlList(arg0.toString());
    		String etStrTmp = arg0.toString();
    		String [] etStrs = etStrTmp.split("\\+");
    		for(String etStr : etStrs){
    			if(etStr.indexOf("\"")==-1){
    				String transtr = strMap.get(etStr.trim());
    				if(transtr!=null && !transtr.equals("")){
    					etStrTmp = etStrTmp.replace(etStr, transtr);
    				}
    			}
    		}
//    		System.out.println(etStrTmp);
    		p.setSqlList(etStrTmp);
    		return p;
    	}
    	
    	public ClassBean visitVariable(VariableTree arg0, ClassBean p) {
    		ExpressionTree et = arg0.getInitializer();
    		if(et == null){
    			return p;
    		}
    		String etStrTmp = et.toString();
    		String [] etStrs = etStrTmp.split("\\+");
    		for(String etStr : etStrs){
    			if(etStr.indexOf("\"")==-1){
    				String transtr = strMap.get(etStr.trim());
    				if(transtr!=null && !transtr.equals("")){
    					etStrTmp = etStrTmp.replace(etStr, transtr);
    				}
    			}
    		}
    		strMap.put(arg0.getName().toString(), etStrTmp);
    		p.setSqlList(etStrTmp);
    		System.out.println(arg0.getName()+"="+etStrTmp);
    		return null;
    	}
        
        
    	
    }  
    // 修饰符判断
    public boolean comparison(String mod1,String mod2){
    	mod1 = "".equals(mod1)?"protected":mod1;
    	mod2 = "".equals(mod2)?"protected":mod2;
    	int num = "private".equals(mod1)?1:0;
    	num = "protected".equals(mod1)?2:num;
    	num = "public".equals(mod1)?3:num;
    	int num2 = "private".equals(mod2)?1:0;
    	num2 = "protected".equals(mod2)?2:num;
    	num2 = "public".equals(mod2)?3:num;
    	return num2>num;
    }
    
    public static void main(String[] args) throws IOException { 
    	try {
			JDKParser.getInstance().Comparison("I:\\19年11月\\04\\1\\test_before\\test\\MethodNameChange.java", "I:\\19年11月\\04\\1\\test_after\\test\\MethodNameChange.java");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	 
    	
        
    }
}  