import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException {
		SyntacticAnalyzer sa = null;
		try {
			String fileName = "test";
			
			File fOutput = new File("output-" + fileName + ".txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(fOutput));
			
			File fError = new File("error-" + fileName + ".txt");
			BufferedWriter error = new BufferedWriter(new FileWriter(fError));
			
			sa = new SyntacticAnalyzer(fileName + ".txt", output, error);
			Boolean parsingResult = sa.parse();
			Boolean checkingResult = sa.check();
			output.append("Parsing Success: " + parsingResult);
			output.newLine();
			output.append("Checking Success: " + checkingResult);
			output.newLine();
			sa.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			sa.close();
		}
	}
}
