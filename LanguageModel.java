import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        In in = new In(fileName);
        String text = in.readAll();
        int textLength = text.length();
        for (int i = 0; i <= textLength - windowLength - 1; i++) {
            String window = text.substring(i, i + windowLength);
            char nextChar = text.charAt(i + windowLength);
            if (!CharDataMap.containsKey(window)) {
                CharDataMap.put(window, new List());
            }
            List charDataList = CharDataMap.get(window);
            charDataList.update(nextChar);  
    }
    for (List probs : CharDataMap.values()) {
        calculateProbabilities(probs);
    }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
    int totalCount = 0;
    
    ListIterator it = probs.listIterator(0);
    while (it.hasNext()) {
        totalCount += it.next().count;
    }

    if (totalCount == 0) return;

    double currentCP = 0.0; 
    it = probs.listIterator(0);
    
    while (it.hasNext()) {
        CharData cd = it.next(); 
        
        cd.p = (double) cd.count / totalCount;
        
        currentCP += cd.p;
        cd.cp = currentCP;
    }
    }

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
        double rand = randomGenerator.nextDouble();
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            if (rand <= cd.cp) {
                return cd.chr;
            }
        }
        return '\0'; 
    }
    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
       if (initialText.length() < windowLength) {
        return initialText; 
    }   
    
    StringBuilder generatedText = new StringBuilder(initialText);
    String currentWindow = initialText.substring(initialText.length() - windowLength);
    
    for (int i = 0; i < textLength; i++) {
        List charDataList = CharDataMap.get(currentWindow);
        if (charDataList == null) {
            break; 
        }   
        
        char nextChar = getRandomChar(charDataList);
        generatedText.append(nextChar);
        
        currentWindow = currentWindow.substring(1) + nextChar;
    }
    return generatedText.toString();
    }
 

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
int windowLength = Integer.parseInt(args[0]);
        
        String initialText = args[1];
        
        int generatedTextLength = Integer.parseInt(args[2]);
        
        String fileName = args[3];

        LanguageModel lm = new LanguageModel(windowLength);
        
        lm.train(fileName);
        
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
