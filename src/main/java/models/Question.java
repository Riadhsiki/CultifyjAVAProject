package Models;

public class Question {
    private int id_question;
    private int quiz_id;
    private String text_question;
    private String response_prop1;
    private String response_prop2;
    private String response_prop3;
    private String response_correct;

    public Question() {
    }

    public Question(int quiz_id, String text_question, String response_prop1, String response_prop2, String response_prop3, String response_correct) {
        this.quiz_id = quiz_id;
        this.text_question = text_question;
        this.response_prop1 = response_prop1;
        this.response_prop2 = response_prop2;
        this.response_prop3 = response_prop3;
        this.response_correct = response_correct;
    }

    public Question(int id_question, int quiz_id, String text_question, String response_prop1, String response_prop2, String response_prop3, String response_correct) {
        this.id_question = id_question;
        this.quiz_id = quiz_id;
        this.text_question = text_question;
        this.response_prop1 = response_prop1;
        this.response_prop2 = response_prop2;
        this.response_prop3 = response_prop3;
        this.response_correct = response_correct;
    }

    // Getters and Setters
    public int getId_question() {
        return id_question;
    }

    public void setId_question(int id_question) {
        this.id_question = id_question;
    }

    public int getQuiz_id() {
        return quiz_id;
    }

    public void setQuiz_id(int quiz_id) {
        this.quiz_id = quiz_id;
    }

    public String getText_question() {
        return text_question;
    }

    public void setText_question(String text_question) {
        this.text_question = text_question;
    }

    public String getResponse_prop1() {
        return response_prop1;
    }

    public void setResponse_prop1(String response_prop1) {
        this.response_prop1 = response_prop1;
    }

    public String getResponse_prop2() {
        return response_prop2;
    }

    public void setResponse_prop2(String response_prop2) {
        this.response_prop2 = response_prop2;
    }

    public String getResponse_prop3() {
        return response_prop3;
    }

    public void setResponse_prop3(String response_prop3) {
        this.response_prop3 = response_prop3;
    }

    public String getResponse_correct() {
        return response_correct;
    }

    public void setResponse_correct(String response_correct) {
        this.response_correct = response_correct;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id_question=" + id_question +
                ", quiz_id=" + quiz_id +
                ", text_question='" + text_question + '\'' +
                ", response_prop1='" + response_prop1 + '\'' +
                ", response_prop2='" + response_prop2 + '\'' +
                ", response_prop3='" + response_prop3 + '\'' +
                ", response_correct='" + response_correct + '\'' +
                '}';
    }
}