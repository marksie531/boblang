package org.bobmarks.xml2java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Converts the contents of a file to a Java String.
 * 
 * @author Bob Marks
 */
public class File2Java {

	public File2Java (String inputFilename, String javaSrcDir, String packageName, String javaClassName) throws Exception {
		System.out.println ("File to Java convertor");
		
		// Open input file
		File inputFile = new File (inputFilename);
		
		if (inputFile.exists()) {
		
			// Create java file
			String javaFullname = javaSrcDir;
			javaFullname = javaFullname.replace("\\", "/");
			if (!javaFullname.endsWith("/"))
				javaFullname = javaFullname + "/";
			javaFullname = javaFullname + packageName.replace(".", "/") + "/" + javaClassName + ".java";
			File javaFile = new File (javaFullname);
			
			// output input and output files
			System.out.println ("Input:  " + inputFile.getAbsolutePath());
			System.out.println ("Output: " + javaFile.getAbsolutePath());
	
			FileWriter out = new FileWriter(javaFile);
			out.write("package " + packageName + ";\n\n");
			out.write("public class " + javaClassName + " {\n\n");
			out.write("   public static final StringBuffer DATA = new StringBuffer ();\n\n");
			out.write("   static {\n");
			
			// Read file
			//BufferedReader br = new BufferedReader(new FileReader(inputFile));
			FileInputStream fis = new FileInputStream(inputFile);
			Reader ur = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(ur);

			
			String nextLine = null;		
			while ((nextLine = br.readLine()) != null) {
				nextLine = nextLine.replace("\"", "\\\"");
				out.write("        DATA.append(\"" + nextLine + "\");\n");
			}
			out.write("    }\n\n}");
			
			out.flush();
			out.close();
			
			br.close();
			
			System.out.println ("Success");
		}
	}
	
	/**
	 * Convert an file into a Java class containing a public static final
	 * String called DATA.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main (String [] args) throws Exception {
		if (args.length == 4)
			new File2Java (args[0], args[1], args[2], args[3]);
		else {
			System.out.println ("Please supply 4 arguments: - <input filename> <output java source dir> <java package> <java classname>");
		}
	}

}
