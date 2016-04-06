import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException {
		SyntacticAnalyzer sa = null;
		try {
			String fileName = "test.txt";
			
			File fOutput = new File("output-" + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(fOutput));
			
			File fError = new File("error-" + fileName);
			BufferedWriter error = new BufferedWriter(new FileWriter(fError));
			
			sa = new SyntacticAnalyzer(fileName, output, error);
			Boolean result = sa.parse();
			output.append("Success: " + result);
			output.newLine();
			sa.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			sa.close();
		}
	}
}
