package fishpowered.best.browser.utilities;

/**
 * A sentence might get split up due to length, pauses, audio clips etc.
 * The original sentence can be determined by looking at the sentence id
 */
public class SentencePart {

    private String text;
    private final String nodeType;
    private final int sentenceId;
    private final int elementId;
    private final boolean isAtBeginningOfElement;
    private final boolean isAtEndOfElement;

    /**
     *
     * @param text
     * @param nodeType
     * @param sentenceId
     * @param elementId
     * @param isAtBeginningOfElement
     * @param isAtEndOfElement
     */
    public SentencePart(String text, String nodeType, int sentenceId, int elementId, boolean isAtBeginningOfElement, boolean isAtEndOfElement){

        this.text = text.trim();
        this.nodeType = nodeType;
        this.sentenceId = sentenceId;
        this.elementId = elementId;
        this.isAtBeginningOfElement = isAtBeginningOfElement;
        this.isAtEndOfElement = isAtEndOfElement;
    }

    public String getText() {
        return text;
    }

    public int getSentenceId() {
        return sentenceId;
    }

    public int getParagraphElementId() {
        return elementId;
    }

    public String getNodeType(){
        return nodeType;
    }

    public boolean isAtEndOfElement() {
        return isAtEndOfElement;
    }

    public boolean isAtBeginningOfElement() {
        return isAtBeginningOfElement;
    }

    public int getWordCount() {
        return text.split("\\s+").length;
    }

    /*public void setPauseAtEnd(int i) {
        /*if(this.text.endsWith(".")){
            this.text = this.text.substring(0, this.text.length()-1);
        }
        this.text = this.text+StringHelper.repeat(getPause(), i);
    }

    public void setPauseAtBeginning(int i) {
        //this.text = StringHelper.repeat(getPause(), i)+" "+this.text;
    }

    public static String getPause(){
        //return ".";
    }*/
}
